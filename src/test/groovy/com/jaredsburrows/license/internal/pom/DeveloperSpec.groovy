package com.jaredsburrows.license.internal.pom

import spock.lang.Specification

final class DeveloperSpec extends Specification {
  private def sut = new Developer(
    name: 'name'
  )

  def 'name'() {
    expect:
    sut.name == 'name'
  }
}
