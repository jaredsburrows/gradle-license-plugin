package com.jaredsburrows.license.internal.report.json

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includePackage = false)
final class JsonReportObject {
  final static def PROJECT = "project"
  final static def AUTHORS = "authors"
  final static def URL = "url"
  final static def YEAR = "year"
  final static def LICENSE = "license"
  final static def LICENSE_URL = "license_url"
  def jsonObject = [:]
  def name
  def authors
  def url
  def year
  def license

  JsonReportObject() {
    this(new Builder())
  }

  JsonReportObject(builder) {
    this.name = builder.name
    this.authors = builder.authors
    this.url = builder.url
    this.year = builder.year
    this.license = builder.license
  }

  Builder newBuilder() {
    new Builder(this)
  }

  /**
   * Convert object to a JsonObject.
   */
  def jsonObject() {
    // Project name
    jsonObject.put(PROJECT, name)

    // Authors/developers
    if (authors) jsonObject.put(AUTHORS, authors)

    // Project url
    if (url) jsonObject.put(URL, url)

    // Inception year
    if (year) jsonObject.put(YEAR, year)

    // Project license
    if (license?.name) jsonObject.put(LICENSE, license.name)
    if (license?.url) jsonObject.put(LICENSE_URL, license.url)

    jsonObject
  }

  /**
   * Build a new {@link JsonReportObject}.
   */
  final static class Builder {
    def name
    def authors
    def url
    def year
    def license

    Builder() {
    }

    Builder(object) {
      this.name = object.name
      this.authors = object.authors
      this.url = object.url
      this.year = object.year
      this.license = object.license
    }

    /**
     * Sets the project name of the {@link JsonReportObject}.
     */
    Builder name(name) {
      this.name = name
      this
    }

    /**
     * Sets the project authors of the {@link JsonReportObject}.
     */
    Builder authors(authors) {
      this.authors = authors
      this
    }

    /**
     * Sets the project url of the {@link JsonReportObject}.
     */
    Builder url(url) {
      this.url = url
      this
    }

    /**
     * Sets the project inception year of the {@link JsonReportObject}.
     */
    Builder year(year) {
      this.year = year
      this
    }

    /**
     * Sets the project license of the {@link JsonReportObject}.
     */
    Builder license(license) {
      this.license = license
      this
    }

    JsonReportObject build() {
      new JsonReportObject(this)
    }
  }
}
