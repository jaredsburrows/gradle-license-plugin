package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.License
import com.jaredsburrows.license.internal.Project
import com.jaredsburrows.license.internal.report.json.JsonReport
import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class JsonReportSpec extends Specification {
  def license = License.builder()
    .name("name")
    .url("url")
    .build()
  def project = Project.builder()
    .name("name")
    .license(license)
    .url("url")
    .developers("developers")
    .year("year")
    .build()
  def projects = [project]
  def sut = new JsonReport(projects)

  def "test jsonArray"() {
    given:
    def jsonArray = sut.jsonArray()

    expect:
    jsonArray[0]["project"] == "name"
    jsonArray[0]["developers"] == "developers"
    jsonArray[0]["url"] == "url"
    jsonArray[0]["year"] == "year"
    jsonArray[0]["license"] == "name"
    jsonArray[0]["license_url"] == "url"
  }

  def "test toJson"() {
    given:
    def json = sut.toJson()
    def jsonArray = new JsonSlurper().parseText(json)

    expect:
    jsonArray[0]["project"] == "name"
    jsonArray[0]["developers"] == "developers"
    jsonArray[0]["url"] == "url"
    jsonArray[0]["year"] == "year"
    jsonArray[0]["license"] == "name"
    jsonArray[0]["license_url"] == "url"
  }
}
