package com.jaredsburrows.license

import com.android.builder.model.ProductFlavor
import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.report.HtmlReport
import com.jaredsburrows.license.internal.report.JsonReport
import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.QName
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.net.URL
import java.util.UUID

abstract class LicenseReportTaskKt : DefaultTask() {
  companion object {
    const val ANDROID_SUPPORT_GROUP_ID = "com.android.support"
    const val APACHE_LICENSE_NAME = "The Apache Software License"
    const val APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    const val OPEN_SOURCE_LICENSES = "open_source_licenses"
    const val HTML_EXT = ".html"
    const val JSON_EXT = ".json"

    @JvmStatic fun getClickableFileUrl(file: File): String =
      URI("file", "", file.toURI().path, null, null).toString()
  }

  @Internal var projects = arrayListOf<Project>()
  @Optional @Input var assetDirs = arrayOf<File>()
  @Optional @Input var generateHtmlReport: Boolean = false
  @Optional @Input var generateJsonReport: Boolean = false
  @Optional @Input var copyHtmlReportToAssets: Boolean = false
  @Optional @Input var copyJsonReportToAssets: Boolean = false
  @Optional @Input var buildType: String? = null
  @Optional @Input var variantName: String? = null
  @Optional @Internal var productFlavors = listOf<ProductFlavor>()
  @OutputFile lateinit var htmlFile: File
  @OutputFile lateinit var jsonFile: File
  var POM_CONFIGURATION = "poms"
  var TEMP_POM_CONFIGURATION = "tempPoms"

  @TaskAction fun licenseReport() {
    setupEnvironment()
    initDependencies()
    generatePOMInfo()

    if (generateHtmlReport) {
      createHtmlReport()

      // If Android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyHtmlReportToAssets) {
        copyHtmlReport()
      }
    }

    if (generateJsonReport) {
      createJsonReport()

      // If Android project and copy enabled, copy to asset directory
      if (!variantName.isNullOrEmpty() && copyJsonReportToAssets) {
        copyJsonReport()
      }
    }
  }

  /**
   * Iterate through all configurations and collect dependencies.
   */
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
      // Add buildType configurations
      configurations.find { it.name == "compile" }?.let {
        configurationSet.add(configurations.getByName("${buildType}Compile"))
      }
      configurations.find { it.name == "api" }?.let {
        configurationSet.add(configurations.getByName("${buildType}Api"))
      }
      configurations.find { it.name == "implementation" }?.let {
        configurationSet.add(configurations.getByName("${buildType}Implementation"))
      }

      // Add productFlavors configurations
      productFlavors.forEach { flavor ->
        // Works for productFlavors and productFlavors with dimensions
        if (variant.capitalize().contains(flavor.name.capitalize())) {
          configurations.find { it.name == "compile" }?.let {
            configurationSet.add(configurations.getByName("${flavor.name}Compile"))
          }
          configurations.find { it.name == "api" }?.let {
            configurationSet.add(configurations.getByName("${flavor.name}Api"))
          }
          configurations.find { it.name == "implementation" }?.let {
            configurationSet.add(configurations.getByName("${flavor.name}Implementation"))
          }
        }
      }
    }

    // Iterate through all the configurations's dependencies
    configurationSet.forEach { set ->
      if (set.isCanBeResolved) {
        set.resolvedConfiguration.lenientConfiguration.artifacts.forEach { artifact ->
          val id = artifact.moduleVersion.id
          val gav = "${id.group}:${id.name}:${id.version}@pom"
          configurations.getByName(POM_CONFIGURATION).dependencies.add(
            project.dependencies.add(POM_CONFIGURATION, gav)
          )
        }
      }
    }
  }

  protected abstract fun generatePOMInfo()

  /**
   * Setup configurations to collect dependencies.
   */
  private fun setupEnvironment() {
    POM_CONFIGURATION += variantName.orEmpty() + UUID.randomUUID()
    TEMP_POM_CONFIGURATION += variantName.orEmpty() + UUID.randomUUID()

    // Create temporary configuration in order to store POM information
    project.configurations.apply {
      create(POM_CONFIGURATION)

      forEach { configuration ->
        try {
          configuration.isCanBeResolved = true
        } catch (ignored: Exception) {
        }
      }
    }
  }

  /**
   * Use Parent POM information when individual dependency license information is missing.
   */
  protected open fun getParentPomFile(node: Node?): File? {
    // Get parent POM information
    val parent = node?.getAt("parent")
    val groupId = parent?.getAt("groupId")?.text().orEmpty()
    val artifactId = parent?.getAt("artifactId")?.text().orEmpty()
    val version = parent?.getAt("version")?.text().orEmpty()
    val dependency = "$groupId:$artifactId:$version@pom"

    // Add dependency to temporary configuration
    val configurations = project.configurations
    configurations.create(TEMP_POM_CONFIGURATION)
    configurations.getByName(TEMP_POM_CONFIGURATION).dependencies.add(
      project.dependencies.add(TEMP_POM_CONFIGURATION, dependency)
    )

    val pomFile = project.configurations.getByName(TEMP_POM_CONFIGURATION)
      .resolvedConfiguration.lenientConfiguration.artifacts.firstOrNull()?.file

    // Reset dependencies in temporary configuration
    project.configurations.remove(project.configurations.getByName(TEMP_POM_CONFIGURATION))

    return pomFile
  }

  /**
   * Generated HTML report.
   */
  private fun createHtmlReport() {
    // Remove existing file
    htmlFile.apply {
      // Remove existing file
      delete()

      // Create directories
      parentFile.mkdirs()
      createNewFile()

      // Write report for file
      bufferedWriter().use { it.write(HtmlReport(projects).string()) }
    }

    // Log output directory for user
    logger.log(LogLevel.LIFECYCLE, "Wrote HTML report to ${getClickableFileUrl(htmlFile)}.")
  }

  /**
   * Generated JSON report.
   */
  private fun createJsonReport() {
    jsonFile.apply {
      // Remove existing file
      delete()

      // Create directories
      parentFile.mkdirs()
      createNewFile()

      // Write report for file
      bufferedWriter().use { it.write(JsonReport(projects).string()) }
    }

    // Log output directory for user
    logger.log(LogLevel.LIFECYCLE, "Wrote JSON report to ${getClickableFileUrl(jsonFile)}.")
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
        licenseFile.bufferedWriter().use { it.write(htmlFile.readText()) }
      }

      // Log output directory for user
      logger.log(LogLevel.LIFECYCLE, "Copied HTML report to ${getClickableFileUrl(licenseFile)}.")
    }
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
        licenseFile.bufferedWriter().use { it.write(jsonFile.readText()) }
      }

      // Log output directory for user
      logger.log(LogLevel.LIFECYCLE, "Copied JSON report to ${getClickableFileUrl(licenseFile)}.")
    }
  }

  protected fun isUrlValid(licenseUrl: String): Boolean {
    var url: URL? = null
    try {
      url = URL(licenseUrl)
    } catch (ignored: Exception) {
      logger.log(LogLevel.WARN, "$name dependency has an invalid license URL; skipping license")
    }
    return url != null
  }
}

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
