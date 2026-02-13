package com.jaredsburrows.license.internal.report

import org.apache.maven.model.License
import org.apache.maven.model.Model

/**
 * Generates Text report of projects dependencies.
 *
 * @property projects list of [Model]s for thr Text report.
 */
class TextReport(
  private val projects: List<Model>,
) : Report {
  override fun toString(): String = report()

  override fun name(): String = NAME

  override fun extension(): String = EXTENSION

  override fun report(): String = if (projects.isEmpty()) emptyReport() else fullReport()

  override fun fullReport(): String {
    val projectInfoList = mutableListOf<String>()
    projectInfoList += "Notice for packages"
    projectInfoList += "\n"

    projects.map { project ->
      val projectInfo = mutableListOf<String>()

      // If no name return early
      if (project.name.isEmpty()) return@map

      // Project Name (1.0) - License Name
      val firstLine =
        when {
          project.name.isNotEmpty() && project.version.isNotEmpty() && project.licenses.isNotEmpty() ->
            "${project.name} (${project.version}) - ${project.licenses.licenseNames()}"

          project.name.isNotEmpty() && project.licenses.isNotEmpty() ->
            "${project.name} - ${project.licenses.licenseNames()}"

          project.name.isNotEmpty() && project.version.isNotEmpty() ->
            "${project.name} (${project.version})"

          project.name.isNotEmpty() ->
            project.name

          else -> return@map
        }

      // Project Description
      val secondLine =
        when {
          project.description.isNotEmpty() -> project.description
          else -> ""
        }

      // Project Url
      val thirdLine =
        when {
          project.url.isNotEmpty() -> project.url
          else -> ""
        }

      // File format
      val text =
        when {
          firstLine.isNotEmpty() && secondLine.isNotEmpty() && thirdLine.isNotEmpty() ->
            """
          $firstLine
          $secondLine
          $thirdLine

          """

          firstLine.isNotEmpty() && secondLine.isNotEmpty() ->
            """
          $firstLine
          $secondLine

          """

          firstLine.isNotEmpty() && thirdLine.isNotEmpty() ->
            """
          $firstLine
          $thirdLine

          """

          else ->
            """
          $firstLine

          """
        }.trimIndent()

      projectInfo += text

      // Add each license to the list
      projectInfoList += projectInfo.joinToString(separator = "")
    }

    // Separate each record with a new line
    return projectInfoList.joinToString(separator = "\n")
  }

  override fun emptyReport(): String = EMPTY_TEXT

  private fun List<License>.licenseNames(): String = this.joinToString(separator = ",", transform = { it.name })

  private companion object {
    private const val EXTENSION = "txt"
    private const val NAME = "Text"
    private const val EMPTY_TEXT = ""
  }
}
