package com.jaredsburrows.license

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet

/** Returns true if Java or Kotlin Gradle project. */
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
    // Ask the main source set for its configuration names rather than hardcoding them. The two it
    // reports are "compileClasspath" and "runtimeClasspath" -- main is the one source set Gradle
    // does not prefix -- so this changes no behaviour here. It is the same assumption that was
    // wrong on both other paths, though: an Android component named "androidMain" resolves
    // "androidCompileClasspath", and a Kotlin/Native target resolves "<target>CompileKlibraries"
    // and has no runtime configuration at all. Both reported nothing until they were asked.
    val main =
      extensions
        .getByType(JavaPluginExtension::class.java)
        .sourceSets
        .getByName(SourceSet.MAIN_SOURCE_SET_NAME)

    // Apply common task configuration first
    configureCommon(
      it,
      listOf(
        main.compileClasspathConfigurationName,
        main.runtimeClasspathConfigurationName,
      ),
    )
  }
}
