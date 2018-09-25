package com.jaredsburrows.license

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll
import test.BaseJavaSpecification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

final class LicensePluginJavaSpec extends BaseJavaSpecification {
  @Unroll def "java project running licenseReport using with gradle #gradleVersion"() {
    given:
    buildFile <<
      """
        plugins {
          id "java"
          id "com.jaredsburrows.license"
        }
      """.stripIndent().trim()

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion)
      .withProjectDir(testProjectDir.root)
      .withArguments("licenseReport")
      .withPluginClasspath()
      .build()

    then:
    result.task(":licenseReport").outcome == SUCCESS
    result.output.find("Wrote HTML report to file:///.*/build/reports/licenses/licenseReport.html.")
    result.output.find("Wrote JSON report to file:///.*/build/reports/licenses/licenseReport.json.")

    where:
    gradleVersion << [
      "4.4",
      "4.10"
    ]
  }
}
