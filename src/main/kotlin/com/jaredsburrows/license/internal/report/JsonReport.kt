package com.jaredsburrows.license.internal.report

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jaredsburrows.license.internal.pom.Project

/**
 * Generates JSON report of projects dependencies.
 *
 * @property projects list of [Project]s for thr JSON report.
 */
class JsonReport(private val projects: List<Project>) : Report {

  override fun toString(): String = report()

  override fun report(): String = if (projects.isEmpty()) emptyReport() else fullReport()

  override fun fullReport(): String {
    val reportList = projects.map { project ->
      // Handle multiple licenses
      val licensesJson = project.licenses.map { license ->
        linkedMapOf(LICENSE to license.name, LICENSE_URL to license.url)
      }

      // Handle multiple developer
      val developerNames = project.developers.map { it.name }

      // Build the report
      linkedMapOf(
        PROJECT to project.name.valueOrNull(),
        DESCRIPTION to project.description.valueOrNull(),
        VERSION to project.version.valueOrNull(),
        DEVELOPERS to developerNames,
        URL to project.url.valueOrNull(),
        YEAR to project.year.valueOrNull(),
        LICENSES to licensesJson,
        DEPENDENCY to project.gav
      )
    }

    return gson.toJson(reportList, object : TypeToken<MutableList<Map<String, Any?>>>() {}.type)
  }

  override fun emptyReport(): String = EMPTY_JSON

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
    private const val DEPENDENCY = "dependency"
    private const val EMPTY_JSON = "[]"
    private val gson = GsonBuilder()
      .setPrettyPrinting()
      .serializeNulls()
      .create()
  }
}
