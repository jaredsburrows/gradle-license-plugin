package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.pom.License
import groovy.json.JsonBuilder

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class JsonReport {
  final static PROJECT = "project"
  final static DESCRIPTION = "description"
  final static VERSION = "version"
  final static DEVELOPERS = "developers"
  final static URL = "url"
  final static YEAR = "year"
  final static LICENSES = "licenses"
  final static LICENSE = "license"
  final static LICENSE_URL = "license_url"
  final static EMPTY_JSON_ARRAY = "[]"
  final List<License> licenses
  final List<Project> projects

  JsonReport(projects) {
    this.projects = projects
  }

  /**
   * Json report when there are open source licenses.
   */
  @SuppressWarnings("GroovyGStringKey")
  def jsonArray() {
    new JsonBuilder(projects.collect { project ->
      def licensesJson = []
      project.licenses.each { license -> 
        licensesJson << [ "$LICENSE": license.name, "$LICENSE_URL": license.url]
      }
      [
        "$PROJECT"    : project.name ? project.name : null,
        "$DESCRIPTION": project.description ? project.description : null,
        "$VERSION"    : project.version ? project.version : null,
        "$DEVELOPERS" : project.developers*.name,
        "$URL"        : project.url ? project.url : null,
        "$YEAR"       : project.year ? project.year : null,
        "$LICENSES"   : licensesJson
      ]
    })
  }

  /**
   * Return Json as a String.
   */
  def string() {
    projects.empty ? EMPTY_JSON_ARRAY : jsonArray().toPrettyString()
  }
}
