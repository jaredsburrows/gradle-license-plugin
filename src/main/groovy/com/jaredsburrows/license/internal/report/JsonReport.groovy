package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.pom.License
import groovy.json.JsonBuilder

final class JsonReport {
  private final static def PROJECT = "project"
  private final static def DESCRIPTION = "description"
  private final static def VERSION = "version"
  private final static def DEVELOPERS = "developers"
  private final static def URL = "url"
  private final static def YEAR = "year"
  private final static def LICENSES = "licenses"
  private final static def LICENSE = "license"
  private final static def LICENSE_URL = "license_url"
  private final static def EMPTY_JSON_ARRAY = "[]"
  private final List<Project> projects

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
