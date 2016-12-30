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
  def license = License.builder().name("name").url("url").build()
  def project = Project.builder().name("name").license(license).url("url").authors("authors").year("year").build()
  def projects = [project]
  def sut = new JsonReport(projects)

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
