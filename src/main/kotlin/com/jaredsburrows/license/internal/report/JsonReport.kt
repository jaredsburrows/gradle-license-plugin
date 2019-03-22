package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Project
import groovy.json.JsonBuilder

class JsonReport(private val projects: List<Project>) {
  companion object {
    private const val PROJECT = "project"
    private const val DESCRIPTION = "description"
    private const val VERSION = "version"
    private const val DEVELOPERS = "developers"
    private const val URL = "url"
    private const val YEAR = "year"
    private const val LICENSES = "licenses"
    private const val LICENSE = "license"
    private const val LICENSE_URL = "license_url"
    private const val EMPTY_JSON_ARRAY = "[]"
    private const val DEPENDENCY = "dependency"
  }

  /**
   * Return Json as a String.
   */
  fun string(): String = if (projects.isEmpty()) EMPTY_JSON_ARRAY else jsonArray()

  /**
   * Json report when there are open source licenses.
   */
  private fun jsonArray(): String {
    val reportList = mutableListOf<Map<String, Any?>>()
    projects.forEach { project ->
      // Handle multiple licenses
      val licensesJson = mutableListOf<Map<String, String?>>()
      project.licenses.orEmpty().forEach { license ->
        licensesJson.add(linkedMapOf(
          LICENSE to license.name,
          LICENSE_URL to license.url
        ))
      }

      // Handle multiple developer
      val developerNames = mutableListOf<String?>()
      project.developers.orEmpty().forEach { developer ->
        developerNames.add(developer.name)
      }

      // Build the report
      reportList.add(linkedMapOf(
        PROJECT to if (!project.name.isNullOrEmpty()) project.name else null,
        DESCRIPTION to if (!project.description.isNullOrEmpty()) project.description else null,
        VERSION to if (!project.version.isNullOrEmpty()) project.version else null,
        DEVELOPERS to developerNames,
        URL to if (!project.url.isNullOrEmpty()) project.url else null,
        YEAR to if (!project.year.isNullOrEmpty()) project.year else null,
        LICENSES to licensesJson,
        DEPENDENCY to project.gav
      ))
    }

    return JsonBuilder(reportList).toPrettyString()
  }
}
