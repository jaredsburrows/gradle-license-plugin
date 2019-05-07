package com.jaredsburrows.license.internal.report

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jaredsburrows.license.internal.pom.Project

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
    private const val EMPTY_JSON = "[]"
    private const val DEPENDENCY = "dependency"
    private val gson = GsonBuilder()
      .setPrettyPrinting()
      .serializeNulls()
      .create()
  }

  /**
   * Return Json as a String.
   */
  fun string(): String = if (projects.isEmpty()) EMPTY_JSON else json()

  /**
   * Json report when there are open source licenses.
   */
  private fun json(): String {
    val reportList = mutableListOf<Map<String, Any?>>()
    projects.forEach { project ->
      // Handle multiple licenses
      val licensesJson = mutableListOf<Map<String, String?>>()
      project.licenses.forEach { license ->
        licensesJson.add(linkedMapOf(
          LICENSE to license.name,
          LICENSE_URL to license.url
        ))
      }

      // Handle multiple developer
      val developerNames = mutableListOf<String?>()
      project.developers.forEach { developer ->
        developerNames.add(developer.name)
      }

      // Build the report
      reportList.add(linkedMapOf(
        PROJECT to if (!project.name.isEmpty()) project.name else null,
        DESCRIPTION to if (!project.description.isEmpty()) project.description else null,
        VERSION to if (!project.version.isEmpty()) project.version else null,
        DEVELOPERS to developerNames,
        URL to if (!project.url.isEmpty()) project.url else null,
        YEAR to if (!project.year.isEmpty()) project.year else null,
        LICENSES to licensesJson,
        DEPENDENCY to project.gav
      ))
    }

    return gson.toJson(reportList, object : TypeToken<MutableList<Map<String, Any?>>>() {}.type)
  }
}
