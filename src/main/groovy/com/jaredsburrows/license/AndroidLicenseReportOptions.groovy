package com.jaredsburrows.license

/**
 * Configuration options for the gradle license plugin. This configuration should only be applied to Android projects.
 *
 * @author <a href="mailto:matthew.tamlin@icloud.com">Matthew Tamlin</a>
 */
class AndroidLicenseReportOptions {
  /**
   * Whether or not the HTML report should be generated.
   */
  boolean generateHtmlReport = true

  /**
   * Whether or not the HTML report should be copied to assets. Has no effect if the report is disabled.
   */
  boolean copyHtmlReportToAssets = true

  /**
   * Whether or not the JSON report should be generated.
   */
  boolean generateJsonReport = true

  /**
   * Whether or not the JSON report should be copied to assets. Has no effect if the report is disabled.
   */
  boolean copyJsonReportToAssets = false
}
