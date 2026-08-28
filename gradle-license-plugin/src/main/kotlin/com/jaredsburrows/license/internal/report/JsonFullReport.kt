package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.LicenseHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.apache.maven.model.Model

/**
 * Generates a "full" JSON report of projects dependencies.
 *
 * This is the [JsonReport] plus the full text of every known license, for applications that render
 * their own license screen instead of displaying the generated HTML report.
 *
 * Every license text is stored exactly once under `license_texts`, keyed by license id (eg. "mit"),
 * and each dependency references it by `license_key`. Dependencies overwhelmingly share a handful
 * of licenses, so interning the texts keeps the report - and the memory an app needs to parse it -
 * roughly an order of magnitude smaller than repeating the text per dependency.
 *
 * `license_key` is null for licenses the plugin does not bundle; `license` and `license_url` always
 * hold what the POM declared.
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
    val licenseTexts = sortedMapOf<String, String>()

    val dependencies =
      projects.map { project ->
        // Handle multiple licenses
        val licensesJson =
          project.licenses.map { license ->
            val licenseKey = licenseKey(license.name, license.url, licenseTexts)

            linkedMapOf(
              LICENSE to license.name,
              LICENSE_URL to license.url,
              LICENSE_KEY to licenseKey,
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

    return toJson(
      linkedMapOf(
        LICENSE_TEXTS to licenseTexts,
        DEPENDENCIES to dependencies,
      ),
    )
  }

  override fun emptyReport(): String =
    toJson(
      linkedMapOf(
        LICENSE_TEXTS to emptyMap<String, String>(),
        DEPENDENCIES to emptyList<Any>(),
      ),
    )

  /**
   * The id of a bundled license (eg. "apache-2.0"), recording its text in [licenseTexts] the first
   * time it is seen. Null when the plugin has no text for the license.
   */
  private fun licenseKey(
    name: String?,
    url: String?,
    licenseTexts: MutableMap<String, String>,
  ): String? {
    val fileName = LicenseHelper.licenseFileName(name, url) ?: return null
    val text = LicenseHelper.licenseText(fileName) ?: return null

    val licenseKey = fileName.removeSuffix(TEXT_EXTENSION)
    licenseTexts.putIfAbsent(licenseKey, text)
    return licenseKey
  }

  private fun toJson(report: Map<String, Any?>): String =
    moshi
      .adapter<Map<String, Any?>>(
        Types.newParameterizedType(
          Map::class.java,
          String::class.java,
          Any::class.java,
        ),
      ).serializeNulls()
      .toJson(report)

  private companion object {
    private const val EXTENSION = "full.json"
    private const val NAME = "Full JSON"
    private const val TEXT_EXTENSION = ".txt"
    private const val LICENSE_TEXTS = "license_texts"
    private const val DEPENDENCIES = "dependencies"
    private const val PROJECT = "project"
    private const val DESCRIPTION = "description"
    private const val VERSION = "version"
    private const val DEVELOPERS = "developers"
    private const val URL = "url"
    private const val YEAR = "year"
    private const val LICENSES = "licenses"
    private const val LICENSE = "license"
    private const val LICENSE_URL = "license_url"
    private const val LICENSE_KEY = "license_key"
    private const val DEPENDENCY = "dependency"
    private val moshi = Moshi.Builder().build()
  }
}
