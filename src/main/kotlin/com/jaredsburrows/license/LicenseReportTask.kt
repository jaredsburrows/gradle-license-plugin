package com.jaredsburrows.license

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.report.HtmlReport
import com.jaredsburrows.license.internal.report.JsonReport
import groovy.util.Node
import groovy.util.NodeList
import groovy.util.XmlParser
import groovy.xml.QName
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI

open class LicenseReportTask : DefaultTask() {
  companion object {
    private const val POM_CONFIGURATION = "poms"
    private const val TEMP_POM_CONFIGURATION = "tempPoms"
    private const val ANDROID_SUPPORT_GROUP_ID = "com.android.support"
    private const val APACHE_LICENSE_NAME = "The Apache Software License"
    private const val APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    private const val OPEN_SOURCE_LICENSES = "open_source_licenses"
    const val HTML_EXT = ".html"
    const val JSON_EXT = ".json"

    @JvmStatic fun getClickableFileUrl(file: File): String {
      return URI("file", "", file.toURI().path, null, null).toString()
    }
  }

  @Internal var projects = arrayListOf<Project>()
  @Optional @Input var assetDirs = arrayOf<File>()
  @Optional @Input var generateHtmlReport: Boolean = false
  @Optional @Input var generateJsonReport: Boolean = false
  @Optional @Input var copyHtmlReportToAssets: Boolean = false
  @Optional @Input var copyJsonReportToAssets: Boolean = false
  @Optional @Input var buildType: String? = null
  @Optional @Input var variant: String? = null
  @Optional @Internal var productFlavors = listOf<com.android.builder.model.ProductFlavor>()
  @OutputFile lateinit var htmlFile: File
  @OutputFile lateinit var jsonFile: File

  @TaskAction open fun licenseReport() {
    setupEnvironment()
    collectDependencies()
    generatePOMInfo()

    if (generateHtmlReport) {
      createHtmlReport()

      // If Android project and copy enabled, copy to asset directory
      if (!variant.isNullOrEmpty() && copyHtmlReportToAssets) {
        copyHtmlReport()
      }
    }

    if (generateJsonReport) {
      createJsonReport()

      // If Android project and copy enabled, copy to asset directory
      if (!variant.isNullOrEmpty() && copyJsonReportToAssets) {
        copyJsonReport()
      }
    }
  }

  /**
   * Setup configurations to collect dependencies.
   */
  open fun setupEnvironment() {
    // Create temporary configuration in order to store POM information
    project.configurations.apply {
      create(POM_CONFIGURATION)

      forEach { configuration ->
        try {
          configuration.isCanBeResolved = true
        } catch (ignore: Exception) {
        }
      }
    }
  }

  /**
   * Iterate through all configurations and collect dependencies.
   */
  open fun collectDependencies() {
    // Add POM information to our POM configuration
    val configurations = linkedSetOf<Configuration>()

    project.configurations.apply {
      // Add "compile" configuration older java and android gradle plugins
      find { configuration -> configuration.name == "compile" }?.let { configurations.add(getByName("compile")) }

      // Add "api" and "implementation" configurations for newer java-library and android gradle plugins
      find { configuration -> configuration.name == "api" }?.let { configurations.add(getByName("api")) }
      find { configuration -> configuration.name == "implementation" }?.let { configurations.add(getByName("implementation")) }

      // If Android project, add extra configurations
      variant?.let { variant ->
        // Add buildType configurations
        find { configuration -> configuration.name == "compile" }?.let { configurations.add(getByName("${buildType}Compile")) }
        find { configuration -> configuration.name == "api" }?.let { configurations.add(getByName("${buildType}Api")) }
        find { configuration -> configuration.name == "implementation" }?.let { configurations.add(getByName("${buildType}Implementation")) }

        // Add productFlavors configurations
        productFlavors.forEach { flavor ->
          // Works for productFlavors and productFlavors with dimensions
          if (variant.capitalize().contains(flavor.name.capitalize())) {
            find { configuration -> configuration.name == "compile" }?.let { configurations.add(getByName("${flavor.name}Compile")) }
            find { configuration -> configuration.name == "api" }?.let { configurations.add(getByName("${flavor.name}Api")) }
            find { configuration -> configuration.name == "implementation" }?.let { configurations.add(getByName("${flavor.name}Implementation")) }
          }
        }
      }
    }

    val moduleVersionIdentifiers = arrayListOf<ModuleVersionIdentifier>()
    // Iterate through all the configurations's dependencies
    configurations.forEach { configuration ->
      val canBeResolved = configuration.isCanBeResolved
      configuration.resolvedConfiguration.lenientConfiguration.artifacts.forEach { artifact ->
        if (canBeResolved) {
          moduleVersionIdentifiers.add(artifact.moduleVersion.id)
        }
      }
    }

    moduleVersionIdentifiers.forEach { moduleVersionIdentifier ->
      project.configurations.getByName(POM_CONFIGURATION).dependencies.add(
        project.dependencies.add(POM_CONFIGURATION, moduleVersionIdentifier)
      )
    }
  }

  /**
   * Get POM information from the dependency artifacts.
   */
  open fun generatePOMInfo() {
    // Iterate through all POMs in order from our custom POM configuration
    project.configurations.getByName(POM_CONFIGURATION)
      .resolvedConfiguration.lenientConfiguration.artifacts.forEach { artifact ->
      val pomFile = artifact.file
      val pomText = XmlParser().parse(pomFile)

      // License information
      var pomName = getName(pomText)
      var pomVersion = pomText.getAt(QName("version"))?.text()
      var pomDescription = pomText.getAt(QName("description"))?.text()
      var pomDevelopers = arrayListOf<Developer>()
      if (!pomText.getAt(QName("developers")).isNullOrEmpty()) {
        val devs = arrayListOf<Developer>()
        pomText.getAt(QName("developers")).getAt(QName("developer")).forEach { developer ->
          devs.add(Developer().apply {
            this.name = (developer as NodeList).getAt(QName("name"))?.text()?.trim()
          })
        }
        pomDevelopers = devs
      }

      var pomUrl = pomText.getAt(QName("url"))?.text()
      var pomYear = pomText.getAt(QName("inceptionYear"))?.text()

      // Clean up and format
      pomName = pomName?.capitalize()
      pomVersion = pomVersion?.trim()
      pomDescription = pomDescription?.trim()
      pomUrl = pomUrl?.trim()
      pomYear = pomYear?.trim()

      var licenses = findLicenses(pomFile)
      if (licenses.isNullOrEmpty()) {
        logger.log(LogLevel.WARN, "$pomName dependency does not have a license.")
        licenses = arrayListOf()
      }

      // Store the information that we need
      val project = Project().apply {
        this.name = pomName
        this.description = pomDescription
        this.version = pomVersion
        this.developers = pomDevelopers
        this.licenses = licenses
        this.url = pomUrl
        this.year = pomYear
//        this.gav = pom.owner
      }

      projects.add(project)
    }

    // Sort POM information by name
    projects.sortBy { project -> project.name }
  }

  open fun getName(pomText: Node?): String? {
    val name = if (!pomText?.getAt(QName("name"))?.text().isNullOrBlank()) {
      pomText?.getAt(QName("name"))?.text()
    } else {
      pomText?.getAt(QName("artifactId"))?.text()
    }
    return name?.trim()
  }

  open fun findLicenses(pomFile: File?): List<License>? {
    if (pomFile == null) {
      return null
    }
    val pomText = XmlParser().parse(pomFile)

    // If the POM is missing a name, do not record it
    val name = getName(pomText)
    if (name.isNullOrEmpty()) {
      logger.log(LogLevel.WARN, "POM file is missing a name: $pomFile")
      return null
    }

    if (ANDROID_SUPPORT_GROUP_ID == pomText.getAt(QName("groupId"))?.text()) {
      return listOf(License().apply {
        this.name = APACHE_LICENSE_NAME
        this.url = APACHE_LICENSE_URL
      })
    }

    // License information found
    if (!pomText.getAt(QName("licenses")).isNullOrEmpty()) {
      val licenses = arrayListOf<License>()
      pomText.getAt(QName("licenses")).getAt(QName("license")).forEach { license ->
        // TODO finish
//        val licenseName = license.geta
      }
      return licenses
    }
    logger.log(LogLevel.INFO, "Project, $name, has no license in POM file.")

    val hasParent = pomText.parent() != null
    if (hasParent) {
      val parentPomFile = getParentPomFile(pomText)
      return findLicenses(parentPomFile)
    }
    return null
  }

  /**
   * Use Parent POM information when individual dependency license information is missing.
   */
  open fun getParentPomFile(pomText: Node?): File {
    // Get parent POM information
    val groupId = pomText?.parent()?.getAt(QName("groupId"))?.text()
    val artifactId = pomText?.parent()?.getAt(QName("artifactId"))?.text()
    val version = pomText?.parent()?.getAt(QName("version"))?.text()
    val dependency = "$groupId:$artifactId:$version@pom"

    // Add dependency to temporary configuration
    project.configurations.apply {
      create(TEMP_POM_CONFIGURATION)
      getByName(TEMP_POM_CONFIGURATION).dependencies.add(
        project.dependencies.add(TEMP_POM_CONFIGURATION, dependency)
      )
    }

    val pomFile = project.configurations.getByName(TEMP_POM_CONFIGURATION)
      .resolvedConfiguration.lenientConfiguration.artifacts.first().file

    // Reset dependencies in temporary configuration
    project.configurations.remove(project.configurations.getByName(TEMP_POM_CONFIGURATION))

    return pomFile
  }

  /**
   * Generated HTML report.
   */
  open fun createHtmlReport() {
    htmlFile.apply {
      // Remove existing file
      delete()

      // Create directories
      parentFile.mkdirs()
      createNewFile()

      // Write report for file
      bufferedWriter().use { out ->
        out.write(HtmlReport(projects).string())
      }
    }
  }

  /**
   * Generated JSON report.
   */
  open fun createJsonReport() {
    jsonFile.apply {
      // Remove existing file
      delete()

      // Create directories
      parentFile.mkdirs()
      createNewFile()

      // Write report for file
      bufferedWriter().use { out ->
        out.write(JsonReport(projects).string())
      }
    }

    // Log output directory for user
    logger.log(LogLevel.LIFECYCLE, "Wrote JSON report to ${getClickableFileUrl(jsonFile)}.")
  }

  open fun copyHtmlReport() {
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
        licenseFile.bufferedWriter().use { out ->
          out.write(htmlFile.readText())
        }
      }
    }
  }

  open fun copyJsonReport() {
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
        licenseFile.bufferedWriter().use { out ->
          out.write(jsonFile.readText())
        }
      }
    }
  }
}
