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
import org.codehaus.plexus.util.ReaderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import java.io.File
import java.net.URL
import java.util.Locale

/** A [org.gradle.api.Task] that creates HTML and JSON reports of the current projects dependencies. */
internal open class LicenseReportTask : DefaultTask() {
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
    pomConfiguration += variantName.orEmpty() + name

    // Create temporary configuration in order to store POM information
    configurations.apply {
      create(pomConfiguration)

      forEach { configuration ->
        try {
          configuration.isCanBeResolved = true
        } catch (e: Exception) {
          logger.warn("Cannot resolve configuration ${configuration.name}: ${e.shortMessage()}")
          logger.debug("Cannot resolve configuration ${configuration.name}", e)
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
      .asSequence()
      .filter { it.isCanBeResolved }
      .map { it.resolvedConfiguration }
      .map { it.lenientConfiguration }
      .map { it.allModuleDependencies }
      .flatMap { getResolvedArtifactsFromResolvedDependencies(it) }
      .toList()
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
        val model = mavenReader.read(ReaderFactory.newXmlReader(pomFile), false)

        // Search for licenses
        var licenses = findLicenses(mavenReader, pomFile, dependencies)
        if (licenses.isEmpty()) {
          logger.warn("Dependency '${artifact.name}' does not have a license.")
          licenses = mutableListOf()
        }

        // Store the information that we need
        val module = artifact.moduleVersion.id
        val project =
          Model().apply {
            this.groupId = module.group.trim()
            this.artifactId = module.name.trim()
            this.version = model.pomVersion(mavenReader, pomFile, dependencies)
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
    skipSet: MutableSet<ResolvedDependency> = hashSetOf(),
  ): Set<ResolvedArtifact> {
    return resolvedDependencies.flatMap { resolvedDependency ->
      if (!skipSet.add(resolvedDependency)) {
        // If the dependency is already in skipSet, skip it
        return@flatMap emptySet<ResolvedArtifact>()
      }

      try {
        when (resolvedDependency.moduleVersion) {
          /**
           * Attempting to getAllModuleArtifacts on a local library project will result
           * in AmbiguousVariantSelectionException as there are not enough criteria
           * to match a specific variant of the library project. Instead, we skip the
           * library project itself and enumerate its dependencies.
           */
          "unspecified" ->
            // Recursively collect artifacts from the children of unresolved dependencies
            getResolvedArtifactsFromResolvedDependencies(resolvedDependency.children, skipSet)
          else ->
            // Collect artifacts from the resolved dependency
            resolvedDependency.allModuleArtifacts
        }
      } catch (e: Exception) {
        logger.warn("Failed to process '${resolvedDependency.name}': ${e.shortMessage()}")
        logger.debug("Failed to process '${resolvedDependency.name}'", e)
        emptySet()
      }
    }.toSet()
  }

  /** Use Parent POM information when individual dependency license information is missing. */
  private fun getParentPomFile(
    model: Model,
    dependencies: DependencyHandler,
  ): File? {
    // Get parent POM information
    val parent = model.parent
    val groupId = parent?.groupId.orEmpty()
    val artifactId = parent?.artifactId.orEmpty()
    val version = parent?.version.orEmpty()
    val dependency = "$groupId:$artifactId:$version@pom"

    val result = dependencies.createArtifactResolutionQuery()
      .forModule(groupId, artifactId, version)
      .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
      .execute()

    var pomFile: File? = null
    for (component in result.resolvedComponents) {
      for (artifact in component.getArtifacts(MavenPomArtifact::class.java)) {
        if (artifact is ResolvedArtifactResult) {
          if (pomFile != null) {
            logger.error("Parent POM ${dependency} resolved to multiple artifacts")
            return null
          }
          pomFile = artifact.file
        }
      }
    }

    if (pomFile == null) {
      logger.warn("Parent POM ${dependency} not found")
    }
    return pomFile
  }

  private fun <T : Report> createReport(
    file: File,
    report: () -> T,
  ) {
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

  private fun <T : Report> copyReport(
    file: File,
    report: () -> T,
  ) {
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
    dependencies: DependencyHandler,
  ): String {
    if (pomFile.isNullOrEmpty()) {
      return ""
    }
    val model = mavenReader.read(ReaderFactory.newXmlReader(pomFile), false)

    // If the POM is missing a name, do not record it
    val name = model.pomName()
    if (name.isEmpty()) {
      logger.warn("POM file is missing a name: $pomFile")
      return ""
    }

    val version = model.pomVersion()
    if (version.isNotEmpty()) {
      return version.trim()
    }

    if (model.parent.artifactId.orEmpty().trim().isNotEmpty()) {
      return findVersion(
        mavenReader,
        getParentPomFile(model, dependencies),
        dependencies,
      )
    }
    return ""
  }

  private fun findLicenses(
    mavenReader: MavenXpp3Reader,
    pomFile: File?,
    dependencies: DependencyHandler,
  ): List<License> {
    if (pomFile.isNullOrEmpty()) {
      return emptyList()
    }
    val model = mavenReader.read(ReaderFactory.newXmlReader(pomFile), false)

    // If the POM is missing a name, do not record it
    val name = model.pomName()
    if (name.isEmpty()) {
      logger.warn("POM file is missing a name: $pomFile")
      return emptyList()
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
    return model.licenses.orEmpty().map { license ->
      License().apply {
        this.name = license.name.orEmpty().trim()
        this.url = license.url.orEmpty().trim()
      }
    }.filter {
      it.name.isNotEmpty() || it.url.isUrlValid()
    }.ifEmpty {
      logger.info("Project, $name, has no license in POM file.")
      model.parent?.artifactId.orEmpty().trim().takeIf { it.isNotEmpty() }?.let {
        findLicenses(mavenReader, getParentPomFile(model, dependencies), dependencies)
      } ?: emptyList()
    }
  }

  private fun String.isUrlValid(): Boolean {
    return try {
      URL(this).toURI()
      true
    } catch (e: Exception) {
      logger.warn("Dependency has an invalid license URL '$this': ${e.shortMessage()}")
      logger.debug("Dependency has an invalid license URL '$this'", e)
      false
    }
  }

  private fun Model.pomVersion(
    mavenReader: MavenXpp3Reader,
    pomFile: File?,
    dependencies: DependencyHandler,
  ): String = version.orEmpty().trim().ifEmpty { findVersion(mavenReader, pomFile, dependencies) }

  private fun Model.pomName(): String = name.orEmpty().trim().ifEmpty { artifactId.orEmpty().trim() }

  private fun Model.pomDescription(): String = description.orEmpty().trim()

  private fun Model.pomUrl(): String = url.orEmpty().trim()

  private fun Model.pomVersion(): String = version.orEmpty().trim()

  private fun Model.pomInceptionYear(): String = inceptionYear.orEmpty().trim()

  private fun Model.pomDevelopers(): List<Developer> {
    return developers.orEmpty().map { developer ->
      Developer().apply {
        id = developer.name.orEmpty().trim()
      }
    }
  }

  private fun File?.isNullOrEmpty(): Boolean = this?.length() == 0L

  private fun Exception.shortMessage(): String =
    (message ?: "<no message>").let {
      if (it.length > MAX_EXCEPTION_MESSAGE_LENGTH) {
        "${it.take(MAX_EXCEPTION_MESSAGE_LENGTH)}... (see --debug for complete message)"
      } else {
        it
      }
    }

  private companion object {
    private const val ANDROID_SUPPORT_GROUP_ID = "com.android.support"
    private const val APACHE_LICENSE_NAME = "The Apache Software License"
    private const val APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    private const val OPEN_SOURCE_LICENSES = "open_source_licenses"
    private const val MAX_EXCEPTION_MESSAGE_LENGTH = 200
  }
}
