package com.jaredsburrows.license.internal.report

import org.apache.maven.model.Model

/**
 * Generates CSV report of projects dependencies.
 *
 * @property projects list of [Model]s for thr CSV report.
 */
class CsvReport(
  private val projects: List<Model>,
) : Report {
  override fun toString(): String = report()

  override fun name(): String = NAME

  override fun extension(): String = EXTENSION

  override fun report(): String = if (projects.isEmpty()) emptyReport() else fullReport()

  override fun fullReport(): String {
    val projectInfoList = mutableListOf<String>()
    projectInfoList += COLUMNS

    projects.map { project ->
      val projectInfo =
        mutableListOf<String?>().apply {
          // Project Name
          addCsvString(project.name)

          // Project Description
          addCsvString(project.description)

          // Project Version
          addCsvString(project.version)

          // Project Developers
          addCsvList(project.developers) { it.id }

          // Project Url
          addCsvString(project.url)

          // Project Year
          addCsvString(project.inceptionYear)

          // Project License Names
          addCsvList(project.licenses) { it.name }

          // Project License Url
          addCsvList(project.licenses) { it.url }

          // Project Dependency
          addCsvString("${project.groupId}:${project.artifactId}:${project.version}")
        }

      // Add each row to the list
      projectInfoList += projectInfo.toCsv()
    }

    // Separate each record with a new line
    return projectInfoList.joinToString(separator = "\n")
  }

  override fun emptyReport(): String = EMPTY_CSV

  /** Convert list of elements to comma separated list. */
  private fun MutableList<String?>.toCsv(): String = this.joinToString(separator = ",") { it ?: "" }

  /** Add elements to Csv. */
  private fun MutableList<String?>.addCsvString(element: String): Boolean {
    val escaped =
      element
        .valueOrNull()
        ?.replace("\"", "\"\"")
        ?.let { el ->
          when {
            el.contains(",") ||
              el.contains("\n") ||
              el.contains("'") ||
              el.contains("\\") ||
              el.contains("\"")
            -> "\"$el\""

            else -> el
          }
        }
    return this.add(escaped)
  }

  /** Add List of elements to Csv as comma separated list with quotes. */
  private fun <T> MutableList<String?>.addCsvList(
    elements: List<T>,
    transform: ((T) -> CharSequence)? = null,
  ): Boolean =
    when {
      elements.isEmpty() -> this.add(null)
      else -> addCsvString(elements.joinToString(separator = ",", transform = transform))
    }

  private companion object {
    private const val EXTENSION = "csv"
    private const val NAME = "CSV"
    private const val COLUMNS =
      "project,description,version,developers,url,year,licenses,license urls,dependency"
    private const val EMPTY_CSV = ""
  }
}
