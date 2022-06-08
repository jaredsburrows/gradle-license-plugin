package com.jaredsburrows.license

import org.gradle.api.Project

/** Returns true if Java Gradle project. */
internal fun Project.isJavaProject(): Boolean {
  return hasPlugin(
    listOf(
      // JavaPlugin
      "java",
    )
  )
}

/**
 * Configure for Java projects.
 *
 * All of these plugins will apply the JavaPlugin(relies on JavaBasePlugin).
 */
internal fun Project.configureJavaProject() {
  tasks.register("licenseReport", LicenseReportTask::class.java) {
    // Apply common task configuration first
    configureCommon(it)
  }
}
