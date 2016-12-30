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
  def projects = []
  def license = new License.Builder().name("name").url("url").build()
  def project = new Project.Builder().name("name").license(license).url("url").authors("authors").year("year").build()
  def sut = new JsonReport(projects)

  def "setup"() {
    projects << project
  }

  def "test jsonArray"() {
    given:
    def jsonArray = sut.jsonArray()

    expect:
    jsonArray.size() == 1
    jsonArray[0].toString() == "[project:name, authors:authors, url:url, year:year, license:name, license_url:url]"
  }

  def "test toJson"() {
    given:
    def json = sut.toJson()
    def parse = new JsonSlurper().parseText(json)

    expect:
    parse[0]["project"] == "name"
    parse[0]["authors"] == "authors"
    parse[0]["url"] == "url"
    parse[0]["year"] == "year"
    parse[0]["license"] == "name"
    parse[0]["license_url"] == "url"
  }
}
