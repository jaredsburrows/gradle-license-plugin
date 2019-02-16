package com.jaredsburrows.license;

/**
 * Configuration options for the gradle license plugin.
 */
public class LicenseReportExtension {
  /**
   * Whether or not the HTML report should be generated.
   */
  private boolean generateHtmlReport = true;

  /**
   * Whether or not the HTML report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the HTML report is disabled.
   */
  private boolean copyHtmlReportToAssets = true;

  /**
   * Whether or not the JSON report should be generated.
   */
  private boolean generateJsonReport = true;

  /**
   * Whether or not the HTML report should be copied to the Android assets directory. Ignored if the
   * project is not an Android project. Has no effect if the JSON report is disabled.
   */
  private boolean copyJsonReportToAssets = false;

  public boolean getGenerateHtmlReport() {
    return generateHtmlReport;
  }

  public boolean isGenerateHtmlReport() {
    return generateHtmlReport;
  }

  public void setGenerateHtmlReport(boolean generateHtmlReport) {
    this.generateHtmlReport = generateHtmlReport;
  }

  public boolean getCopyHtmlReportToAssets() {
    return copyHtmlReportToAssets;
  }

  public boolean isCopyHtmlReportToAssets() {
    return copyHtmlReportToAssets;
  }

  public void setCopyHtmlReportToAssets(boolean copyHtmlReportToAssets) {
    this.copyHtmlReportToAssets = copyHtmlReportToAssets;
  }

  public boolean getGenerateJsonReport() {
    return generateJsonReport;
  }

  public boolean isGenerateJsonReport() {
    return generateJsonReport;
  }

  public void setGenerateJsonReport(boolean generateJsonReport) {
    this.generateJsonReport = generateJsonReport;
  }

  public boolean getCopyJsonReportToAssets() {
    return copyJsonReportToAssets;
  }

  public boolean isCopyJsonReportToAssets() {
    return copyJsonReportToAssets;
  }

  public void setCopyJsonReportToAssets(boolean copyJsonReportToAssets) {
    this.copyJsonReportToAssets = copyJsonReportToAssets;
  }
}
