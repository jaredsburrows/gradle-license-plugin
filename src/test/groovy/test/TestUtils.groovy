package test

import com.google.gson.JsonParser
import com.jaredsburrows.license.internal.report.HtmlReport
import org.apache.commons.csv.CSVFormat
import org.gradle.testkit.runner.GradleRunner
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input

final class TestUtils {
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
    return HtmlReport.getLicenseText(fileName)
  }

  static def assertJson(def expected, def actual) {
    def parser = new JsonParser()
    return parser.parseString(actual).toString() == parser.parseString(expected).toString()
  }

  static def assertCsv(def expected, def actual) {
    def left = CSVFormat.DEFAULT.parse(new StringReader(actual)).records.collect { it.toString() }
    def right = CSVFormat.DEFAULT.parse(new StringReader(expected)).records.collect { it.toString() }
    return left == right
  }

  static def htmlToXml(String text) {
    // Convert HTML into legal-enough XML that we can use the XML comparison
    // utility to compare two HTML strings. This is only just what we need for
    // this exact case, so update as needed.
    text = text.replaceAll('<br>', '<br/>')
    text = text.replaceAll('<hr>', '<hr/>')
    text = text.replaceAll('&copy;', '(c)')
    // Unicode code points being transformed strangely - normalize
    text = text.replaceAll('Karol Wr.*niak', 'Karol WrXXniak')
    return text
  }

  static def assertHtml(def expected, def actual) {
    def left = htmlToXml(expected)
    def right = htmlToXml(actual)
    return !DiffBuilder.compare(Input.fromString(right).build())
      .withTest(Input.fromString(left).build())
      .normalizeWhitespace()
      .ignoreWhitespace()
      .build()
      .differences
  }
}
