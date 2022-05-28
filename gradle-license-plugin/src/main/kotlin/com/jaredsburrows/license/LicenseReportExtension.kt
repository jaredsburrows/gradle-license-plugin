package com.jaredsburrows.license

/** Configuration options for the gradle license plugin. */
open class LicenseReportExtension { // extensions can't be final
  /** Whether or not the Csv report should be generated. */
  var generateCsvReport = true

  /** Whether or not the HTML report should be generated. */
  var generateHtmlReport = true

  /** Whether or not the JSON report should be generated. */
  var generateJsonReport = true

  /** Whether or not the Text report should be generated. */
  var generateTextReport = true

  /**
   * Whether or not the Csv report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the Csv report is disabled.
   */
  var copyCsvReportToAssets = false

  /**
   * Whether or not the HTML report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the HTML report is disabled.
   */
  var copyHtmlReportToAssets = true

  /**
   * Whether or not the JSON report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the JSON report is disabled.
   */
  var copyJsonReportToAssets = false

  /**
   * Wheither when copying reports to the Android assets directory, it uses the variant-specific
   * asset directory instead of main. Defaults to false.
   */
  var useVariantSpecificAssetDirs = false

  /**
   * Whether or not the Text report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the Text report is disabled.
   */
  var copyTextReportToAssets = false
}
