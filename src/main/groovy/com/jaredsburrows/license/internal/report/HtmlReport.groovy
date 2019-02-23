package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import groovy.xml.MarkupBuilder

final class HtmlReport extends HtmlReportKt {
  public HtmlReport(List<Project> projects) {
    super(projects)
  }

  /**
   * Html report when there are open source licenses.
   */
  public String openSourceHtml() {
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
        style(CSS)
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
}
