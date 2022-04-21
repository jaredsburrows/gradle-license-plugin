package com.jaredsburrows.license

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import java.io.File

/** A [org.gradle.api.Task] that is common for both Java and Android projects. */
open class BaseLicenseReportTask : DefaultTask() {

  // Task annotations cannot be internal
  @OutputFile var csvFile: File
  @OutputFile var htmlFile: File
  @OutputFile var jsonFile: File
  @OutputFile var textFile: File
  @Input var generateCsvReport = false
  @Input var generateHtmlReport = false
  @Input var generateJsonReport = false
  @Input var generateTextReport = false
  @Input var copyCsvReportToAssets = false
  @Input var copyHtmlReportToAssets = false
  @Input var copyJsonReportToAssets = false
  @Input var copyTextReportToAssets = false
  private var outputPath: String

  init {
    // From DefaultTask
    description = "Outputs licenses report for $name."
    group = "Reporting"

    // Customizing internal task options
    outputPath = "${project.buildDir}/reports/licenses/".replace('/', File.separatorChar)
    csvFile = File(outputPath, "$name$CSV_EXT")
    htmlFile = File(outputPath, "$name$HTML_EXT")
    jsonFile = File(outputPath, "$name$JSON_EXT")
    textFile = File(outputPath, "$name$TEXT_EXT")

    // Customizing internal task options from extension
    val extension = project.extensions.getByType(LicenseReportExtension::class.java)
    generateCsvReport = extension.generateCsvReport
    generateHtmlReport = extension.generateHtmlReport
    generateJsonReport = extension.generateJsonReport
    generateTextReport = extension.generateTextReport
    copyCsvReportToAssets = extension.copyCsvReportToAssets
    copyHtmlReportToAssets = extension.copyHtmlReportToAssets
    copyJsonReportToAssets = extension.copyJsonReportToAssets
    copyTextReportToAssets = extension.copyTextReportToAssets
  }

  internal companion object {
    const val CSV_EXT = ".csv"
    const val HTML_EXT = ".html"
    const val JSON_EXT = ".json"
    const val TEXT_EXT = ".txt"
  }
}
