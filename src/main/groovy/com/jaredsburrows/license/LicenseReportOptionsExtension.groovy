package com.jaredsburrows.license

/**
 * Configuration options for the gradle license plugin.
 */
class LicenseReportOptionsExtension {
  /**
   * Whether or not the HTML report should be generated.
   */
  boolean generateHtmlReport = true

  /**
   * Whether or not the HTML report should be copied to the Android assets directory. Ignored if the project is not
   * an Android project. Has no effect if the HTML report is disabled.
   */
  boolean copyHtmlReportToAssets = true

  /**
   * Whether or not the JSON report should be generated.
   */
  boolean generateJsonReport = true

  /**
   * Whether or not the HTML report should be copied to the Android assets directory. Ignored if the project is not
   * an Android project. Has no effect if the JSON report is disabled.
   */
  boolean copyJsonReportToAssets = false
}
