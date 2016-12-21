package com.jaredsburrows.license.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
@EqualsAndHashCode(excludes = "name", includeFields = true)
@ToString(includeNames = true, includePackage = false)
final class License {
  final def name
  final def url

  License() {
    this(new Builder())
  }

  License(builder) {
    this.name = builder.name
    this.url = builder.url
  }

  Builder newBuilder() {
    new Builder(this)
  }

  final static class Builder {
    def name
    def url

    Builder() {
    }

    Builder(license) {
      this.name = license.name
      this.url = license.url
    }

    Builder name(name) {
      this.name = name
      this
    }

    Builder url(url) {
      this.url = url
      this
    }

    License build() {
      new License(this)
    }
  }
}
