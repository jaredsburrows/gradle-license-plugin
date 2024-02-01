package com.jaredsburrows.license

import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension

/** Returns true if plugin exists in project. */
internal fun Project.hasPlugin(list: List<String>): Boolean = list.any { plugins.hasPlugin(it) }

/** Configure common configuration for both Java and Android tasks. */
internal fun Project.configureCommon(task: LicenseReportTask) {
  val reportingExtension = extensions.getByType(ReportingExtension::class.java)
  val licenseExtension = extensions.getByType(LicenseReportExtension::class.java)

  task.apply {
    buildFile = this@configureCommon.buildFile
    outputDir = reportingExtension.file("licenses")

    generateCsvReport = licenseExtension.generateCsvReport
    generateHtmlReport = licenseExtension.generateHtmlReport
    generateJsonReport = licenseExtension.generateJsonReport
    generateTextReport = licenseExtension.generateTextReport
    copyCsvReportToAssets = licenseExtension.copyCsvReportToAssets
    copyHtmlReportToAssets = licenseExtension.copyHtmlReportToAssets
    copyJsonReportToAssets = licenseExtension.copyJsonReportToAssets
    copyTextReportToAssets = licenseExtension.copyTextReportToAssets
    useVariantSpecificAssetDirs = licenseExtension.useVariantSpecificAssetDirs
    ignoredPatterns = licenseExtension.ignoredPatterns
  }
}
