package com.jaredsburrows.license

import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.report.JsonReport
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI

abstract class LicenseReportTaskKt : DefaultTask() {
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
//  @Optional @Internal var productFlavors = listOf<com.android.builder.model.ProductFlavor>()
  @OutputFile lateinit var htmlFile: File
  @OutputFile lateinit var jsonFile: File

  @TaskAction open fun licenseReport() {
    setupEnvironment()
    initDependencies()
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

  abstract fun initDependencies()

  abstract fun createHtmlReport()

  abstract fun generatePOMInfo()

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
        } catch (ignored: Exception) {
        }
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
