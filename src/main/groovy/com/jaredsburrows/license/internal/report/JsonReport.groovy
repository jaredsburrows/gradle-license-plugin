package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Project
import groovy.json.JsonBuilder

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class JsonReport {
  final static PROJECT = "project"
  final static DEVELOPERS = "developers"
  final static URL = "url"
  final static YEAR = "year"
  final static LICENSE = "license"
  final static LICENSE_URL = "license_url"
  final static EMPTY_JSON_ARRAY = "[]"
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
      [
        "$PROJECT"    : project.name ? project.name : null,
        "$DEVELOPERS" : project.developers ? project.developers.collect { developer -> developer?.name }?.join(", ") : null,
        "$URL"        : project.url ? project.url : null,
        "$YEAR"       : project.year ? project.year : null,
        "$LICENSE"    : project.license?.name ? project.license?.name : null,
        "$LICENSE_URL": project.license?.url ? project.license?.url : null
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
