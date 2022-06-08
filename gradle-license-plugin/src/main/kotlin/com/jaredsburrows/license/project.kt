package com.jaredsburrows.license

import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension

/** Returns true if plugin exists in project. */
internal fun Project.hasPlugin(list: List<String>): Boolean {
  return list.find { plugins.hasPlugin(it) } != null
}

/** Configure common configuration for both Java and Android tasks. */
internal fun Project.configureCommon(task: LicenseReportTask) {
  task.buildFile = buildFile

  // Customizing internal task options
  task.outputDir = extensions.getByType(ReportingExtension::class.java).file("licenses")

  // Customizing internal task options from extension
  val extension = extensions.getByType(LicenseReportExtension::class.java)
  task.generateCsvReport = extension.generateCsvReport
  task.generateHtmlReport = extension.generateHtmlReport
  task.generateJsonReport = extension.generateJsonReport
  task.generateTextReport = extension.generateTextReport
  task.copyCsvReportToAssets = extension.copyCsvReportToAssets
  task.copyHtmlReportToAssets = extension.copyHtmlReportToAssets
  task.copyJsonReportToAssets = extension.copyJsonReportToAssets
  task.copyTextReportToAssets = extension.copyTextReportToAssets
  task.useVariantSpecificAssetDirs = extension.useVariantSpecificAssetDirs
  task.ignoredGroupIds = extension.ignoredGroupIds
}
