package com.jaredsburrows.license.internal

import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class ProjectSpec extends Specification {
  def license = new License.Builder().name("name").url("url").build()
  def sut = new Project.Builder().name("name").license(license).build()

  def "test name"() {
    expect:
    sut.name == "name"
    sut.getName() == "name"
  }

  def "test no name"() {
    when:
    sut = sut.newBuilder().name(null).build()

    then:
    !sut.name
  }

  def "test license"() {
    expect:
    sut.license == license
    sut.getLicense() == license
  }

  def "test no license"() {
    when:
    sut = sut.newBuilder().license(null).build()

    then:
    !sut.license
  }
}
