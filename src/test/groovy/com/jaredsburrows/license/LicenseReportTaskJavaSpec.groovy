package com.jaredsburrows.license

import test.BaseJavaSpecification

final class LicenseReportTaskJavaSpec extends BaseJavaSpecification {
  def "java project running licenseReport with no dependencies"() {
    given:
    project.apply plugin: "java"
    new LicensePlugin().apply(project)

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
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

  def "java project running licenseReport with project dependencies - multi java modules"() {
    given:
    project.apply plugin: "java"
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
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#314129783'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#314129783'>Design</a>
      </li>
      <a name='314129783' />
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
        ],
        "dependency": "com.android.support:appcompat-v7:26.1.0"
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
        ],
        "dependency": "com.android.support:design:26.1.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "java project running licenseReport with  no open source dependencies"() {
    given:
    project.apply plugin: "java"
    new LicensePlugin().apply(project)
    project.dependencies {
      compile FIREBASE_CORE
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#0'>Firebase-core</a>
      </li>
      <a name='0' />
      <pre>No license found</pre>
    </ul>
  </body>
</html>
""".stripIndent().trim()
    def actualJson = task.jsonFile.text.stripIndent().trim()
    def expectedJson =
      """
[
    {
        "project": "Firebase-core",
        "description": null,
        "version": "10.0.1",
        "developers": [
            
        ],
        "url": null,
        "year": null,
        "licenses": [
            
        ],
        "dependency": "com.google.firebase:firebase-core:10.0.1"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "java project running licenseReport with duplicate dependencies"() {
    given:
    project.apply plugin: "java"
    new LicensePlugin().apply(project)
    project.dependencies {
      // Handles duplicates
      compile APPCOMPAT_V7
      compile APPCOMPAT_V7
      compile DESIGN
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#314129783'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#314129783'>Design</a>
      </li>
      <a name='314129783' />
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
        ],
        "dependency": "com.android.support:appcompat-v7:26.1.0"
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
        ],
        "dependency": "com.android.support:design:26.1.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "java project running licenseReport with dependency with full pom with project name, developers, url, year, bad license"() {
    given:
    project.apply plugin: "java"
    new LicensePlugin().apply(project)
    project.dependencies {
      compile FAKE_DEPENDENCY3
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
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

  def "java project running licenseReport with dependency with full pom and project name, developers, url, year, single license"() {
    given:
    project.apply plugin: "java"
    new LicensePlugin().apply(project)
    project.dependencies {
      compile FAKE_DEPENDENCY
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#755498312'>Fake dependency name</a>
      </li>
      <a name='755498312' />
      <pre>Some license
<a href='http://website.tld/'>http://website.tld/</a></pre>
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
        ],
        "dependency": "group:name:1.0.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "java project with running licenseReport dependency with full pom - project name, multiple developers, url, year, multiple licenses"() {
    given:
    project.apply plugin: "java"
    new LicensePlugin().apply(project)
    project.dependencies {
      compile FAKE_DEPENDENCY2
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#755498312'>Fake dependency name</a>
      </li>
      <a name='755498312' />
      <pre>Some license
<a href='http://website.tld/'>http://website.tld/</a></pre>
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
        ],
        "dependency": "group:name2:1.0.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "java project running licenseReport with dependency without license information that check it's parent's pom"() {
    given:
    project.apply plugin: "java"
    new LicensePlugin().apply(project)
    project.dependencies {
      compile CHILD_DEPENDENCY
      compile RETROFIT_DEPENDENCY
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#314129783'>Retrofit</a>
      </li>
      <a name='314129783' />
      <pre>${getLicenseText("apache-2.0.txt")}</pre>
      <pre>Some license
<a href='http://website.tld/'>http://website.tld/</a></pre>
      <li>
        <a href='#755498312'>Fake dependency name</a>
      </li>
      <a name='755498312' />
      <pre>Some license
<a href='http://website.tld/'>http://website.tld/</a></pre>
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
        ],
        "dependency": "group:child:1.0.0"
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
        ],
        "dependency": "com.squareup.retrofit2:retrofit:2.3.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }

  def "java project running licenseReport using api and implementation configurations with multi java modules"() {
    given:
    project.apply plugin: "java-library"
    new LicensePlugin().apply(project)
    project.dependencies {
      api APPCOMPAT_V7
      implementation project.project(":subproject")
    }

    subproject.apply plugin: "java-library"
    subproject.dependencies {
      implementation DESIGN
    }

    when:
    project.evaluate()
    LicenseReportTask task = project.tasks.getByName("licenseReport")
    task.execute()

    def actualHtml = task.htmlFile.text.stripIndent().trim()
    def expectedHtml =
      """
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href='#314129783'>Appcompat-v7</a>
      </li>
      <li>
        <a href='#314129783'>Design</a>
      </li>
      <a name='314129783' />
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
        ],
        "dependency": "com.android.support:appcompat-v7:26.1.0"
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
        ],
        "dependency": "com.android.support:design:26.1.0"
    }
]
""".stripIndent().trim()

    then:
    actualHtml == expectedHtml
    actualJson == expectedJson
  }
}
