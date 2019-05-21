package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import com.jaredsburrows.license.internal.pom.Project
import kotlinx.html.A
import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.HtmlTagMarker
import kotlinx.html.a
import kotlinx.html.attributesMapOf
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
import kotlinx.html.visit
import java.io.File

class HtmlReport(private val projects: List<Project>) {
  companion object {
    const val CSS_STYLE = "body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }"
    const val OPEN_SOURCE_LIBRARIES = "Open source licenses"
    const val NO_LIBRARIES = "None"
    const val NO_LICENSE = "No license found"
    const val NOTICE_LIBRARIES = "Notice for packages:"
  }

  /** Return Html as a String. */
  fun string(): String = if (projects.isEmpty()) noOpenSourceHtml() else openSourceHtml()

  /** Html report when there are open source licenses. */
  private fun openSourceHtml(): String {
    val projectsMap = hashMapOf<String?, List<Project>>()
    val licenseMap = LicenseHelper.licenseMap

    // Store packages by license
    projects.forEach { project ->
      var key: String? = ""

      // first check to see if the project's license is in our list of known licenses:
      if (!project.licenses.isNullOrEmpty()) {
        val license = project.licenses.first()
        key = when {
          // look up by url
          licenseMap.containsKey(license.url) -> licenseMap[license.url]
          // then by name
          licenseMap.containsKey(license.name) -> licenseMap[license.name]
          // otherwise, use the url as a key
          else -> license.url
        }
      }

      if (!projectsMap.containsKey(key)) {
        projectsMap[key] = arrayListOf()
      }

      (projectsMap[key] as ArrayList).add(project)
    }

    return StringBuilder()
      .appendHTML()
      .html {
        head {
          style {
            unsafe { +CSS_STYLE }
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
              val sortedProjects = entry.value.sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
              )

              var currentProject: Project? = null
              var currentLicense: Int? = null

              sortedProjects.forEach { project ->
                currentProject = project
                currentLicense = entry.key.hashCode()

                // Display libraries
                li {
                  a(href = "#$currentLicense") {
                    +project.name
                  }
                }
              }

              a(name = currentLicense.toString())
              // Display associated license with libraries
              if (currentProject?.licenses.isNullOrEmpty()) {
                pre {
                  unsafe { +NO_LICENSE }
                }
              } else if (!entry.key.isNullOrEmpty() && licenseMap.values.contains(entry.key!!)) {
                // license from license map
                pre {
                  unsafe { +getLicenseText(entry.key!!) }
                }
              } else {
                // if not found in the map, just display the info from the POM.xml
                val currentLicenseName = currentProject?.licenses?.first()?.name?.trim()
                val currentUrl = currentProject?.licenses?.first()?.url?.trim()

                if (!currentLicenseName.isNullOrEmpty() || !currentUrl.isNullOrEmpty()) {
                  pre {
                    unsafe { +"$currentLicenseName\n<a href=\"$currentUrl\">$currentUrl</a>" }
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
      .toString()
  }

  /** Html report when there are no open source licenses. */
  private fun noOpenSourceHtml(): String = StringBuilder()
    .appendHTML()
    .html {
      head {
        style {
          unsafe { +CSS_STYLE }
        }
        title {
          +OPEN_SOURCE_LIBRARIES
        }
      }

      body {
        h3 {
          unsafe { +NO_LIBRARIES }
        }
      }
    }.toString()

  private fun getLicenseText(fileName: String): String =
    HtmlReport::class.java.getResource(
      "/license/$fileName".replace('/', File.separatorChar)
    ).readText()
}

@HtmlTagMarker
fun FlowOrInteractiveOrPhrasingContent.a(
  href: String? = null,
  target: String? = null,
  classes: String? = null,
  name: String? = null,
  block: A.() -> Unit = {}
): Unit = A(attributesMapOf(
  "href", href,
  "target", target,
  "class", classes,
  "name", name), consumer)
  .visit(block)
