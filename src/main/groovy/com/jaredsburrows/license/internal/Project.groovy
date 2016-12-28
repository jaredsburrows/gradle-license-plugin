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
  final def url
  final def authors
  final def year

  Project() {
    this(new Builder())
  }

  Project(builder) {
    this.name = builder.name
    this.license = builder.license
    this.url = builder.url
    this.authors = builder.authors
    this.year = builder.year
  }

  Builder newBuilder() {
    new Builder(this)
  }

  /**
   * Build a new {@link Project}.
   */
  final static class Builder {
    def name
    def license
    def url
    def authors
    def year

    Builder() {
    }

    Builder(project) {
      this.name = project.name
      this.license = project.license
      this.url = project.url
      this.authors = project.authors
      this.year = project.year
    }

    /**
     * Sets the name of the {@link Project}.
     */
    Builder name(name) {
      this.name = name
      this
    }

    /**
     * Sets the {@link License} of the {@link Project}.
     */
    Builder license(license) {
      this.license = license
      this
    }

    /**
     * Sets the URL of the {@link Project}.
     */
    Builder url(url) {
      this.url = url
      this
    }

    /**
     * Sets the developers/authors of the {@link Project}.
     */
    Builder authors(authors) {
      this.authors = authors
      this
    }

    /**
     * Sets the inception year of the {@link Project}.
     */
    Builder year(year) {
      this.year = year
      this
    }

    Project build() {
      new Project(this)
    }
  }
}
