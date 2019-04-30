package com.jaredsburrows.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class LicensePluginKt : Plugin<Project> {
  companion object {
    /** Handles pre-3.0 and 3.0+, "com.android.base" was added in AGP 3.0 */
    private val ANDROID_IDS = arrayOf(
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
  ): DomainObjectCollection<out BaseVariant>? {
    if (project.plugins.hasPlugin("com.android.application")) {
      return project.extensions
        .findByType(AppExtension::class.java)
        ?.applicationVariants
    }

    if (project.plugins.hasPlugin("com.android.test")) {
      return project.extensions
        .findByType(TestExtension::class.java)
        ?.applicationVariants
    }

    if (project.plugins.hasPlugin("com.android.library")) {
      return project.extensions
        .findByType(LibraryExtension::class.java)
        ?.libraryVariants
    }

    throw IllegalArgumentException("Missing the Android Gradle Plugin.")
  }
}
