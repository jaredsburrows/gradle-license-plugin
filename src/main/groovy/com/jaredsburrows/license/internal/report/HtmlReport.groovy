package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import groovy.xml.MarkupBuilder

final class HtmlReport {
  private static final String BODY_CSS = "body { font-family: sans-serif }"
  private static final String PRE_CSS = "pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }"
  private static final String CSS_STYLE = BODY_CSS + " " + PRE_CSS
  private static final String OPEN_SOURCE_LIBRARIES = "Open source licenses"
  private static final String NO_LIBRARIES = "None"
  private static final String NO_LICENSE = "No license found"
  private static final String NOTICE_LIBRARIES = "Notice for packages:"
  private final List<Project> projects

  public HtmlReport(List<Project> projects) {
    this.projects = projects
  }

  /**
   * Return Html as a String.
   */
  public String string() {
    return projects.isEmpty() ? noOpenSourceHtml() : openSourceHtml()
  }

  /**
   * Html report when there are open source licenses.
   */
  private String openSourceHtml() {
    StringWriter writer = new StringWriter()
    MarkupBuilder markup = new MarkupBuilder(writer)
    Map<String, List<Project>> projectsMap = new HashMap<>()
    Map<String, String> licenseMap = LicenseHelper.getLicenseMap()

    // Store packages by license
    projects.each { project ->
      String key = ""

      // first check to see if the project's license is in our list of known licenses:
      if (project.getLicenses() && !project.getLicenses().isEmpty()) {
        License license = project.getLicenses().get(0)
        if (licenseMap.containsKey(license.getUrl())) {
          // look up by url
          key = licenseMap[license.getUrl()]
        } else if (licenseMap.containsKey(license.getName())) {
          // then by name
          key = licenseMap[license.getName()]
        } else {
          // otherwise, use the url as a key
          key = license.getUrl()
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
            List<Project> sortedProjects = entry.getValue().sort {
              left, right -> left.getName() <=> right.getName()
            }

            Project currentProject = null
            Integer currentLicense = null
            sortedProjects.each { project ->
              currentProject = project
              currentLicense = entry.getKey().hashCode()

              // Display libraries
              li {
                a(href: "#${currentLicense}", project.getName())
              }
            }

            a(name: currentLicense)
            // Display associated license with libraries
            if (!currentProject.getLicenses() || currentProject.getLicenses().isEmpty()) {
              pre(NO_LICENSE)
            } else if (!entry.getKey().isEmpty() && licenseMap.values().contains(entry.getKey())) {
              // license from license map
              pre(getLicenseText(entry.key))
            } else {
              // if not found in the map, just display the info from the POM.xml -  name along with the url
              String currentLicenseName = currentProject.getLicenses().get(0).getName().trim()
              String currentUrl = currentProject.getLicenses().get(0).getUrl().trim()
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
    return writer.toString()
  }

  /**
   * Html report when there are no open source licenses.
   */
  private static String noOpenSourceHtml() {
    StringWriter writer = new StringWriter()
    MarkupBuilder markup = new MarkupBuilder(writer)

    markup.html {
      head {
        style(CSS_STYLE)
        title(OPEN_SOURCE_LIBRARIES)
      }

      body {
        h3(NO_LIBRARIES)
      }
    }
    return writer.toString()
  }

  private String getLicenseText(String fileName) {
    return getClass().getResource("/license/${fileName}").getText()
  }
}
