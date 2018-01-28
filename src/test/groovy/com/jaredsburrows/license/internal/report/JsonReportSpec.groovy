package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import spock.lang.Specification

final class JsonReportSpec extends Specification {
  def "no open source json"() {
    given:
    def projects = []
    def sut = new JsonReport(projects)

    when:
    def actual = sut.string().trim()
    def expected =
      """
[]
""".trim()

    then:
    actual == expected
  }

  def "open source json - missing values"() {
    given:
    def license = new License(name: "name", url: "url")
    def project = new Project(name: "name", licenses: [license], developers: [])
    def projects = [project, project]
    def sut = new JsonReport(projects)

    when:
    def actual = sut.string().trim()
    def expected =
      """
[
    {
        "project": "name",
        "description": null,
        "version": null,
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            {
                "license": "name",
                "license_url": "url"
            }
        ]
    },
    {
        "project": "name",
        "description": null,
        "version": null,
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            {
                "license": "name",
                "license_url": "url"
            }
        ]
    }
]
""".trim()

    then:
    actual == expected
  }

  def "open source json - all values"() {
    given:
    def developer = new Developer(name: "name")
    def developers = [developer, developer]
    def license = new License(name: "name", url: "url")
    def project = new Project(name: "name", description: "description", version: "1.0.0", licenses: [license], url: "url", developers: developers,
      year: "year")
    def projects = [project, project]
    def sut = new JsonReport(projects)

    when:
    def actual = sut.string().trim()
    def expected =
      """
[
    {
        "project": "name",
        "description": "description",
        "version": "1.0.0",
        "developers": [
            "name",
            "name"
        ],
        "url": "url",
        "year": "year",
        "licenses": [
            {
                "license": "name",
                "license_url": "url"
            }
        ]
    },
    {
        "project": "name",
        "description": "description",
        "version": "1.0.0",
        "developers": [
            "name",
            "name"
        ],
        "url": "url",
        "year": "year",
        "licenses": [
            {
                "license": "name",
                "license_url": "url"
            }
        ]
    }
]
""".trim()

    then:
    actual == expected
  }
}
