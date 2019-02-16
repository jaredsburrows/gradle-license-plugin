package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
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
    List<Map<String, String>> reportList = new ArrayList<>()
    for (Project project : projects) {
      // Handle multiple licenses
      List<Map<String, String>> licensesJson = new ArrayList<>()
      for (License license : project.getLicenses()) {
        Map<String, String> licenseMap = new LinkedHashMap<>()
        licenseMap.put(LICENSE, license.getName())
        licenseMap.put(LICENSE_URL, license.getUrl())
        licensesJson.add(licenseMap)
      }

      // Handle multiple developer
      List<String> developerNames = new ArrayList<>()
      for (Developer developer: project.getDevelopers()) {
        developerNames.add(developer.getName())
      }

      // Build the report
      Map<String, String> report = new LinkedHashMap<>()
      report.put(PROJECT, isNotNullorEmpty(project.getName()) ? project.getName() : null)
      report.put(DESCRIPTION, isNotNullorEmpty(project.getDescription()) ? project.getDescription() : null)
      report.put(VERSION, isNotNullorEmpty(project.getVersion()) ? project.getVersion() : null)
      report.put(DEVELOPERS, developerNames)
      report.put(URL, isNotNullorEmpty(project.getUrl()) ? project.getUrl() : null)
      report.put(YEAR, isNotNullorEmpty(project.getYear()) ? project.getYear() : null)
      report.put(LICENSES, licensesJson)
      report.put(DEPENDENCY, project.getGav())
      reportList.add(report)
    }
    return new JsonBuilder(reportList).toPrettyString()
  }

  private static boolean isNotNullorEmpty(String input) {
    return input != null && input.length() > 0
  }
}
