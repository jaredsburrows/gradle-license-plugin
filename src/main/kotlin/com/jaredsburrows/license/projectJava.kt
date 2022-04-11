package com.jaredsburrows.license

import org.gradle.api.Project

/** Returns true if Java Gradle project */
internal fun Project.isJavaProject(): Boolean {
  return hasPlugin(
    listOf(
      "application", // JavaApplicationPlugin
      "groovy", // GroovyPlugin
      "java", // JavaPlugin, also applies JavaBasePlugin
      "java-library", // JavaLibraryPlugin, also applies JavaPlugin
      "scala", // ScalaPlugin
    )
  )
}

/**
 * Configure for Java projects.
 *
 * JavaPlugin - "java" - also applies JavaBasePlugin
 * JavaLibraryPlugin - "java-library" - also applies JavaPlugin
 */
internal fun Project.configureJavaProject() {
  tasks.register("licenseReport", LicenseReportTask::class.java)
}
