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
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileReader
import java.net.URI
import java.net.URL
import java.util.Locale
import java.util.UUID

/** A [org.gradle.api.Task] that creates HTML and JSON reports of the current projects dependencies. */
internal open class LicenseReportTask : BaseLicenseReportTask() { // tasks can't be final

  @Input var assetDirs = emptyList<File>()
  @Optional @Input var variantName: String? = null

  private val projects = mutableListOf<Model>()
  private var pomConfiguration = "poms"
  private var tempPomConfiguration = "tempPoms"

  @TaskAction fun licenseReport() {
    val mavenReader = MavenXpp3Reader()

    setupEnvironment()
    initDependencies()
    generatePOMInfo(mavenReader)

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
  private fun setupEnvironment() {
    pomConfiguration += variantName.orEmpty() + UUID.randomUUID()
    tempPomConfiguration += variantName.orEmpty() + UUID.randomUUID()

    // Create temporary configuration in order to store POM information
    project.configurations.apply {
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
  private fun initDependencies() {
    // Add POM information to our POM configuration
    val configurationSet = linkedSetOf<Configuration>()
    val configurationList = mutableListOf("api", "compile", "implementation")

    // If Android project, add extra configurations
    variantName?.let { variant ->
      project.configurations.find { it.name == "${variant}RuntimeClasspath" }?.also {
        configurationList.add(it.name)
      }
    }

    // Iterate through all the configuration's dependencies
    project.configurations
      .filter { configurationList.contains(it.name) }
      .forEach { configurationSet.add(it) }

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
        project.configurations.getByName(pomConfiguration).dependencies.add(
          project.dependencies.add(pomConfiguration, gav)
        )
      }
  }

  /** Get POM information from the dependency artifacts. */
  private fun generatePOMInfo(mavenReader: MavenXpp3Reader) {
    // Iterate through all POMs in order from our custom POM configuration
    project
      .configurations
      .getByName(pomConfiguration)
      .resolvedConfiguration
      .lenientConfiguration
      .artifacts.forEach { resolvedArtifact ->

        // Skip artifact processing for non-pom type artifacts
        if (resolvedArtifact.type != "pom") {
          return@forEach
        }

        // POM of artifact
        val pomFile = resolvedArtifact.file
        val model = mavenReader.read(FileReader(pomFile), false)

        // License information
        val name = getName(model).trim()
        val description = model.description.orEmpty().trim()
        var version = model.version.orEmpty().trim()
        val developers = mutableListOf<Developer>()
        if (model.developers.orEmpty().isNotEmpty()) {
          model.developers.orEmpty().forEach { developer ->
            developers.add(
              Developer().apply {
                id = developer.name.orEmpty().trim()
              }
            )
          }
        }

        val url = model.url.orEmpty().trim()
        val inceptionYear = model.inceptionYear.orEmpty().trim()

        // Search for licenses
        var licenses = findLicenses(mavenReader, pomFile)
        if (licenses.isEmpty()) {
          logger.warn("$name dependency does not have a license.")
          licenses = mutableListOf()
        }

        // Search for version
        if (version.isEmpty()) {
          version = findVersion(mavenReader, pomFile)
        }

        // Store the information that we need
        val module = resolvedArtifact.moduleVersion.id
        val project = Model().apply {
          this.name = name
          this.description = description
          this.licenses = licenses
          this.url = url
          this.developers = developers
          this.inceptionYear = inceptionYear
          this.groupId = module.group.orEmpty().trim()
          this.artifactId = module.name.orEmpty().trim()
          this.version = version
        }

        projects.add(project)
      }

    // Sort POM information by name
    projects.sortBy { it.name.lowercase(Locale.getDefault()) }
  }

  private fun getResolvedArtifactsFromResolvedDependencies(
    resolvedDependencies: Set<ResolvedDependency>
  ): Set<ResolvedArtifact> {
    val resolvedArtifacts = hashSetOf<ResolvedArtifact>()
    resolvedDependencies.forEach { resolvedDependency ->
      try {
        when (resolvedDependency.moduleVersion) {
          /**
           * Attempting to getAllModuleArtifacts on a local library project will result
           * in AmbiguousVariantSelectionException as there are not enough criteria
           * to match a specific variant of the library project. Instead we skip the
           * the library project itself and enumerate its dependencies.
           */
          "unspecified" -> resolvedArtifacts.addAll(
            getResolvedArtifactsFromResolvedDependencies(resolvedDependency.children)
          )
          else -> resolvedArtifacts.addAll(resolvedDependency.allModuleArtifacts)
        }
      } catch (e: Exception) {
        logger.warn("Failed to process ${resolvedDependency.name}", e)
      }
    }
    return resolvedArtifacts
  }

  /** Use Parent POM information when individual dependency license information is missing. */
  private fun getParentPomFile(model: Model): File? {
    // Get parent POM information
    val parent = model.parent
    val groupId = parent?.groupId.orEmpty()
    val artifactId = parent?.artifactId.orEmpty()
    val version = parent?.version.orEmpty()
    val dependency = "$groupId:$artifactId:$version@pom"

    // Add dependency to temporary configuration
    val configurations = project.configurations
    configurations.create(tempPomConfiguration)
    configurations.getByName(tempPomConfiguration).dependencies.add(
      project.dependencies.add(tempPomConfiguration, dependency)
    )

    val pomFile = project.configurations.getByName(tempPomConfiguration)
      .resolvedConfiguration.lenientConfiguration.artifacts.firstOrNull()?.file

    // Reset dependencies in temporary configuration
    project.configurations.remove(project.configurations.getByName(tempPomConfiguration))

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
      "Wrote ${newReport.name()} report to ${ConsoleRenderer().asClickableFileUrl(file)}."
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
        "Copied ${newReport.name()} report to ${ConsoleRenderer().asClickableFileUrl(licenseFile)}."
      )
    }
  }

  private fun isUrlValid(licenseUrl: String): Boolean {
    var uri: URI? = null
    try {
      uri = URL(licenseUrl).toURI()
    } catch (e: Exception) {
      logger.warn("$name dependency has an invalid license URL; skipping license", e)
    }
    return uri != null
  }

  private fun findVersion(mavenReader: MavenXpp3Reader, pomFile: File?): String {
    if (pomFile.isNullOrEmpty()) {
      return ""
    }
    val model = mavenReader.read(FileReader(pomFile!!), false)

    // If the POM is missing a name, do not record it
    val name = getName(model)
    if (name.isEmpty()) {
      logger.warn("POM file is missing a name: $pomFile")
      return ""
    }

    val version = model.version.orEmpty().trim()
    if (version.isNotEmpty()) {
      return version.trim()
    }

    if (model.parent.artifactId.orEmpty().trim().isNotEmpty()) {
      return findVersion(mavenReader, getParentPomFile(model))
    }
    return ""
  }

  private fun findLicenses(mavenReader: MavenXpp3Reader, pomFile: File?): List<License> {
    if (pomFile.isNullOrEmpty()) {
      return mutableListOf()
    }
    val model = mavenReader.read(FileReader(pomFile!!), false)

    // If the POM is missing a name, do not record it
    val name = getName(model)
    if (name.isEmpty()) {
      logger.warn("POM file is missing a name: $pomFile")
      return mutableListOf()
    }

    if (ANDROID_SUPPORT_GROUP_ID == model.groupId.orEmpty().trim()) {
      return listOf(
        License().apply {
          this.name = APACHE_LICENSE_NAME
          url = APACHE_LICENSE_URL
        }
      )
    }

    // License information found
    if (model.licenses.orEmpty().isNotEmpty()) {
      val licenses = mutableListOf<License>()
      model.licenses.orEmpty().forEach { license ->
        val licenseName = license.name.orEmpty().trim()
        val licenseUrl = license.url.orEmpty().trim()
        if (isUrlValid(licenseUrl)) {
          licenses.add(
            License().apply {
              this.name = licenseName
              url = licenseUrl
            }
          )
        }
      }
      return licenses
    }

    logger.info("Project, $name, has no license in POM file.")

    if (model.parent?.artifactId.orEmpty().trim().isNotEmpty()) {
      return findLicenses(mavenReader, getParentPomFile(model))
    }
    return mutableListOf()
  }

  private fun getName(model: Model): String {
    return model.name.orEmpty().trim().ifEmpty { model.artifactId.orEmpty().trim() }
  }

  private fun File?.isNullOrEmpty(): Boolean = this == null || this.length() == 0L

  private companion object {
    private const val ANDROID_SUPPORT_GROUP_ID = "com.android.support"
    private const val APACHE_LICENSE_NAME = "The Apache Software License"
    private const val APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    private const val OPEN_SOURCE_LICENSES = "open_source_licenses"
  }
}
