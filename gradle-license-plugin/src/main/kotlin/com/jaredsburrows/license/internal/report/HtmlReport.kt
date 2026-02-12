package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import kotlinx.html.Entities
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.dd
import kotlinx.html.dl
import kotlinx.html.dt
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.lang
import kotlinx.html.li
import kotlinx.html.pre
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe
import org.apache.maven.model.License
import org.apache.maven.model.Model

/**
 * Generates HTML report of projects dependencies.
 *
 * @property projects list of [Model]s for thr HTML report.
 */
class HtmlReport(
  private val projects: List<Model>,
  private val showVersions: Boolean,
) : Report {
  override fun toString(): String = report()

  override fun name(): String = NAME

  override fun extension(): String = EXTENSION

  override fun report(): String = if (projects.isEmpty()) emptyReport() else fullReport()

  override fun fullReport(): String {
    val projectsMap = hashMapOf<String, List<Model>>()
    val licenseMap = LicenseHelper.licenseMap

    // Store packages by licenses: build a composite key of all the licenses, sorted in the (probably vain)
    // hope that there's more than one project with the same set of multiple licenses.
    projects.forEach { project ->
      val keys = mutableListOf<String>()

      // first check to see if the project's license is in our list of known licenses.
      if (project.licenses.isNotEmpty()) {
        project.licenses.forEach { license -> keys += getLicenseKey(license) }
      }

      keys.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
      var key = ""
      if (keys.isNotEmpty()) {
        // No Licenses -> empty key, sort first
        key = keys.toString()
      }

      if (!projectsMap.containsKey(key)) {
        projectsMap[key] = mutableListOf()
      }

      (projectsMap[key] as MutableList).add(project)
    }

    val sortedProjectsList =
      projectsMap.entries
        .map { (key, projects) ->
          Pair(key, projects.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }))
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.first })

    return buildString {
      appendLine(DOCTYPE) // createHTMLDocument() add doctype and meta
      appendHTML()
        .html {
          lang = "en"
          head {
            unsafe { +META }
            style {
              unsafe { +CSS_STYLE }
            }
            title { +OPEN_SOURCE_LIBRARIES }
          }

          body {
            h3 {
              +NOTICE_LIBRARIES
            }
            sortedProjectsList.forEach { (key, sortedProjects) ->
              var currentProject: Model? = null
              var currentLicense: Int? = null

              ul {
                sortedProjects.forEach { project ->
                  currentProject = project
                  currentLicense = key.hashCode()

                  // Display libraries
                  li {
                    a(href = "#$currentLicense") {
                      +project.name
                      if (showVersions) {
                        +" (${project.version})"
                      }
                    }
                    val copyrightYear = project.inceptionYear.ifEmpty { DEFAULT_YEAR }
                    dl {
                      if (project.developers.isNotEmpty()) {
                        project.developers.forEach { developer ->
                          dt {
                            +COPYRIGHT
                            +Entities.copy
                            +" $copyrightYear ${developer.id}"
                          }
                          dd { }
                        }
                      } else {
                        dt {
                          +COPYRIGHT
                          +Entities.copy
                          +" $copyrightYear $DEFAULT_AUTHOR"
                        }
                        dd { }
                      }
                    }
                  }
                }
              }

              a {
                id = currentLicense.toString()
              }

              // Display associated license text with libraries
              val licenses = currentProject?.licenses
              if (licenses.isNullOrEmpty()) {
                pre {
                  +NO_LICENSE
                }
              } else {
                val sortedKeysAndLicenses =
                  licenses
                    .map { license ->
                      Pair(getLicenseKey(license), license)
                    }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.first })

                sortedKeysAndLicenses.forEach { (key, license) ->
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
                        +NO_LICENSE
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
  }

  override fun emptyReport(): String =
    buildString {
      appendLine(DOCTYPE) // createHTMLDocument() add doctype and meta
      appendHTML()
        .html {
          lang = "en"
          head {
            unsafe { +META }
            style {
              unsafe { +CSS_STYLE }
            }
            title { +OPEN_SOURCE_LIBRARIES }
          }

          body {
            h3 {
              +NO_LIBRARIES
            }
          }
        }
    }

  /**
   * See if the license is in our list of known licenses (which coalesces differing URLs to the
   * same license text). If not, use the URL if present. Else "".
   */
  private fun getLicenseKey(license: License): String =
    when {
      // look up by url
      LicenseHelper.licenseMap.containsKey(license.url) -> LicenseHelper.licenseMap[license.url]
      // then by name
      LicenseHelper.licenseMap.containsKey(license.name) -> LicenseHelper.licenseMap[license.name]
      // otherwise, use the url as a key
      else -> license.url
    } as String

  private companion object {
    private const val EXTENSION = "html"
    private const val NAME = "HTML"
    const val DOCTYPE = "<!DOCTYPE html>"
    const val META = "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"
    const val CSS_STYLE =
      // Default light theme styles
      "body { font-family: sans-serif; background-color: #ffffff; color: #000000; } " +
        "a { color: #0000EE; } " +
        "pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; " +
        "word-break: break-word; display: inline-block; } " +

        // Dark theme styles
        "@media (prefers-color-scheme: dark) { " +
        "body { background-color: #121212; color: #E0E0E0; } " +
        "a { color: #BB86FC; } " +
        "pre { background-color: #333333; color: #E0E0E0; } }"
    const val OPEN_SOURCE_LIBRARIES = "Open source licenses"
    const val NO_LIBRARIES = "None"
    const val NO_LICENSE = "No license found"
    const val NOTICE_LIBRARIES = "Notice for packages:"
    const val COPYRIGHT = "Copyright "
    const val DEFAULT_AUTHOR = "The original author or authors"
    const val DEFAULT_YEAR = "20xx"
    private const val MISSING_LICENSE = "Missing standard license text for: "

    @JvmStatic
    fun getLicenseText(fileName: String): String =
      HtmlReport::class.java
        .getResource("/license/$fileName")
        ?.readText()
        ?: (MISSING_LICENSE + fileName)
  }
}
