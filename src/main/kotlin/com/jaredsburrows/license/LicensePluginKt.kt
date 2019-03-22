package com.jaredsburrows.license

import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class LicensePluginKt : Plugin<Project> {
  companion object {
    /** Handles pre-3.0 and 3.0+, "com.android.base" was added in AGP 3.0 */
    @JvmStatic val ANDROID_IDS = arrayOf(
      "com.android.application",
      "com.android.feature",
      "com.android.instantapp",
      "com.android.library",
      "com.android.test")
  }

  override fun apply(project: Project) {
    project.plugins.withId("java") { configureJavaProject(project) }

    ANDROID_IDS.forEach { id ->
      project.plugins.withId(id) { configureAndroidProject(project) }
    }
  }

  protected abstract fun configureJavaProject(project: Project)

  protected abstract fun configureAndroidProject(project: Project)

  /**
   * Check for the android library plugin, default to application variants for applications and
   * test plugin.
   */
  protected fun getAndroidVariant(
    project: Project
  ): DomainObjectCollection<out com.android.build.gradle.api.BaseVariant>? {
    if (project.plugins.hasPlugin("com.android.application")) {
      return project.extensions
        .findByType(com.android.build.gradle.AppExtension::class.java)
        ?.applicationVariants
    }

    if (project.plugins.hasPlugin("com.android.test")) {
      return project.extensions
        .findByType(com.android.build.gradle.TestExtension::class.java)
        ?.applicationVariants
    }

    if (project.plugins.hasPlugin("com.android.library")) {
      return project.extensions
        .findByType(com.android.build.gradle.LibraryExtension::class.java)
        ?.libraryVariants
    }

    throw IllegalArgumentException("Missing the Android Gradle Plugin.")
  }
}
