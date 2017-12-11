package com.jaredsburrows.license

import spock.lang.Unroll
import test.BaseJavaSpecification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicenseReportTaskJavaSpec extends BaseJavaSpecification {
  @Unroll def "java - #projectPlugin licenseReport - no dependencies"() {
    given:
    project.apply plugin: projectPlugin
    new LicensePlugin().apply(project)

    when:
    project.evaluate()
    def task = project.tasks.getByName("licenseReport") as LicenseReportTask
    task.execute()

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>No open source libraries</h3>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[]
""".trim()

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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#1288288048'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288288048'>Design</a>
      </li>
    </ul>
    <a name='1288288048' />
    <h3>The Apache Software License</h3>
    <pre>The Apache Software License, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
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
""".trim()

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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>No open source libraries</h3>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[]
""".trim()

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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#1288288048'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#1288288048'>Design</a>
      </li>
    </ul>
    <a name='1288288048' />
    <h3>The Apache Software License</h3>
    <pre>The Apache Software License, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
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

""".trim()

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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>No open source libraries</h3>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
    def expectedJson =
      """
[]
""".trim()

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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#755502249'>Fake dependency name</a>
      </li>
    </ul>
    <a name='755502249' />
    <h3>Some license</h3>
    <pre>Some license, http://website.tld/</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
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
""".trim()

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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#755502249'>Fake dependency name</a>
      </li>
    </ul>
    <a name='755502249' />
    <h3>Some license</h3>
    <pre>Some license, http://website.tld/</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
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
""".trim()

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

    def actualHtml = task.htmlFile.text.trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body{font-family: sans-serif} pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for libraries:</h3>
    <ul>
      <li>
        <a href='#755502249'>Fake dependency name</a>
      </li>
      <li>
        <a href='#1288288048'>Retrofit</a>
      </li>
    </ul>
    <a name='755502249' />
    <h3>Some license</h3>
    <pre>Some license, http://website.tld/</pre>
    <a name='1288288048' />
    <h3>Apache 2.0</h3>
    <pre>Apache 2.0, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
  </body>
</html>
""".trim()
    def actualJson = task.jsonFile.text.trim()
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
""".trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }
}
