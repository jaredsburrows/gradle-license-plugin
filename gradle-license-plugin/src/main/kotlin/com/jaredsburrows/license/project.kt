package com.jaredsburrows.license

import org.gradle.api.Project

/** Returns true if plugin exists in project */
internal fun Project.hasPlugin(list: List<String>): Boolean {
  return list.find { project.plugins.hasPlugin(it) } != null
}
