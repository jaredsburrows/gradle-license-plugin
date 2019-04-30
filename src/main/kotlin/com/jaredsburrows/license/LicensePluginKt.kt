package com.jaredsburrows.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.FeaturePlugin
import com.android.build.gradle.InstantAppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

abstract class LicensePluginKt : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.all { plugin ->
      when (plugin) {
        is JavaPlugin -> {
          configureJavaProject(project)
        }
        is AppPlugin,
        is FeaturePlugin,
        is LibraryPlugin,
        is InstantAppPlugin,
        is TestPlugin -> {
          configureAndroidProject(project)
        }
      }
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
