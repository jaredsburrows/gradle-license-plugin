package com.jaredsburrows.license

import org.gradle.api.Project

/** Returns true if Java Gradle project */
internal fun Project.isJavaProject(): Boolean {
  return hasPlugin(
    listOf(
      // ApplicationPlugin
      "application",
      // GroovyPlugin
      "groovy",
      // JavaPlugin
      "java",
      // JavaGradlePluginPlugin
      "java-gradle-plugin",
      // JavaLibraryPlugin
      "java-library",
      // ScalaPlugin
      "scala",
    )
  )
}

/**
 * Configure for Java projects.
 *
 * ApplicationPlugin - "application" - also applies JavaPlugin
 * GroovyPlugin - "groovy" - also applies JavaPlugin
 * JavaPlugin - "java" - also applies JavaBasePlugin
 * JavaGradlePluginPlugin - "java-gradle-plugin" - also applies JavaPlugin
 * JavaLibraryPlugin - "java-library" - also applies JavaPlugin
 * ScalaPlugin - "scala" - also applies JavaPlugin
 */
internal fun Project.configureJavaProject() {
  tasks.register("licenseReport", LicenseReportTask::class.java)
}
