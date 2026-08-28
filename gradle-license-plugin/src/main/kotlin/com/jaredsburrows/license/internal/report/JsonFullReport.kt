package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.apache.maven.model.Model

/**
 * Generates a "full" JSON report of projects dependencies.
 *
 * This is the [JsonReport] plus the full text of every known license (`license_text`), so
 * applications can render their own custom license screen instead of displaying the generated
 * HTML report. `license_text` is null when the license is not one of the bundled licenses.
 *
 * @property projects list of [Model]s for the full JSON report.
 */
class JsonFullReport(
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
              LICENSE_TEXT to LicenseHelper.licenseTextFor(license.name, license.url),
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
    private const val EXTENSION = "full.json"
    private const val NAME = "Full JSON"
    private const val PROJECT = "project"
    private const val DESCRIPTION = "description"
    private const val VERSION = "version"
    private const val DEVELOPERS = "developers"
    private const val URL = "url"
    private const val YEAR = "year"
    private const val LICENSES = "licenses"
    private const val LICENSE = "license"
    private const val LICENSE_URL = "license_url"
    private const val LICENSE_TEXT = "license_text"
    private const val DEPENDENCY = "dependency"
    private const val EMPTY_JSON = "[]"
    private val moshi = Moshi.Builder().build()
  }
}
