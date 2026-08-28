package com.jaredsburrows.license

/** Configuration options for the gradle license plugin. */
open class LicenseReportExtension {
  /** Whether the Csv report should be generated. */
  var generateCsvReport = true

  /** Whether the HTML report should be generated. */
  var generateHtmlReport = true

  /** Whether the JSON report should be generated. */
  var generateJsonReport = true

  /**
   * Whether the full JSON report should be generated. This is the JSON report plus the full text
   * of every known license, for applications that render their own license screen. Disabled by
   * default since the license texts make the report considerably larger.
   */
  var generateJsonFullReport = false

  /** Whether the Text report should be generated. */
  var generateTextReport = true

  /**
   * Whether the Csv report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the Csv report is disabled.
   */
  var copyCsvReportToAssets = false

  /**
   * Whether the HTML report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the HTML report is disabled.
   */
  var copyHtmlReportToAssets = true

  /**
   * Whether the JSON report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the JSON report is disabled.
   */
  var copyJsonReportToAssets = false

  /**
   * Whether the full JSON report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the full JSON report is disabled.
   */
  var copyJsonFullReportToAssets = false

  /**
   * Whether when copying reports to the Android assets directory, it uses the variant-specific
   * asset directory instead of main. Defaults to false.
   */
  var useVariantSpecificAssetDirs = false

  /**
   * Whether the Text report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the Text report is disabled.
   */
  var copyTextReportToAssets = false

  /** Set of patterns to ignore while generating the reports. Empty by default. */
  var ignoredPatterns = setOf<String>()

  /** Whether to show the licenses in the reports (As of now, only affects HTML since it is used in the app itself). */
  var showVersions = false
}
