package com.jaredsburrows.license

import org.gradle.api.DefaultTask
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import java.io.File

/** A [org.gradle.api.Task] that is common for both Java and Android projects. */
open class BaseLicenseReportTask : DefaultTask() {

  // Task annotations cannot be internal
  @get:OutputDirectory var outputDir: File
  @Input var generateCsvReport = false
  @Input var generateHtmlReport = false
  @Input var generateJsonReport = false
  @Input var generateTextReport = false
  @Input var copyCsvReportToAssets = false
  @Input var copyHtmlReportToAssets = false
  @Input var copyJsonReportToAssets = false
  @Input var copyTextReportToAssets = false

  init {
    // From DefaultTask
    description = "Outputs licenses report for $name."
    group = "Reporting"

    // Customizing internal task options
    outputDir = project.extensions.getByType(ReportingExtension::class.java).file("licenses")

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
}
