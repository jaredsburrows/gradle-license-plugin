package com.jaredsburrows.license

import spock.lang.Unroll
import test.BaseJavaSpecification

final class LicenseReportTaskJavaSpec extends BaseJavaSpecification {
  @Unroll def "java - #projectPlugin licenseReport - no dependencies"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project)

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>None</h3>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }

  @Unroll def "java - #projectPlugin licenseReport - project dependencies"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project)
    project.dependencies {
      compile APPCOMPAT_V7
      compile project.project(":subproject")
    }

    subproject.apply plugin: "java-library"
    subproject.dependencies {
      compile DESIGN
    }

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#1288284111'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288284111'>Design</a>
      </li>
      <a name='1288284111' />
      <pre>${getLicenseText("apache-2.0.txt")}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
        "description": null,
        "version": "26.1.0",
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            {
                "license": "The Apache Software License",
                "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        ]
    },
    {
        "project": "Design",
        "description": null,
        "version": "26.1.0",
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            {
                "license": "The Apache Software License",
                "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        ]
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }

  @Unroll def "java - #projectPlugin licenseReport - no open source dependencies"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project)
    project.dependencies {
      compile FIREBASE_CORE
    }

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>None</h3>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }

  @Unroll def "java - #projectPlugin licenseReport"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project)
    project.dependencies {
      // Handles duplicates
      compile APPCOMPAT_V7
      compile APPCOMPAT_V7
      compile DESIGN
    }

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#1288284111'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288284111'>Design</a>
      </li>
      <a name='1288284111' />
      <pre>${getLicenseText("apache-2.0.txt")}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Appcompat-v7",
        "description": null,
        "version": "26.1.0",
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            {
                "license": "The Apache Software License",
                "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        ]
    },
    {
        "project": "Design",
        "description": null,
        "version": "26.1.0",
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            {
                "license": "The Apache Software License",
                "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        ]
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson

    where:
    projectPlugin << LicensePlugin.JVM_PLUGINS
  }

  def "java - dependency with full pom - project name, developers, url, year, bad license"() {
    given:
    project.apply plugin: "java-library"
    new LicensePlugin().apply(project)
    project.dependencies {
      compile FAKE_DEPENDENCY3
    }

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>None</h3>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "java - dependency with full pom - project name, developers, url, year, single license"() {
    given:
    project.apply plugin: "java-library"
    new LicensePlugin().apply(project)
    project.dependencies {
      compile FAKE_DEPENDENCY
    }

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#755498312'>Fake dependency name</a>
      </li>
      <pre>Some license
http://website.tld/</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Fake dependency name",
        "description": "Fake dependency description",
        "version": "1.0.0",
        "developers": [
            "name"
        ],
        "url": "https://github.com/user/repo",
        "year": "2017",
        "licenses": [
            {
                "license": "Some license",
                "license_url": "http://website.tld/"
            }
        ]
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "java - dependency with full pom - project name, multiple developers, url, year, multiple licenses"() {
    given:
    project.apply plugin: "java-library"
    new LicensePlugin().apply(project)
    project.dependencies {
      compile FAKE_DEPENDENCY2
    }

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#755498312'>Fake dependency name</a>
      </li>
      <pre>Some license
http://website.tld/</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Fake dependency name",
        "description": "Fake dependency description",
        "version": "1.0.0",
        "developers": [
            "name"
        ],
        "url": "https://github.com/user/repo",
        "year": "2017",
        "licenses": [
            {
                "license": "Some license",
                "license_url": "http://website.tld/"
            },
            {
                "license": "Some license",
                "license_url": "http://website.tld/"
            }
        ]
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "java - dependency without license information - check it's parent"() {
    given:
    project.apply plugin: "java-library"
    new LicensePlugin().apply(project)
    project.dependencies {
      compile CHILD_DEPENDENCY
      compile RETROFIT_DEPENDENCY
    }

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#755498312'>Fake dependency name</a>
      </li>
      <pre>Some license
http://website.tld/</pre>
      <li>
        <a href='#1288284111'>Retrofit</a>
      </li>
      <a name='1288284111' />
      <pre>${getLicenseText("apache-2.0.txt")}</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Fake dependency name",
        "description": "Fake dependency description",
        "version": null,
        "developers": [
            "name"
        ],
        "url": "https://github.com/user/repo",
        "year": "2017",
        "licenses": [
            {
                "license": "Some license",
                "license_url": "http://website.tld/"
            }
        ]
    },
    {
        "project": "Retrofit",
        "description": null,
        "version": null,
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            {
                "license": "Apache 2.0",
                "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        ]
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }
}
