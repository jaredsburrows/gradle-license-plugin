package com.jaredsburrows.license

import com.jaredsburrows.license.internal.ConsoleRenderer
import com.jaredsburrows.license.internal.report.CsvReport
import com.jaredsburrows.license.internal.report.HtmlReport
import com.jaredsburrows.license.internal.report.JsonReport
import com.jaredsburrows.license.internal.report.Report
import com.jaredsburrows.license.internal.report.TextReport
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileReader
import java.net.URI
import java.net.URL
import java.util.Locale
import java.util.UUID

/** A [org.gradle.api.Task] that creates HTML and JSON reports of the current projects dependencies. */
internal open class LicenseReportTask : DefaultTask() { // tasks can't be final

  @Input
  var assetDirs = emptyList<File>()

  @Optional
  @Input
  var variantName: String? = null

  // This input is used by the task indirectly via some project properties (such as "configurations" and "dependencies")
  // that affect the task's outcome. When the mentioned project properties change the task should re-run the next time
  // it is requested and should *not* be marked as UP-TO-DATE.
  @InputFile
  var buildFile: File? = null

  // Task annotations cannot be internal
  @get:OutputDirectory
  lateinit var outputDir: File

  @Input
  var generateCsvReport = false

  @Input
  var generateHtmlReport = false

  @Input
  var generateJsonReport = false

  @Input
  var generateTextReport = false

  @Input
  var copyCsvReportToAssets = false

  @Input
  var copyHtmlReportToAssets = false

  @Input
  var copyJsonReportToAssets = false

  @Input
  var copyTextReportToAssets = false

  @Input
  var useVariantSpecificAssetDirs = false

  @Input
  var ignoredPatterns = setOf<String>()

  private val projects = mutableListOf<Model>()
  private var pomConfiguration = "poms"
  private var tempPomConfiguration = "tempPoms"

  init {
    // From DefaultTask
    description = "Outputs licenses report for $name."
    group = "Reporting"
  }

  @TaskAction
  fun licenseReport() {
    val mavenReader = MavenXpp3Reader()
    val configurations: ConfigurationContainer = project.configurations
    val dependencies: DependencyHandler = project.dependencies

    setupEnvironment(configurations)
    initDependencies(configurations)
    generatePOMInfo(mavenReader, configurations, dependencies)

    // Create CSV report
    if (generateCsvReport) {
      val csvReport = CsvReport(projects)
      val csvFile = File(outputDir, "$name.${csvReport.extension()}")
      createReport(file = csvFile) { csvReport }

      // If android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyCsvReportToAssets) {
        copyReport(file = csvFile) { csvReport }
      }
    }

    // Create HTML report
    if (generateHtmlReport) {
      val htmlReport = HtmlReport(projects)
      val htmlFile = File(outputDir, "$name.${htmlReport.extension()}")
      createReport(file = htmlFile) { htmlReport }

      // If android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyHtmlReportToAssets) {
        copyReport(file = htmlFile) { htmlReport }
      }
    }

    // Create JSON report
    if (generateJsonReport) {
      val jsonReport = JsonReport(projects)
      val jsonFile = File(outputDir, "$name.${jsonReport.extension()}")
      createReport(file = jsonFile) { jsonReport }

      // If android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyJsonReportToAssets) {
        copyReport(file = jsonFile) { jsonReport }
      }
    }

    // Create Text report
    if (generateTextReport) {
      val textReport = TextReport(projects)
      val textFile = File(outputDir, "$name.${textReport.extension()}")
      createReport(file = textFile) { textReport }

      // If android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyTextReportToAssets) {
        copyReport(file = textFile) { textReport }
      }
    }
  }

  /** Setup configurations to collect dependencies. */
  private fun setupEnvironment(configurations: ConfigurationContainer) {
    pomConfiguration += variantName.orEmpty() + UUID.randomUUID()
    tempPomConfiguration += variantName.orEmpty() + UUID.randomUUID()

    // Create temporary configuration in order to store POM information
    configurations.apply {
      create(pomConfiguration)

      forEach { configuration ->
        try {
          configuration.isCanBeResolved = true
        } catch (e: Exception) {
          logger.warn("Cannot resolve configuration ${configuration.name}", e)
        }
      }
    }
  }

  /** Iterate through all configurations and collect dependencies. */
  private fun initDependencies(configurations: ConfigurationContainer) {
    // Add POM information to our POM configuration
    val configurationSet = linkedSetOf<Configuration>()
    val configurationList = mutableListOf("api", "compile", "implementation")

    // If Android project, add extra configurations
    variantName?.let { variant ->
      configurations
        .find { it.name == "${variant}RuntimeClasspath" }
        ?.also { configurationList += it.name }
    }

    // Iterate through all the configuration's dependencies
    configurations
      .filter { configurationList.contains(it.name) }
      .forEach { configurationSet += it }

    // Resolve the POM artifacts
    configurationSet
      .filter { it.isCanBeResolved }
      .map { it.resolvedConfiguration }
      .map { it.lenientConfiguration }
      .map { it.allModuleDependencies }
      .flatMap { getResolvedArtifactsFromResolvedDependencies(it) }
      .forEach { artifact ->
        val id = artifact.moduleVersion.id
        val gav = "${id.group}:${id.name}:${id.version}@pom"
        configurations
          .getByName(pomConfiguration)
          .dependencies += project.dependencies.add(pomConfiguration, gav)
      }
  }

  /** Get POM information from the dependency artifacts. */
  private fun generatePOMInfo(
    mavenReader: MavenXpp3Reader,
    configurations: ConfigurationContainer,
    dependencies: DependencyHandler,
  ) {
    // Iterate through all POMs in order from our custom POM configuration
    configurations
      .getByName(pomConfiguration)
      .resolvedConfiguration
      .lenientConfiguration
      .artifacts
      .filter { it.type == "pom" }
      // Filter out artifacts for ignored patterns
      .filter { artifact ->
        val depString = with(artifact.moduleVersion.id) { "$group:$name:$version" }
        ignoredPatterns.none { depString.contains(it) }
      }
      .map { artifact ->
        // POM of artifact
        val pomFile = artifact.file
        val model = mavenReader.read(FileReader(pomFile), false)

        // Search for licenses
        var licenses = findLicenses(mavenReader, pomFile, configurations, dependencies)
        if (licenses.isEmpty()) {
          logger.warn("$name dependency does not have a license.")
          licenses = mutableListOf()
        }

        // Store the information that we need
        val module = artifact.moduleVersion.id
        val project = Model().apply {
          this.groupId = module.group.orEmpty().trim()
          this.artifactId = module.name.orEmpty().trim()
          this.version = model.pomVersion(mavenReader, pomFile, configurations, dependencies)
          this.name = model.pomName()
          this.description = model.pomDescription()
          this.url = model.pomUrl()
          this.inceptionYear = model.pomInceptionYear()
          this.licenses = licenses
          this.developers = model.pomDevelopers()
        }

        projects += project
      }

    // Sort POM information by name
    projects.sortBy { it.name.lowercase(Locale.getDefault()) }
  }

  private fun getResolvedArtifactsFromResolvedDependencies(
    resolvedDependencies: Set<ResolvedDependency>,
  ): Set<ResolvedArtifact> {
    val resolvedArtifacts = hashSetOf<ResolvedArtifact>()
    resolvedDependencies.forEach { resolvedDependency ->
      try {
        when (resolvedDependency.moduleVersion) {
          /**
           * Attempting to getAllModuleArtifacts on a local library project will result
           * in AmbiguousVariantSelectionException as there are not enough criteria
           * to match a specific variant of the library project. Instead, we skip the
           * library project itself and enumerate its dependencies.
           */
          "unspecified" -> resolvedArtifacts += getResolvedArtifactsFromResolvedDependencies(
            resolvedDependency.children,
          )

          else -> resolvedArtifacts += resolvedDependency.allModuleArtifacts
        }
      } catch (e: Exception) {
        logger.warn("Failed to process ${resolvedDependency.name}", e)
      }
    }
    return resolvedArtifacts
  }

  /** Use Parent POM information when individual dependency license information is missing. */
  private fun getParentPomFile(
    model: Model,
    configurations: ConfigurationContainer,
    dependencies: DependencyHandler,
  ): File? {
    // Get parent POM information
    val parent = model.parent
    val groupId = parent?.groupId.orEmpty()
    val artifactId = parent?.artifactId.orEmpty()
    val version = parent?.version.orEmpty()
    val dependency = "$groupId:$artifactId:$version@pom"

    // Add dependency to temporary configuration
    configurations.create(tempPomConfiguration)
    configurations
      .getByName(tempPomConfiguration)
      .dependencies += dependencies.add(tempPomConfiguration, dependency)

    val pomFile = configurations
      .getByName(tempPomConfiguration)
      .resolvedConfiguration
      .lenientConfiguration
      .artifacts
      .firstOrNull { it.type == "pom" }
      ?.file

    // Reset dependencies in temporary configuration
    configurations.remove(configurations.getByName(tempPomConfiguration))

    return pomFile
  }

  private fun <T : Report> createReport(file: File, report: () -> T) {
    val newReport = report()

    file.apply {
      // Remove existing file
      delete()

      // Write report for file
      parentFile.mkdirs()
      writeText(newReport.toString())
    }

    // Log output directory for user
    logger.lifecycle(
      "Wrote ${newReport.name()} report to ${ConsoleRenderer().asClickableFileUrl(file)}.",
    )
  }

  private fun <T : Report> copyReport(file: File, report: () -> T) {
    val newReport = report()

    // Iterate through all asset directories
    assetDirs.forEach { directory ->
      val licenseFile = File(directory.path, "$OPEN_SOURCE_LICENSES.${newReport.extension()}")

      licenseFile.apply {
        // Remove existing file
        delete()

        // Write report for file
        parentFile.mkdirs()
        writeText(file.readText())
      }

      // Log output directory for user
      logger.lifecycle(
        "Copied ${newReport.name()} report to ${ConsoleRenderer().asClickableFileUrl(licenseFile)}.",
      )
    }
  }

  private fun findVersion(
    mavenReader: MavenXpp3Reader,
    pomFile: File?,
    configurations: ConfigurationContainer,
    dependencies: DependencyHandler,
    recursionDepth: Int = 0,
  ): String {
    if (recursionDepth == MAX_RECURSION_DEPTH) {
      logger.warn("Failed to find version after $recursionDepth attempts: $pomFile")
      return ""
    }

    if (pomFile.isNullOrEmpty()) {
      return ""
    }
    val model = mavenReader.read(FileReader(pomFile!!), false)

    // If the POM is missing a name, do not record it
    val name = model.pomName()
    if (name.isEmpty()) {
      logger.warn("POM file is missing a name: $pomFile")
      return ""
    }

    val version = model.version.orEmpty().trim()
    if (version.isNotEmpty()) {
      return version.trim()
    }

    if (model.parent.artifactId.orEmpty().trim().isNotEmpty()) {
      return findVersion(
        mavenReader,
        getParentPomFile(model, configurations, dependencies),
        configurations,
        dependencies,
        recursionDepth = recursionDepth + 1,
      )
    }
    return ""
  }

  private fun findLicenses(
    mavenReader: MavenXpp3Reader,
    pomFile: File?,
    configurations: ConfigurationContainer,
    dependencies: DependencyHandler,
    recursionDepth: Int = 0,
  ): List<License> {
    if (recursionDepth == MAX_RECURSION_DEPTH) {
      logger.warn("Failed to find license after $recursionDepth attempts: $pomFile")
      return emptyList()
    }

    println("findLicenses $pomFile")
    if (pomFile.isNullOrEmpty()) {
      return mutableListOf()
    }
    val model = mavenReader.read(FileReader(pomFile!!), false)

    // If the POM is missing a name, do not record it
    val name = model.pomName()
    if (name.isEmpty()) {
      logger.warn("POM file is missing a name: $pomFile")
      return mutableListOf()
    }

    if (ANDROID_SUPPORT_GROUP_ID == model.groupId.orEmpty().trim()) {
      return listOf(
        License().apply {
          this.name = APACHE_LICENSE_NAME
          url = APACHE_LICENSE_URL
        },
      )
    }

    // License information found
    if (model.licenses.orEmpty().isNotEmpty()) {
      val licenses = mutableListOf<License>()
      model.licenses.orEmpty().forEach { license ->
        val licenseName = license.name.orEmpty().trim()
        val licenseUrl = license.url.orEmpty().trim()
        if (licenseUrl.isUrlValid()) {
          licenses += License().apply {
            this.name = licenseName
            url = licenseUrl
          }
        }
      }
      return licenses
    }

    logger.info("Project, $name, has no license in POM file.")

    if (model.parent?.artifactId.orEmpty().trim().isNotEmpty()) {
      return findLicenses(
        mavenReader,
        getParentPomFile(model, configurations, dependencies),
        configurations,
        dependencies,
        recursionDepth = recursionDepth + 1,
      )
    }
    return mutableListOf()
  }

  private fun String.isUrlValid(): Boolean {
    var uri: URI? = null
    try {
      uri = URL(this).toURI()
    } catch (e: Exception) {
      logger.warn("$this dependency has an invalid license URL; skipping license", e)
    }
    return uri != null
  }

  private fun Model.pomVersion(
    mavenReader: MavenXpp3Reader,
    pomFile: File?,
    configurations: ConfigurationContainer,
    dependencies: DependencyHandler,
  ): String {
    return version.orEmpty().trim()
      .ifEmpty { findVersion(mavenReader, pomFile, configurations, dependencies) }
  }

  private fun Model.pomName(): String {
    return name.orEmpty().trim().ifEmpty { artifactId.orEmpty().trim() }
  }

  private fun Model.pomDescription(): String {
    return description.orEmpty().trim()
  }

  private fun Model.pomUrl(): String {
    return url.orEmpty().trim()
  }

  private fun Model.pomInceptionYear(): String {
    return inceptionYear.orEmpty().trim()
  }

  private fun Model.pomDevelopers(): List<Developer> {
    val developers = mutableListOf<Developer>()
    this.developers.orEmpty().forEach { developer ->
      developers += Developer().apply {
        id = developer.name.orEmpty().trim()
      }
    }
    return developers
  }

  private fun File?.isNullOrEmpty(): Boolean = this == null || this.length() == 0L

  private companion object {
    private const val ANDROID_SUPPORT_GROUP_ID = "com.android.support"
    private const val APACHE_LICENSE_NAME = "The Apache Software License"
    private const val APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    private const val OPEN_SOURCE_LICENSES = "open_source_licenses"
    private const val MAX_RECURSION_DEPTH = 5
  }
}
