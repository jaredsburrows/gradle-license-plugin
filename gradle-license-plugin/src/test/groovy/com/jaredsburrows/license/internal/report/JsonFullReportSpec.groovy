package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import groovy.json.JsonSlurper
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import spock.lang.Specification
import spock.lang.Unroll

final class JsonFullReportSpec extends Specification {
  private static final String APACHE_URL = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
  private static final String MIT_NAME = 'The MIT License'

  def 'the empty report still has both top level keys'() {
    when:
    JsonFullReport report = new JsonFullReport([])

    then:
    report.name() == 'Full JSON'
    report.extension() == 'full.json'
    new JsonSlurper().parseText(report.toString()) == [license_texts: [:], dependencies: []]
  }

  def 'a license text is stored once no matter how many dependencies share it'() {
    given: 'three dependencies, two of them Apache'
    List<Model> projects = [
      project('First', 'first', [license('The Apache Software License', APACHE_URL)]),
      project('Second', 'second', [license('Apache License 2.0', 'https://www.apache.org/licenses/LICENSE-2.0')]),
      project('Third', 'third', [license(MIT_NAME, 'https://spdx.org/licenses/MIT.html')]),
    ]

    when:
    Map<String, Object> report = (Map<String, Object>) new JsonSlurper().parseText(new JsonFullReport(projects).toString())

    then: 'differently spelled Apache licenses coalesce onto one text'
    report.license_texts.keySet() == ['apache-2.0', 'mit'] as Set
    report.license_texts['apache-2.0'] == LicenseHelper.INSTANCE.licenseText('apache-2.0.txt')
    report.license_texts['mit'] == LicenseHelper.INSTANCE.licenseText('mit.txt')

    and: 'each dependency only points at it'
    report.dependencies*.licenses*.license_key.flatten() == ['apache-2.0', 'apache-2.0', 'mit']

    and: 'the text appears once in the serialized report, not once per dependency'
    new JsonFullReport(projects).toString().count('Version 2.0, January 2004') == 1
  }

  def 'the POM name and url are kept even when the license is not bundled'() {
    given:
    List<Model> projects = [project('Unknown', 'unknown', [license('Some license', 'http://website.tld/')])]

    when:
    Map<String, Object> report = (Map<String, Object>) new JsonSlurper().parseText(new JsonFullReport(projects).toString())

    then:
    report.license_texts.isEmpty()
    report.dependencies.size() == 1
    report.dependencies[0].licenses[0].license == 'Some license'
    report.dependencies[0].licenses[0].license_url == 'http://website.tld/'
    report.dependencies[0].licenses[0].license_key == null
  }

  def 'a dependency with several licenses references each of them'() {
    given:
    List<Model> projects = [
      project('Dual', 'dual', [license(MIT_NAME, 'https://spdx.org/licenses/MIT.html'), license('The Apache Software License', APACHE_URL)]),
    ]

    when:
    Map<String, Object> report = (Map<String, Object>) new JsonSlurper().parseText(new JsonFullReport(projects).toString())

    then:
    report.license_texts.keySet() == ['apache-2.0', 'mit'] as Set
    report.dependencies[0].licenses*.license_key == ['mit', 'apache-2.0']
  }

  def 'the dependency entries carry everything the JSON report carries'() {
    given:
    Model model = project('Full', 'full', [license(MIT_NAME, 'https://spdx.org/licenses/MIT.html')])
    model.description = 'A description'
    model.url = 'https://github.com/user/repo'
    model.inceptionYear = '2017'
    model.developers = [new Developer().tap { it.id = 'A Developer' }]

    when:
    Map<String, Object> report = (Map<String, Object>) new JsonSlurper().parseText(new JsonFullReport([model]).toString())
    Map<String, Object> dependency = report.dependencies[0]

    then:
    dependency.project == 'Full'
    dependency.description == 'A description'
    dependency.version == '1.0.0'
    dependency.developers == ['A Developer']
    dependency.url == 'https://github.com/user/repo'
    dependency.year == '2017'
    dependency.dependency == 'group:full:1.0.0'
  }

  def 'missing values are serialized as null rather than dropped'() {
    given:
    List<Model> projects = [project('Sparse', 'sparse', [license(MIT_NAME, 'https://spdx.org/licenses/MIT.html')])]

    when:
    String json = new JsonFullReport(projects).toString()

    then: 'consumers can rely on the keys being present, as in the JSON report'
    json.contains('"description":null')
    json.contains('"url":null')
    json.contains('"year":null')
  }

  /** Populated the way LicenseReportTask populates it, which never leaves a field null. */
  private static Model project(String name, String artifactId, List<License> licenses) {
    return new Model().tap {
      it.groupId = 'group'
      it.artifactId = artifactId
      it.version = '1.0.0'
      it.name = name
      it.description = ''
      it.url = ''
      it.inceptionYear = ''
      it.licenses = licenses
      it.developers = []
    }
  }

  private static License license(String name, String url) {
    return new License().tap {
      it.name = name
      it.url = url
    }
  }

  @Unroll
  def 'every bundled license reaches the full report under its own key - #spdxId'() {
    given: 'the key is the bundled file name without its extension'
    List<Model> projects = [project('P', 'p', [license(spdxId, '')])]

    when:
    Map<String, Object> report = (Map<String, Object>) new JsonSlurper().parseText(new JsonFullReport(projects).toString())

    then: 'the dependency points at the key, and the text is carried under it'
    report.dependencies[0].licenses[0].license_key == expectedKey
    report.license_texts[expectedKey] == LicenseHelper.INSTANCE.licenseText(expectedKey + '.txt')

    where:
    spdxId << LicenseHelper.INSTANCE.bundledFileNames().collect { it - '.txt' }
    expectedKey = spdxId
  }

}
