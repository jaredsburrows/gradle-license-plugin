package com.jaredsburrows.license

/** Configuration options for the gradle license plugin. */
open class LicenseReportExtension { // extensions can't be final
  /** Whether or not the Csv report should be generated. */
  var generateCsvReport = true

  /**
   * Whether or not the Csv report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the Csv report is disabled.
   */
  var copyCsvReportToAssets = true

  /** Whether or not the HTML report should be generated. */
  var generateHtmlReport = true

  /**
   * Whether or not the HTML report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the HTML report is disabled.
   */
  var copyHtmlReportToAssets = true

  /** Whether or not the JSON report should be generated. */
  var generateJsonReport = true

  /**
   * Whether or not the JSON report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the JSON report is disabled.
   */
  var copyJsonReportToAssets = false
}
