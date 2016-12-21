package com.jaredsburrows.license.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includePackage = false)
final class Project {
  final def name
  final def license

  Project() {
    this(new Builder())
  }

  Project(builder) {
    this.name = builder.name
    this.license = builder.license
  }

  Builder newBuilder() {
    new Builder(this)
  }

  final static class Builder {
    def name
    def license

    Builder() {
    }

    Builder(project) {
      this.name = project.name
      this.license = project.license
    }

    Builder name(name) {
      this.name = name
      this
    }

    Builder license(license) {
      this.license = license
      this
    }

    Project build() {
      new Project(this)
    }
  }
}
