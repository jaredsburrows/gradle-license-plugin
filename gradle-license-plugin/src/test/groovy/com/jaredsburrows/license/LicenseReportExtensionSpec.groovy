package com.jaredsburrows.license

import groovy.transform.TypeChecked
import spock.lang.Specification

@TypeChecked
final class LicenseReportExtensionSpec extends Specification {
  def 'the full JSON report is opt in'() {
    given:
    LicenseReportExtension extension = new LicenseReportExtension()
    boolean generateJsonFullReport = extension.generateJsonFullReport
    boolean copyJsonFullReportToAssets = extension.copyJsonFullReportToAssets

    expect: 'the license texts make it large, so it is off until asked for'
    !generateJsonFullReport
    !copyJsonFullReportToAssets
  }

  def 'the existing report defaults are unchanged'() {
    given:
    LicenseReportExtension extension = new LicenseReportExtension()
    boolean generateCsvReport = extension.generateCsvReport
    boolean generateHtmlReport = extension.generateHtmlReport
    boolean generateJsonReport = extension.generateJsonReport
    boolean generateTextReport = extension.generateTextReport
    boolean copyHtmlReportToAssets = extension.copyHtmlReportToAssets
    boolean copyCsvReportToAssets = extension.copyCsvReportToAssets
    boolean copyJsonReportToAssets = extension.copyJsonReportToAssets
    boolean copyTextReportToAssets = extension.copyTextReportToAssets
    boolean useVariantSpecificAssetDirs = extension.useVariantSpecificAssetDirs
    boolean showVersions = extension.showVersions
    boolean noIgnoredPatterns = extension.ignoredPatterns.isEmpty()

    expect:
    generateCsvReport
    generateHtmlReport
    generateJsonReport
    generateTextReport

    and: 'only the HTML report is copied to the assets by default'
    copyHtmlReportToAssets
    !copyCsvReportToAssets
    !copyJsonReportToAssets
    !copyTextReportToAssets

    and: 'each variant copies into its own asset directory, so they cannot overwrite each other'
    useVariantSpecificAssetDirs

    and:
    !showVersions
    noIgnoredPatterns
  }
}
