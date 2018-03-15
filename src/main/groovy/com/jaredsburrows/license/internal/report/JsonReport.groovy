package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.pom.License
import groovy.json.JsonBuilder

final class JsonReport {
  final static def PROJECT = "project"
  final static def DESCRIPTION = "description"
  final static def VERSION = "version"
  final static def DEVELOPERS = "developers"
  final static def URL = "url"
  final static def YEAR = "year"
  final static def LICENSES = "licenses"
  final static def LICENSE = "license"
  final static def LICENSE_URL = "license_url"
  final static def EMPTY_JSON_ARRAY = "[]"
  final List<License> licenses
  final List<Project> projects

  JsonReport(projects) {
    this.projects = projects
  }

  /**
   * Return Json as a String.
   */
  def string() {
    projects.empty ? EMPTY_JSON_ARRAY : jsonArray().toPrettyString()
  }

  /**
   * Json report when there are open source licenses.
   */
  @SuppressWarnings("GroovyGStringKey")
  private def jsonArray() {
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
}
