package com.jaredsburrows.license

import org.gradle.api.Project

/** Returns true if a plain JVM Gradle project: Java, or Kotlin targeting the JVM. */
internal fun Project.isJvmProject(): Boolean =
  hasPlugin(
    listOf(
      // JavaPlugin
      "java",
      // KotlinJvmPlugin
      "org.jetbrains.kotlin.jvm",
    ),
  )

/** Configure for JVM projects, which produce a single artifact and so a single report. */
internal fun Project.configureJvmProject() {
  tasks.register("licenseReport", LicenseReportTask::class.java) {
    // Apply common task configuration first
    configureCommon(it, listOf("compileClasspath", "runtimeClasspath"))
  }
}
