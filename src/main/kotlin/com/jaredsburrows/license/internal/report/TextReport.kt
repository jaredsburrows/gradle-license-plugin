package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.Project

/**
 * Generates CSV report of projects dependencies.
 *
 * @property projects list of [Project]s for thr CSV report.
 */
class TextReport(private val projects: List<Project>) : Report {

  override fun toString(): String = report()

  override fun report(): String = if (projects.isEmpty()) emptyReport() else fullReport()

  override fun fullReport(): String {
    val projectInfoList = mutableListOf<String>()
    projectInfoList.add("Notice for packages")
    projectInfoList.add("\n")

    projects.map { project ->
      val projectInfo = mutableListOf<String?>().apply {

        // Project Name
        if (project.name.isNotEmpty()) {
          add("${project.name} ")

          // Project Version
          if (project.version.isNotEmpty()) {
            add("(${project.version}) ")
          }

          // Project License Names
          if (project.licenses.isNotEmpty()) {
            add("- ")
            add(project.licenses) { it.name }
          }

          add("\n")

          // Add Description
          if (project.description.isNotEmpty()) {
            add(project.description)
            add("\n")
          }

          // Project Url
          if (project.url.isNotEmpty()) {
            add(project.url)
            add("\n")
          }
        }
      }

      // Add each row to the list
      projectInfoList.add(projectInfo.joinToString(separator = ""))
    }

    // Separate each record with a new line
    return projectInfoList.joinToString(separator = "\n")
  }

  override fun emptyReport(): String = EMPTY_TEXT

  /** Add List of elements to as comma separated list with quotes. */
  private fun <T> MutableList<String?>.add(
    elements: List<T>,
    transform: ((T) -> CharSequence)? = null
  ): Boolean {
    return when {
      elements.isEmpty() -> this.add("")
      else -> {
        val element = elements.joinToString(separator = ",", transform = transform)
        when (elements.size) {
          1 -> this.add(element)
          else -> this.add("\"${element}\"")
        }
      }
    }
  }

  private companion object {
    private const val EMPTY_TEXT = ""
  }
}
