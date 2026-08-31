package test

import com.jaredsburrows.license.internal.report.HtmlReport
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import groovy.transform.CompileStatic
import javax.xml.parsers.DocumentBuilderFactory
import org.apache.commons.csv.CSVFormat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.util.DocumentBuilderFactoryConfigurer

@CompileStatic
final class TestUtils {
  private TestUtils() {
    //noinspection GroovyAccessibility
    throw new AssertionError('No instances')
  }

  static boolean assertCsv(String expected, String actual) {
    return csvRows(expected) == csvRows(actual)
  }

  /** The parsed rows, so a spec can compare two values rather than assert on a boolean. */
  static List<String> csvRows(String text) {
    return CSVFormat.DEFAULT.parse(new StringReader(text)).records.collect { it.toString() }
  }

  static boolean assertHtml(String expected, String actual) {
    String left = htmlToXml(expected)
    String right = htmlToXml(actual)
    return !DiffBuilder.compare(Input.fromString(right).build())
      .withTest(Input.fromString(left).build())
      // XMLUnit 2.12.0+ disallows DOCTYPE declarations by default, but the generated HTML reports start with one
      .withDocumentBuilderFactory(DocumentBuilderFactoryConfigurer.DefaultWithDTDParsing.configure(DocumentBuilderFactory.newInstance()))
      .normalizeWhitespace()
      .ignoreWhitespace()
      .build()
      .differences
  }

  static boolean assertJson(String expected, String actual) {
    return jsonOf(expected) == jsonOf(actual)
  }

  /** The parsed JSON, so a spec can compare two values rather than assert on a boolean. */
  static List<Map<String, Object>> jsonOf(String text) {
    Moshi moshi = new Moshi.Builder().build()
    JsonAdapter<List<Map<String, Object>>> jsonAdapter =
      moshi.adapter(Types.newParameterizedType(List, Map, String, Object))
    return jsonAdapter.fromJson(text)
  }

  static BuildResult gradleWithCommand(File file, String... commands) {
    return GradleRunner.create()
      .withProjectDir(file)
      .withArguments(commands)
      .withPluginClasspath()
      .build()
  }

  static BuildResult gradleWithCommandWithFail(File file, String... commands) {
    return GradleRunner.create()
      .withProjectDir(file)
      .withArguments(commands)
      .withPluginClasspath()
      .buildAndFail()
  }

  static String getLicenseText(String fileName) {
    return HtmlReport.getLicenseText(fileName)
  }

  private static String htmlToXml(String text) {
    // Convert HTML into legal-enough XML that we can use the XML comparison
    // utility to compare two HTML strings. This is only just what we need for
    // this exact case, so update as needed.
    String result = text
    result = result.replaceAll('<br>', '<br/>')
    result = result.replaceAll('<hr>', '<hr/>')
    result = result.replaceAll('&copy;', '(c)')
    result = result.replaceAll('<meta http-equiv="content-type" content="text/html; charset=utf-8">', '<meta http-equiv="content-type" content="text/html; charset=utf-8" />')
    // Unicode code points being transformed strangely - normalize
    result = result.replaceAll('Karol Wr.*niak', 'Karol WrXXniak')
    return result
  }
}
