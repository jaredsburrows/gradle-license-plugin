package com.jaredsburrows.license

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll
import test.BaseJavaSpecification

import java.util.regex.Pattern

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

final class LicensePluginJavaSpec extends BaseJavaSpecification {
  def "unsupported project"() {
    given:
    buildFile <<
      """
        plugins {
            id "com.jaredsburrows.license"
        }
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withPluginClasspath()
      .buildAndFail()

    then:
    result.output.contains("License report plugin can only be applied to android or java projects.")
  }

  @Unroll def "java - #projectPlugin - all tasks created"() {
    given:
    buildFile <<
      """
        plugins {
            id "${projectPlugin}"
            id "com.jaredsburrows.license"
        }
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("licenseReport")
      .withPluginClasspath()
      .build()

    then:
    result.task(":licenseReport").outcome == SUCCESS
    Pattern.compile("Wrote HTML report to file:///.*/build/reports/licenses/licenseReport.html.").matcher(result.output).find()
    Pattern.compile("Wrote JSON report to file:///.*/build/reports/licenses/licenseReport.json.").matcher(result.output).find()

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }
}
