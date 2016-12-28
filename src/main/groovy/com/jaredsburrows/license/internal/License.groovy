package com.jaredsburrows.license.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
@EqualsAndHashCode(includes = "url", includeFields = true)
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

  /**
   * Build a new {@link License}.
   */
  final static class Builder {
    def name
    def url

    Builder() {
    }

    Builder(license) {
      this.name = license.name
      this.url = license.url
    }

    /**
     * Sets the name of the {@link License}.
     */
    Builder name(name) {
      this.name = name
      this
    }

    /**
     * Sets the URL of the {@link License}.
     */
    Builder url(url) {
      this.url = url
      this
    }

    License build() {
      new License(this)
    }
  }
}
