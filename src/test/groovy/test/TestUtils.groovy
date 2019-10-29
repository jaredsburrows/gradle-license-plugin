package test

import com.google.gson.JsonParser
import com.jaredsburrows.license.LicenseReportTask
import com.jaredsburrows.license.internal.report.HtmlReport
import org.gradle.testkit.runner.GradleRunner
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input

import static com.jaredsburrows.license.internal.report.HtmlReportKt.getLicenseText

final class TestUtils {
  private static def parser = new JsonParser()

  private TestUtils() {
    throw new AssertionError('No instances')
  }

  static def gradleWithCommand(def file, String... commands) {
    return GradleRunner.create()
      .withProjectDir(file)
      .withArguments(commands)
      .withPluginClasspath()
      .build()
  }
  static def myGetLicenseText(String fileName) {
    return getLicenseText(fileName)
  }

  static def assertJson(def expected, def actual) {
    return parser.parse(actual) == parser.parse(expected)
  }

  static def htmlToXml(String text) {
    // Convert HTML into legal-enough XML that we can use the XML comparison
    // utility to compare two HTML strings. This is only just what we need for
    // this exact case, so update as needed.
    text = text.replaceAll('<br>', '<br/>')
    text = text.replaceAll('<hr>', '<hr/>')
    text = text.replaceAll('&copy;', '(c)')
    return text
  }

  static def assertHtml(def expected, def actual) {
    expected = htmlToXml(expected)
    actual = htmlToXml(actual)
    return !DiffBuilder.compare(Input.fromString(actual).build())
      .withTest(Input.fromString(expected).build())
      .normalizeWhitespace()
      .ignoreWhitespace()
      .build()
      .differences
  }
}
