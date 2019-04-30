package com.jaredsburrows.license

import com.android.builder.model.ProductFlavor
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.report.JsonReport
import groovy.util.Node
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
  protected fun initDependencies() {
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

  protected abstract fun getName(pomText: Node?): String

  protected abstract fun findLicenses(pomFile: File?): List<License>

  /**
   * Use Parent POM information when individual dependency license information is missing.
   */
  protected open fun getParentPomFile(pomText: Node?): File? {
    val pomFile = project.configurations.getByName(TEMP_POM_CONFIGURATION)
      .resolvedConfiguration.lenientConfiguration.artifacts.firstOrNull()?.file

    // Reset dependencies in temporary configuration
    project.configurations.remove(project.configurations.getByName(TEMP_POM_CONFIGURATION))

    return pomFile
  }

  /**
   * Generated HTML report.
   */
  protected abstract fun createHtmlReport()

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
      bufferedWriter().use { out ->
        out.write(JsonReport(projects).string())
      }
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
        licenseFile.bufferedWriter().use { out ->
          out.write(htmlFile.readText())
        }
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
        licenseFile.bufferedWriter().use { out ->
          out.write(jsonFile.readText())
        }
      }

      // Log output directory for user
      logger.log(LogLevel.LIFECYCLE, "Copied JSON report to ${getClickableFileUrl(licenseFile)}.")
    }
  }
}
