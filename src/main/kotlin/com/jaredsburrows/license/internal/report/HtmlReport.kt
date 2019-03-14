package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import com.jaredsburrows.license.internal.pom.Project
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.li
import kotlinx.html.pre
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe

class HtmlReport(private val projects: List<Project>) {
  companion object {
    private const val CSS = "body { font-family: sans-serif } " +
      "pre { background-color: #eeeeee; " +
      "padding: 1em; " +
      "white-space: pre-wrap; " +
      "display: inline-block }"
    private const val OPEN_SOURCE_LIBRARIES = "Open source licenses"
    private const val NO_LIBRARIES = "None"
    private const val NO_LICENSE = "No license found"
    private const val NOTICE_LIBRARIES = "Notice for packages:"

    /**
     * Html report when there are no open source licenses.
     */
    private fun noOpenSourceHtml(): String {
      return buildString {
        appendHTML(false)
          .html {
            head {
              style {
                unsafe { +CSS }
              }
              title {
                unsafe { +OPEN_SOURCE_LIBRARIES }
              }
            }

            body {
              h3 {
                unsafe { +NO_LIBRARIES }
              }
            }
          }
      }
    }

    private fun getLicenseText(fileName: String): String {
      return HtmlReport::class.java.getResource("/license/$fileName").readText()
    }
  }

  /**
   * Return Html as a String.
   */
  fun string(): String = if (projects.isEmpty()) noOpenSourceHtml() else openSourceHtml()

  /**
   * Html report when there are open source licenses.
   */
  private fun openSourceHtml(): String {
    val projectsMap = hashMapOf<String?, ArrayList<Project>>()
    val licenseMap = LicenseHelper.licenseMap

    // Store packages by license
    projects.forEach { project ->
      var key: String? = ""

      // first check to see if the project's license is in our list of known licenses:
      if (!project.licenses.isNullOrEmpty()) {
        val license = project.licenses?.get(0)
        key = when {
          // look up by url
          licenseMap.containsKey(license?.url) -> licenseMap[license?.url]
          // then by name
          licenseMap.containsKey(license?.name) -> licenseMap[license?.name]
          // otherwise, use the url as a key
          else -> license?.url
        }
      }

      if (!projectsMap.containsKey(key)) {
        projectsMap[key] = arrayListOf()
      }

      projectsMap[key]?.add(project)
    }

    return buildString {
      appendHTML(false)
        .html {
          head {
            style {
              unsafe { +CSS }
            }
            title {
              unsafe { +OPEN_SOURCE_LIBRARIES }
            }
          }

          body {
            h3 {
              unsafe { +NOTICE_LIBRARIES }
            }
            ul {
              projectsMap.entries.forEach { entry ->
                val sortedProjects = entry.value.sortedWith(compareBy { it.name })

                var currentProject: Project? = null
                var currentLicense: Int? = null

                sortedProjects.forEach { project ->
                  currentProject = project
                  currentLicense = entry.key.hashCode()

                  // Display libraries
                  li {
                    a("#$currentLicense") {
                      +"${project.name}"
                    }
                  }
                }

                a(currentLicense.toString())
                // Display associated license with libraries
                if (currentProject?.licenses.isNullOrEmpty()) {
                  pre {
                    unsafe { +NO_LICENSE }
                  }
                } else if (!entry.key.isNullOrEmpty() && licenseMap.values.contains(entry.key.toString())) {
                  // license from license map
                  pre {
                    unsafe { getLicenseText(entry.key.toString()) }
                  }
                } else {
                  // if not found in the map, just display the info from the POM.xml - name along with the url
                  val currentLicenseName = currentProject?.licenses?.get(0)?.name?.trim()
                  val currentUrl = currentProject?.licenses?.get(0)?.url?.trim()

                  if (!currentLicenseName.isNullOrEmpty() || !currentUrl.isNullOrEmpty()) {
                    // TODO finish
                    pre {
                      unsafe { +"$currentLicenseName" }
                      a("#$currentUrl") {
                        +"$currentUrl"
                      }
//                      mkp.yield("$currentLicenseName\n")
//                      mkp.yieldUnescaped("<a href='$currentUrl'>$currentUrl</a>")
                    }
                  } else {
                    pre {
                      unsafe { +NO_LICENSE }
                    }
                  }
                }
              }
            }
          }
        }
    }
  }
}

/*
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
              // if not found in the map, just display the info from the POM.xml - name along with the url
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
 */
