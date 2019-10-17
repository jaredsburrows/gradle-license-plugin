package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import kotlinx.html.*
import kotlinx.html.stream.appendHTML

/**
 * Generates HTML report of projects dependencies.
 *
 * @property projects list of [Project]s for thr HTML report.
 */
class HtmlReport(private val projects: List<Project>) {
  companion object {
    const val CSS_STYLE = "body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }"
    const val OPEN_SOURCE_LIBRARIES = "Open source licenses"
    const val NO_LIBRARIES = "None"
    const val NO_LICENSE = "No license found"
    const val NOTICE_LIBRARIES = "Notice for packages:"
    const val COPYRIGHT = "Copyright "
    const val DEFAULT_AUTHOR = "The original author or authors"
    const val DEFAULT_YEAR = "20xx"
    const val MISSING_LICENSE = "Missing standard license text for: "
  }

  /** Return Html as a String. */
  fun string(): String = if (projects.isEmpty()) noOpenSourceHtml() else openSourceHtml()

  /** Html report when there are open source licenses. */
  private fun openSourceHtml(): String {
    val projectsMap = hashMapOf<String?, List<Project>>()
    val licenseMap = LicenseHelper.licenseMap

    // Store packages by licenses: build a composite key of all the licenses, sorted in the (probably vain)
    // hope that there's more than one project with the same set of multiple licenses.
    projects.forEach { project ->
      val keys: MutableList<String> = mutableListOf()

      // first check to see if the project's license is in our list of known licenses.
      if (!project.licenses.isNullOrEmpty()) {
        project.licenses.forEach { license ->
          keys.add(getLicenseKey(license))
        }
      }

      keys.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
      var key = ""
      if (keys.size > 0) {
        // No Licenses -> empty key, sort first
        key = keys.toString()
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
                    +" ("
                    +project.version
                    +")"
                  }
                  val copyrightYear = if (project.year.isEmpty()) DEFAULT_YEAR else project.year
                  dl {
                    if (project.developers.isNotEmpty()) {
                      project.developers.forEach { developer ->
                        dt {
                          +COPYRIGHT
                          +Entities.copy
                          +" "
                          +copyrightYear
                          +" "
                          +developer.name
                        }
                      }
                    } else {
                      dt {
                        +COPYRIGHT
                        +Entities.copy
                        +" "
                        +copyrightYear
                        +" "
                        +DEFAULT_AUTHOR
                      }
                    }
                  }
                }
              }

              // This isn't correctly indented in the html source (but is otherwise correct).
              // It appears to be a bug in the DSL implementation from what little I see on the web.
              a(name = currentLicense.toString())

              // Display associated license text with libraries
              val licenses = currentProject?.licenses
              if (licenses.isNullOrEmpty()) {
                pre {
                  unsafe { +NO_LICENSE }
                }
              } else {
                licenses.forEach { license ->
                  val key = getLicenseKey(license)
                  if (key.isNotEmpty() && licenseMap.values.contains(key)) {
                    // license from license map
                    pre {
                      unsafe { +getLicenseText(key) }
                    }
                  } else {
                    // if not found in the map, just display the info from the POM.xml
                    val currentLicenseName = license.name.trim()
                    val currentUrl = license.url.trim()

                    if (currentLicenseName.isNotEmpty() && currentUrl.isNotEmpty()) {
                      pre {
                        unsafe { +"$currentLicenseName\n<a href=\"$currentUrl\">$currentUrl</a>" }
                      }
                    } else if (currentUrl.isNotEmpty()) {
                      pre {
                        unsafe { +"<a href=\"$currentUrl\">$currentUrl</a>" }
                      }
                    } else if (currentLicenseName.isNotEmpty()) {
                      pre {
                        unsafe { +"$currentLicenseName\n" }
                      }
                    } else {
                      pre {
                        unsafe { +NO_LICENSE }
                      }
                    }
                  }
                  br
                }
              }
              hr {}
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

  private fun getLicenseText(fileName: String): String {
    val resource = HtmlReport::class.java.getResource("/license/$fileName")
    return (resource?.readText() ?: MISSING_LICENSE + fileName)
  }

  private fun getLicenseKey(license: License): String {
    // See if the license is in our list of known licenses (which coalesces differing URLs to the same license text)
    // If not, use the URL if present. Else "".
    return when {
      // look up by url
      LicenseHelper.licenseMap.containsKey(license.url) -> LicenseHelper.licenseMap[license.url]
      // then by name
      LicenseHelper.licenseMap.containsKey(license.name) -> LicenseHelper.licenseMap[license.name]
      // otherwise, use the url as a key
      else -> license.url
    } as String
  }
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
