package com.jaredsburrows.license.internal.report.json

import com.jaredsburrows.license.internal.Project
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.gradle.internal.impldep.org.mortbay.util.ajax.JSON

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class JsonReport {
  final static def PROJECT = "project"
  final static def DEVELOPERS = "developers"
  final static def URL = "url"
  final static def YEAR = "year"
  final static def LICENSE = "license"
  final static def LICENSE_URL = "license_url"
  final List<Project> projects

  JsonReport(projects) {
    this.projects = projects
  }

  /**
   * Json report when there are open source licenses.
   */
  def jsonArray() {
    new JsonBuilder(
      projects.collect { project ->
        [
          "$PROJECT"    : project.name ? project.name : null,
          "$DEVELOPERS" : project.developers ? project.developers : null,
          "$URL"        : project.url ? project.url : null,
          "$YEAR"       : project.year ? project.year : null,
          "$LICENSE"    : project.license?.name ? project.license?.name : null,
          "$LICENSE_URL": project.license?.url ? project.license?.url : null
        ]
      }
    )
  }

  /**
   * Return Json as a String.
   */
  def string() {
    projects.empty ? "[]" : jsonArray().toPrettyString()
  }
}
