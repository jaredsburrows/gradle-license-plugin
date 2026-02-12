package com.jaredsburrows.license.internal.report

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.apache.maven.model.Model

/**
 * Generates JSON report of projects dependencies.
 *
 * @property projects list of [Model]s for thr JSON report.
 */
class JsonReport(
  private val projects: List<Model>,
) : Report {
  override fun toString(): String = report()

  override fun name(): String = NAME

  override fun extension(): String = EXTENSION

  override fun report(): String = if (projects.isEmpty()) emptyReport() else fullReport()

  override fun fullReport(): String {
    val reportList =
      projects.map { project ->
        // Handle multiple licenses
        val licensesJson =
          project.licenses.map { license ->
            linkedMapOf(
              LICENSE to license.name,
              LICENSE_URL to license.url,
            )
          }

        // Handle multiple developer
        val developerNames = project.developers.map { it.id }

        // Build the report
        linkedMapOf(
          PROJECT to project.name.valueOrNull(),
          DESCRIPTION to project.description.valueOrNull(),
          VERSION to project.version.valueOrNull(),
          DEVELOPERS to developerNames,
          URL to project.url.valueOrNull(),
          YEAR to project.inceptionYear.valueOrNull(),
          LICENSES to licensesJson,
          DEPENDENCY to "${project.groupId}:${project.artifactId}:${project.version}",
        )
      }

    return moshi
      .adapter<List<Map<String, Any?>>>(
        Types.newParameterizedType(
          List::class.java,
          Map::class.java,
          String::class.java,
          Any::class.java,
        ),
      ).serializeNulls()
      .toJson(reportList)
  }

  override fun emptyReport(): String = EMPTY_JSON

  private companion object {
    private const val EXTENSION = "json"
    private const val NAME = "JSON"
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
    private val moshi = Moshi.Builder().build()
  }
}
