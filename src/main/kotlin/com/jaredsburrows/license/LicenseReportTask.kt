package com.jaredsburrows.license

import com.jaredsburrows.license.internal.ConsoleRenderer
import com.jaredsburrows.license.internal.report.CsvReport
import com.jaredsburrows.license.internal.report.HtmlReport
import com.jaredsburrows.license.internal.report.JsonReport
import com.jaredsburrows.license.internal.report.TextReport
import groovy.namespace.QName
import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.net.URL
import java.util.Locale
import java.util.UUID

/** A [org.gradle.api.Task] that creates HTML and JSON reports of the current projects dependencies. */
internal open class LicenseReportTask : BaseLicenseReportTask() { // tasks can't be final

  @Internal var projects = mutableListOf<Model>()
  @Input var assetDirs = emptyList<File>()

  @Optional @Input
  var variantName: String? = null
  private var pomConfiguration = "poms"
  private var tempPomConfiguration = "tempPoms"

  /**
   * Use a non-static parser instance to avoid errors with concurrent licenseReport tasks
   * in multi-project setups. See https://github.com/jaredsburrows/gradle-license-plugin/pull/191
   * for additional details.
   */
  private var xmlParser = XmlParser(false, false)

  @TaskAction fun licenseReport() {
    setupEnvironment()
    initDependencies()
    generatePOMInfo()

    if (generateCsvReport) {
      createCsvReport()

      // If android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyCsvReportToAssets) {
        copyCsvReport()
      }
    }

    if (generateHtmlReport) {
      createHtmlReport()

      // If android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyHtmlReportToAssets) {
        copyHtmlReport()
      }
    }

    if (generateJsonReport) {
      createJsonReport()

      // If android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyJsonReportToAssets) {
        copyJsonReport()
      }
    }

    if (generateTextReport) {
      createTextReport()

      // If android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyTextReportToAssets) {
        copyTextReport()
      }
    }
  }

  /** Iterate through all configurations and collect dependencies. */
  private fun initDependencies() {
    // Add POM information to our POM configuration
    val configurationSet = linkedSetOf<Configuration>()
    val configurations = project.configurations

    // Add "compile" configuration older java and android gradle plugins
    configurations.find { it.name == "compile" }?.let {
      configurationSet.add(configurations.getByName("compile"))
    }

    // Add "api" and "implementation" configurations for newer java-library and android gradle plugins
    configurations.find { it.name == "api" }?.let {
      configurationSet.add(configurations.getByName("api"))
    }

    configurations.find { it.name == "implementation" }?.let {
      configurationSet.add(configurations.getByName("implementation"))
    }

    // If Android project, add extra configurations
    variantName?.let { variant ->
      configurations.find { it.name == "${variant}RuntimeClasspath" }?.also {
        configurationSet.add(it)
      }
    }

    // Iterate through all the configuration's dependencies
    configurationSet.forEach { configuration ->
      if (configuration.isCanBeResolved) {
        val allDeps = configuration.resolvedConfiguration.lenientConfiguration.allModuleDependencies
        getResolvedArtifactsFromResolvedDependencies(allDeps).forEach { artifact ->
          val id = artifact.moduleVersion.id
          val gav = "${id.group}:${id.name}:${id.version}@pom"
          configurations.getByName(pomConfiguration).dependencies.add(
            project.dependencies.add(pomConfiguration, gav)
          )
        }
      }
    }
  }

  private fun getResolvedArtifactsFromResolvedDependencies(
    resolvedDependencies: Set<ResolvedDependency>
  ): Set<ResolvedArtifact> {
    val resolvedArtifacts = hashSetOf<ResolvedArtifact>()
    for (resolvedDependency in resolvedDependencies) {
      try {
        if (resolvedDependency.moduleVersion == "unspecified") {
          /**
           * Attempting to getAllModuleArtifacts on a local library project will result
           * in AmbiguousVariantSelectionException as there are not enough criteria
           * to match a specific variant of the library project. Instead we skip the
           * the library project itself and enumerate its dependencies.
           */
          resolvedArtifacts.addAll(
            getResolvedArtifactsFromResolvedDependencies(resolvedDependency.children)
          )
        } else {
          resolvedArtifacts.addAll(resolvedDependency.allModuleArtifacts)
        }
      } catch (e: Exception) {
        logger.warn("Failed to process $resolvedDependency.name", e)
      }
    }
    return resolvedArtifacts
  }

  /** Get POM information from the dependency artifacts. */
  private fun generatePOMInfo() {
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
        val node = xmlParser.parse(pomFile)

        // License information
        val name = getName(node).trim()
        val description = node.getAt("description").text().trim()
        var version = node.getAt("version").text().trim()
        val developers = mutableListOf<Developer>()
        if (node.getAt("developers").isNotEmpty()) {
          node.getAt("developers").getAt("developer").forEach { developer ->
            developers.add(
              Developer().apply {
                id = (developer as Node).getAt("name").text().trim()
              }
            )
          }
        }

        val url = node.getAt("url").text().trim()
        val inceptionYear = node.getAt("inceptionYear").text().trim()

        // Search for licenses
        var licenses = findLicenses(pomFile)
        if (licenses.isEmpty()) {
          logger.log(LogLevel.WARN, "$name dependency does not have a license.")
          licenses = mutableListOf()
        }

        // Search for version
        if (version.isEmpty()) {
          version = findVersion(pomFile)
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
          this.groupId = module.group
          this.artifactId = module.name
          this.version = version
        }

        projects.add(project)
      }

    // Sort POM information by name
    projects.sortBy { it.name.lowercase(Locale.getDefault()) }
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
        } catch (ignored: Exception) {
        }
      }
    }
  }

  /** Use Parent POM information when individual dependency license information is missing. */
  protected open fun getParentPomFile(node: Node?): File? {
    // Get parent POM information
    val parent = node?.getAt("parent")
    val groupId = parent?.getAt("groupId")?.text().orEmpty()
    val artifactId = parent?.getAt("artifactId")?.text().orEmpty()
    val version = parent?.getAt("version")?.text().orEmpty()
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

  /** Generated HTML report. */
  private fun createCsvReport() {
    // Remove existing file
    csvFile.apply {
      // Remove existing file
      delete()

      // Create directories
      parentFile.mkdirs()
      createNewFile()

      // Write report for file
      bufferedWriter().use { it.write(CsvReport(projects).toString()) }
    }

    // Log output directory for user
    logger.log(
      LogLevel.LIFECYCLE,
      "Wrote CSV report to ${ConsoleRenderer().asClickableFileUrl(csvFile)}."
    )
  }

  private fun copyCsvReport() {
    // Iterate through all asset directories
    assetDirs.forEach { directory ->
      val licenseFile = File(directory.path, OPEN_SOURCE_LICENSES + CSV_EXT)

      licenseFile.apply {
        // Remove existing file
        delete()

        // Create new file
        parentFile.mkdirs()
        createNewFile()

        // Copy HTML file to the assets directory
        bufferedWriter().use { it.write(csvFile.readText()) }
      }

      // Log output directory for user
      logger.log(
        LogLevel.LIFECYCLE,
        "Copied CSV report to ${ConsoleRenderer().asClickableFileUrl(licenseFile)}."
      )
    }
  }

  /** Generated HTML report. */
  private fun createHtmlReport() {
    // Remove existing file
    htmlFile.apply {
      // Remove existing file
      delete()

      // Create directories
      parentFile.mkdirs()
      createNewFile()

      // Write report for file
      bufferedWriter().use { it.write(HtmlReport(projects).toString()) }
    }

    // Log output directory for user
    logger.log(
      LogLevel.LIFECYCLE,
      "Wrote HTML report to ${ConsoleRenderer().asClickableFileUrl(htmlFile)}."
    )
  }

  private fun copyHtmlReport() {
    // Iterate through all asset directories
    assetDirs.forEach { directory ->
      val licenseFile = File(directory.path, OPEN_SOURCE_LICENSES + HTML_EXT)

      licenseFile.apply {
        // Remove existing file
        delete()

        // Create new file
        parentFile.mkdirs()
        createNewFile()

        // Copy HTML file to the assets directory
        bufferedWriter().use { it.write(htmlFile.readText()) }
      }

      // Log output directory for user
      logger.log(
        LogLevel.LIFECYCLE,
        "Copied HTML report to ${ConsoleRenderer().asClickableFileUrl(licenseFile)}."
      )
    }
  }

  /** Generated JSON report. */
  private fun createJsonReport() {
    jsonFile.apply {
      // Remove existing file
      delete()

      // Create directories
      parentFile.mkdirs()
      createNewFile()

      // Write report for file
      bufferedWriter().use { it.write(JsonReport(projects).toString()) }
    }

    // Log output directory for user
    logger.log(
      LogLevel.LIFECYCLE,
      "Wrote JSON report to ${ConsoleRenderer().asClickableFileUrl(jsonFile)}."
    )
  }

  private fun copyJsonReport() {
    // Iterate through all asset directories
    assetDirs.forEach { directory ->
      val licenseFile = File(directory.path, OPEN_SOURCE_LICENSES + JSON_EXT)

      licenseFile.apply {
        // Remove existing file
        delete()

        // Create new file
        parentFile.mkdirs()
        createNewFile()

        // Copy JSON file to the assets directory
        bufferedWriter().use { it.write(jsonFile.readText()) }
      }

      // Log output directory for user
      logger.log(
        LogLevel.LIFECYCLE,
        "Copied JSON report to ${ConsoleRenderer().asClickableFileUrl(licenseFile)}."
      )
    }
  }

  /** Generated Text report. */
  private fun createTextReport() {
    // Remove existing file
    textFile.apply {
      // Remove existing file
      delete()

      // Create directories
      parentFile.mkdirs()
      createNewFile()

      // Write report for file
      bufferedWriter().use { it.write(TextReport(projects).toString()) }
    }

    // Log output directory for user
    logger.log(
      LogLevel.LIFECYCLE,
      "Wrote Text report to ${ConsoleRenderer().asClickableFileUrl(textFile)}."
    )
  }

  private fun copyTextReport() {
    // Iterate through all asset directories
    assetDirs.forEach { directory ->
      val licenseFile = File(directory.path, OPEN_SOURCE_LICENSES + TEXT_EXT)

      licenseFile.apply {
        // Remove existing file
        delete()

        // Create new file
        parentFile.mkdirs()
        createNewFile()

        // Copy HTML file to the assets directory
        bufferedWriter().use { it.write(textFile.readText()) }
      }

      // Log output directory for user
      logger.log(
        LogLevel.LIFECYCLE,
        "Copied Text report to ${ConsoleRenderer().asClickableFileUrl(licenseFile)}."
      )
    }
  }

  private fun isUrlValid(licenseUrl: String): Boolean {
    var uri: URI? = null
    try {
      uri = URL(licenseUrl).toURI()
    } catch (ignored: Exception) {
      logger.log(LogLevel.WARN, "$name dependency has an invalid license URL; skipping license")
    }
    return uri != null
  }

  private fun findVersion(pomFile: File?): String {
    if (pomFile.isNullOrEmpty()) {
      return ""
    }
    val node = xmlParser.parse(pomFile)

    // If the POM is missing a name, do not record it
    val name = getName(node)
    if (name.isEmpty()) {
      logger.log(LogLevel.WARN, "POM file is missing a name: $pomFile")
      return ""
    }

    if (node.getAt("version").isNotEmpty()) {
      return node.getAt("version").text().trim()
    }

    if (node.getAt("parent").isNotEmpty()) {
      return findVersion(getParentPomFile(node))
    }
    return ""
  }

  private fun findLicenses(pomFile: File?): List<License> {
    if (pomFile.isNullOrEmpty()) {
      return mutableListOf()
    }
    val node = xmlParser.parse(pomFile)

    // If the POM is missing a name, do not record it
    val name = getName(node)
    if (name.isEmpty()) {
      logger.log(LogLevel.WARN, "POM file is missing a name: $pomFile")
      return mutableListOf()
    }

    if (ANDROID_SUPPORT_GROUP_ID == node.getAt("groupId").text()) {
      return listOf(
        License().apply {
          this.name = APACHE_LICENSE_NAME
          url = APACHE_LICENSE_URL
        }
      )
    }

    // License information found
    if (node.getAt("licenses").isNotEmpty()) {
      val licenses = mutableListOf<License>()
      (node.getAt("licenses")[0] as Node).getAt("license").forEach { license ->
        val licenseName = (license as Node).getAt("name").text().trim()
        val licenseUrl = license.getAt("url").text().trim()
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

    logger.log(LogLevel.INFO, "Project, $name, has no license in POM file.")

    if (!node.getAt("parent").isEmpty()) {
      return findLicenses(getParentPomFile(node))
    }
    return mutableListOf()
  }

  private fun getName(node: Node): String {
    return node.getAt("name").text().trim().ifEmpty {
      node.getAt("artifactId").text()
    }.trim()
  }

  private fun File?.isNullOrEmpty(): Boolean = this == null || this.length() == 0L

  private fun Node.getAt(name: String): NodeList {
    val answer = NodeList()
    val var3 = this.children().iterator()

    while (var3.hasNext()) {
      val child = var3.next()
      if (child is Node) {
        val childNodeName = child.name()
        if (childNodeName is QName) {
          if (childNodeName.matches(name)) {
            answer.add(child)
          }
        } else if (name == childNodeName) {
          answer.add(child)
        }
      }
    }

    return answer
  }

  internal companion object {
    private const val ANDROID_SUPPORT_GROUP_ID = "com.android.support"
    private const val APACHE_LICENSE_NAME = "The Apache Software License"
    private const val APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    private const val OPEN_SOURCE_LICENSES = "open_source_licenses"
  }
}
