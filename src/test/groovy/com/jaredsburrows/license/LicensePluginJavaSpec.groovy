package com.jaredsburrows.license

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll
import test.BaseJavaSpecification

final class LicensePluginJavaSpec extends BaseJavaSpecification {
  def "unsupported project project"() {
    given:
    def srdErrWriter = new StringWriter()
    buildFile << """
        plugins {
          id "com.jaredsburrows.license"
        }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withPluginClasspath(pluginClasspath)
      .forwardStdError(srdErrWriter)
      .buildAndFail()

    then:
    result.output.contains("License report plugin can only be applied to android or java projects.")
  }

  @Unroll def "java - #projectPlugin - all tasks created"() {
    given:
    buildFile << """
        plugins {
          id "${projectPlugin}"
          id "com.jaredsburrows.license"
        }
        """

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments("licenseReport")
      .withPluginClasspath(pluginClasspath)
      .build()

    then:
    result.output.contains("Wrote HTML report to ")
    result.output.contains("/build/reports/licenses/licenseReport.html")
    result.output.contains("Wrote JSON report to ")
    result.output.contains("/build/reports/licenses/licenseReport.json")
    result.task(":licenseReport").outcome == TaskOutcome.SUCCESS

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }
}
