package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.pom.License
import groovy.json.JsonBuilder

final class JsonReport {
  private static final String PROJECT = "project"
  private static final String DESCRIPTION = "description"
  private static final String VERSION = "version"
  private static final String DEVELOPERS = "developers"
  private static final String URL = "url"
  private static final String YEAR = "year"
  private static final String LICENSES = "licenses"
  private static final String LICENSE = "license"
  private static final String LICENSE_URL = "license_url"
  private static final String EMPTY_JSON_ARRAY = "[]"
  private static final String DEPENDENCY = "dependency"
  private final List<Project> projects

  public JsonReport(List<Project> projects) {
    this.projects = projects
  }

  /**
   * Return Json as a String.
   */
  public String string() {
    return projects.isEmpty() ? EMPTY_JSON_ARRAY : jsonArray()
  }

  /**
   * Json report when there are open source licenses.
   */
  private String jsonArray() {
    return new JsonBuilder(projects.collect { project ->
      List licensesJson = []
      project.getLicenses().each { license ->
        licensesJson << [ "$LICENSE": license.getName(), "$LICENSE_URL": license.getUrl()]
      }
      [
        "$PROJECT"    : project.getName() ? project.getName() : null,
        "$DESCRIPTION": project.getDescription() ? project.description : null,
        "$VERSION"    : project.getVersion() ? project.version : null,
        "$DEVELOPERS" : project.getDevelopers()*.getName(),
        "$URL"        : project.getUrl() ? project.getUrl() : null,
        "$YEAR"       : project.getYear() ? project.getYear() : null,
        "$LICENSES"   : licensesJson,
        "$DEPENDENCY" : project.getGav()
      ]
    }).toPrettyString()
  }
}
