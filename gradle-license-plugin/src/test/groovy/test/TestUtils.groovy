package test

import com.jaredsburrows.license.internal.report.HtmlReport
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.apache.commons.csv.CSVFormat
import org.gradle.testkit.runner.GradleRunner
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input

final class TestUtils {
  private TestUtils() {
    //noinspection GroovyAccessibility
    throw new AssertionError('No instances')
  }

  static def assertCsv(String expected, String actual) {
    def left = CSVFormat.DEFAULT.parse(new StringReader(actual)).records.collect { it.toString() }
    def right = CSVFormat.DEFAULT.parse(new StringReader(expected)).records.collect { it.toString() }
    return left == right
  }

  static def assertHtml(String expected, String actual) {
    def left = htmlToXml(expected)
    def right = htmlToXml(actual)
    return !DiffBuilder.compare(Input.fromString(right).build())
      .withTest(Input.fromString(left).build())
      .normalizeWhitespace()
      .ignoreWhitespace()
      .build()
      .differences
  }

  static def assertJson(String expected, String actual) {
    def moshi = new Moshi.Builder().build()
    def jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, Map.class, String.class, Object.class))
    return jsonAdapter.fromJson(expected) == jsonAdapter.fromJson(actual)
  }

  static def gradleWithCommand(File file, String... commands) {
    return GradleRunner.create()
      .withProjectDir(file)
      .withArguments(commands)
      .withPluginClasspath()
      .build()
  }

  static def gradleWithCommandWithFail(File file, String... commands) {
    return GradleRunner.create()
      .withProjectDir(file)
      .withArguments(commands)
      .withPluginClasspath()
      .buildAndFail()
  }

  static def getLicenseText(String fileName) {
    return HtmlReport.getLicenseText(fileName)
  }

  private static def htmlToXml(String text) {
    // Convert HTML into legal-enough XML that we can use the XML comparison
    // utility to compare two HTML strings. This is only just what we need for
    // this exact case, so update as needed.
    text = text.replaceAll('<br>', '<br/>')
    text = text.replaceAll('<hr>', '<hr/>')
    text = text.replaceAll('&copy;', '(c)')
    text = text.replaceAll('<meta http-equiv="content-type" content="text/html; charset=utf-8">', '<meta http-equiv="content-type" content="text/html; charset=utf-8" />')
    // Unicode code points being transformed strangely - normalize
    text = text.replaceAll('Karol Wr.*niak', 'Karol WrXXniak')
    return text
  }
}
