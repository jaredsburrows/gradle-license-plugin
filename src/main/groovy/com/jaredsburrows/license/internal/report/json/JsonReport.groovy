package com.jaredsburrows.license.internal.report.json

import groovy.json.JsonOutput

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class JsonReport {
  final def projects
  def jsonArray = []

  JsonReport(projects) {
    this.projects = projects
  }

  def jsonArray() {
    // Create new license object for each project
    projects.each { project ->
      final def jsonReportObject = new JsonReportObject.Builder()
        .name(project.name)
        .authors(project.authors)
        .url(project.url)
        .year(project.year)
        .license(project.license)
        .build()
        .jsonObject()

      jsonArray.add(jsonReportObject)
    }

    jsonArray
  }

  def toJson() {
    JsonOutput.toJson(jsonArray())
  }
}
