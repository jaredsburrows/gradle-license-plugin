package com.jaredsburrows.license

import com.jaredsburrows.license.internal.ConsoleRenderer
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

/** A [org.gradle.api.Task] that Android portion of license reports */
internal open class AndroidLicenseReportTask : JavaLicenseReportTask() { // tasks can't be final

  @Input var assetDirs = emptyList<File>()

  @Optional @Input
  var variantName: String? = null

  @TaskAction
  override fun licenseReport() {
    setupEnvironment(name = variantName.orEmpty())
    initDependencies()
    initAndroidDependencies()
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
  }

  private fun initAndroidDependencies() {
    // If Android project, add extra configurations
    variantName?.let { variant ->
      project.configurations.find { it.name == "${variant}RuntimeClasspath" }?.also {
        configurationSet.add(it)
      }
    }
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
}
