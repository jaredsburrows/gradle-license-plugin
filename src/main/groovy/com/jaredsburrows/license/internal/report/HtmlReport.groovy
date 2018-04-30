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
    final Map<License, List<Project>> projectsMap = new HashMap<>()

    // Store packages by license
    projects.each { project ->
      def key = new License(name: "No license found", url: "N/A")

      if (project.licenses && project.licenses.size > 0) {
        key = project.licenses[0]
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
            final List<Project> sortedProjects = entry.value.sort {
              left, right -> left.name <=> right.name
            }

            def currentProject = null
            def currentLicense = null
            sortedProjects.each { project ->
              currentProject = project
              currentLicense = entry.key.url.hashCode()

              // Display libraries
              li {
                a(href: "#${currentLicense}", project.name)
              }
            }

            // Display associated license with libraries
            // Try to find license by URL, name and then default to whatever is listed in the POM.xml
            def licenseMap = LicenseHelper.LICENSE_MAP
            if (!currentProject.licenses || currentProject.licenses.size == 0) {
              pre(NO_LICENSE)
            } else if (licenseMap.containsKey(entry.key.url)) {
              a(name: currentLicense)
              pre(getLicenseText(licenseMap.get(entry.key.url)))
            } else if (licenseMap.containsKey(entry.key.name)) {
              a(name: currentLicense)
              pre(getLicenseText(licenseMap.get(entry.key.name)))
            } else {
              if (currentProject && (currentProject.licenses[0].name.trim() || currentProject.licenses[0].url.trim())) {
                pre("${currentProject.licenses[0].name.trim()}\n${currentProject.licenses[0].url.trim()}")
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
    final def writer = new StringWriter()
    final def markup = new MarkupBuilder(writer)

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
