package com.jaredsburrows.license

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll
import test.BaseJavaSpecification
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

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }
}
