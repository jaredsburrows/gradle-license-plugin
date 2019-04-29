package test

import com.google.gson.JsonParser
import org.gradle.testkit.runner.GradleRunner
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input

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

  static def getLicenseText(def fileName) {
    getClass().getResource("/license/${fileName}").text
  }

  static def assertJson(def expected, def actual) {
    return parser.parse(actual) == parser.parse(expected)
  }

  static def assertHtml(def expected, def actual) {
    return !DiffBuilder.compare(Input.fromString(actual).build())
      .withTest(Input.fromString(expected).build())
      .normalizeWhitespace()
      .ignoreWhitespace()
      .build()
      .differences
  }
}
