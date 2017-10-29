package com.jaredsburrows.license

import spock.lang.Unroll
import test.BaseJavaSpecification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicensePluginJavaSpec extends BaseJavaSpecification {
  def "unsupported project project"() {
    when:
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"

    then:
    def e = thrown(IllegalStateException)
    e.message == "License report plugin can only be applied to android or java projects."
  }

  @Unroll "java - #projectPlugin project"() {
    given:
    project.apply plugin: projectPlugin

    when:
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"

    then:
    noExceptionThrown()

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }

  @Unroll "java - #projectPlugin - all tasks created"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project) // project.apply plugin: "com.jaredsburrows.license"

    when:
    project.evaluate()

    then:
    project.tasks.getByName("licenseReport")

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }
}
