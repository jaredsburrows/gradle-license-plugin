package com.jaredsburrows.license

import spock.lang.Specification

final class LicenseReportExtensionSpec extends Specification {
  private LicenseReportExtension extension = new LicenseReportExtension()

  def 'the full JSON report is opt in'() {
    expect: 'the license texts make it large, so it is off until asked for'
    !extension.generateJsonFullReport
    !extension.copyJsonFullReportToAssets
  }

  def 'the existing report defaults are unchanged'() {
    expect:
    extension.generateCsvReport
    extension.generateHtmlReport
    extension.generateJsonReport
    extension.generateTextReport

    and: 'only the HTML report is copied to the assets by default'
    extension.copyHtmlReportToAssets
    !extension.copyCsvReportToAssets
    !extension.copyJsonReportToAssets
    !extension.copyTextReportToAssets

    and:
    !extension.useVariantSpecificAssetDirs
    !extension.showVersions
    extension.ignoredPatterns.isEmpty()
  }
}
