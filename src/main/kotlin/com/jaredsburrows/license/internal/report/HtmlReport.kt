package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Project
import kotlinx.html.body
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

class HtmlReport(private val projects: List<Project>) : HtmlReportG() {
  companion object {
//    const val CSS = "body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }"
//    const val OPEN_SOURCE_LIBRARIES = "Open source licenses"
//    const val NO_LIBRARIES = "None"
//    const val NO_LICENSE = "No license found"
//    const val NOTICE_LIBRARIES = "Notice for packages:"
  }

  /**
   * Return Html as a String.
   */
  fun string(): String {
    return if (projects.isEmpty()) noOpenSourceHtml() else openSourceHtml()
  }

/*
  /**
   * Html report when there are open source licenses.
   */
  fun openSourceHtml2(): String {
    val projectsMap = hashMapOf<String?, List<Project>>()
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

      if (projectsMap.containsKey(key)) {
        projectsMap[key] = listOf()
      }
//      (projectsMap[key] as List).add
//      projectsMap[key]?.add(project)
    }

    return StringBuilder()
      .appendHTML()
      .html {
        head {
          style {
            unsafe {
              +CSS
            }
          }
          title {
            unsafe {
              +OPEN_SOURCE_LIBRARIES
            }
          }
        }

        body {
          h3 {
            unsafe {
              +NOTICE_LIBRARIES
            }
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
                  unsafe {
                    +NO_LICENSE
                  }
                }
//              } else if (!entry.key.isNullOrEmpty() && licenseMap.values.contains(entry.key)) {
//                // license from license map
//                pre {
//                  unsafe {
////                    getLicenseText(entry.key)
//                  }
//                }
              } else {
                // if not found in the map, just display the info from the POM.xml -  name along with the url
                val currentLicenseName = currentProject?.licenses?.get(0)?.name?.trim()
                val currentUrl = currentProject?.licenses?.get(0)?.url?.trim()

                if (!currentLicenseName.isNullOrEmpty() || !currentUrl.isNullOrEmpty()) {
                  pre {

                  }
                } else {
                  pre {
                    unsafe {
                      +NO_LICENSE
                    }
                  }
                }
              }
            }
          }
        }
      }
      .toString()
  }
*/
  /**
   * Html report when there are no open source licenses.
   */
  private fun noOpenSourceHtml(): String {
    return StringBuilder()
      .appendHTML()
      .html {
        head {
          style {
            unsafe {
              +CSS
            }
          }
          title {
            +OPEN_SOURCE_LIBRARIES
          }
        }

        body {
          h3 {
            unsafe {
              +NO_LIBRARIES
            }
          }
        }
      }.toString()
  }

  private fun getLicenseText(fileName: String): String {
    return HtmlReport::class.java.getResource("/license/$fileName").readText()
  }
}
