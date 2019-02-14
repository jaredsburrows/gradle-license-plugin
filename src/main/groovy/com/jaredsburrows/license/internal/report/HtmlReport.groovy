package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import groovy.xml.MarkupBuilder

final class HtmlReport {
  final static def BODY_CSS = "body { font-family: sans-serif }"
  final static def PRE_CSS = "pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }"
  final static def CSS_STYLE = BODY_CSS + " " + PRE_CSS
  final static def OPEN_SOURCE_LIBRARIES = "Open source licenses"
  final static def NO_LIBRARIES = "None"
  final static def NO_LICENSE = "No license found"
  final static def NOTICE_LIBRARIES = "Notice for packages:"
  final List<Project> projects

  HtmlReport(def projects) {
    this.projects = projects
  }

  /**
   * Return Html as a String.
   */
  def string() {
    projects.empty ? noOpenSourceHtml() : openSourceHtml()
  }

  /**
   * Html report when there are open source licenses.
   */
  private def openSourceHtml() {
    final def writer = new StringWriter()
    final def markup = new MarkupBuilder(writer)
    final Map<String, List<Project>> projectsMap = new HashMap<>()
    def licenseMap = LicenseHelper.LICENSE_MAP

    // Store packages by license
    projects.each { project ->
      def key = ""

      // first check to see if the project's license is in our list of known licenses:
      if (project.licenses && project.licenses.size > 0) {
        def license = project.licenses[0]
        if (licenseMap.containsKey(license.url)) {
          // look up by url
          key = licenseMap[license.url]
        } else if (licenseMap.containsKey(license.name)) {
          // then by name
          key = licenseMap[license.name]
        } else {
          // otherwise, use the url as a key
          key = license.url
        }
      }

      if (!projectsMap.containsKey(key)) {
        projectsMap.put(key, [])
      }

      projectsMap.get(key).add(project)
    }

    markup.html {
      head {
        style(CSS_STYLE)
        title(OPEN_SOURCE_LIBRARIES)
      }

      body {
        h3(NOTICE_LIBRARIES)
        ul {

          projectsMap.entrySet().each { entry ->
            List<Project> sortedProjects = entry.value.sort {
              left, right -> left.name <=> right.name
            }

            def currentProject = null
            def currentLicense = null
            sortedProjects.each { project ->
              currentProject = project
              currentLicense = entry.key.hashCode()

              // Display libraries
              li {
                a(href: "#${currentLicense}", project.name)
              }
            }

            a(name: currentLicense)
            // Display associated license with libraries
            if (!currentProject.licenses || currentProject.licenses.size == 0) {
              pre(NO_LICENSE)
            } else if (!entry.key.empty && licenseMap.values().contains(entry.key)) {
              // license from license map
              pre(getLicenseText(entry.key))
            } else {
              // if not found in the map, just display the info from the POM.xml -  name along with the url
              def currentLicenseName = currentProject.licenses[0].name.trim()
              def currentUrl = currentProject.licenses[0].url.trim()
              if (currentLicenseName || currentUrl) {
                pre {
                  mkp.yield("$currentLicenseName\n")
                  mkp.yieldUnescaped("<a href='$currentUrl'>$currentUrl</a>")
                }
              } else {
                pre(NO_LICENSE)
              }
            }
          }

        }
      }
    }
    writer.toString()
  }

  /**
   * Html report when there are no open source licenses.
   */
  private static noOpenSourceHtml() {
    def writer = new StringWriter()
    def markup = new MarkupBuilder(writer)

    markup.html {
      head {
        style(CSS_STYLE)
        title(OPEN_SOURCE_LIBRARIES)
      }

      body {
        h3(NO_LIBRARIES)
      }
    }
    writer.toString()
  }

  private getLicenseText(def fileName) {
    getClass().getResource("/license/${fileName}").text
  }
}
