package com.jaredsburrows.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.FeatureExtension
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
        is JavaPlugin -> configureJavaProject(project)
        is AppPlugin,
        is FeaturePlugin,
        is LibraryPlugin,
        is InstantAppPlugin,
        is TestPlugin -> configureAndroidProject(project)
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
    return when {
      project.plugins.hasPlugin(AppPlugin::class.java) -> project.extensions
        .findByType(AppExtension::class.java)
        ?.applicationVariants
      project.plugins.hasPlugin(FeaturePlugin::class.java) -> project.extensions
        .findByType(FeatureExtension::class.java)
        ?.featureVariants
      project.plugins.hasPlugin(LibraryPlugin::class.java) -> project.extensions
        .findByType(LibraryExtension::class.java)
        ?.libraryVariants
      project.plugins.hasPlugin(TestPlugin::class.java) -> project.extensions
        .findByType(TestExtension::class.java)
        ?.applicationVariants
      else -> throw IllegalArgumentException("Missing the Android Gradle Plugin.")
    }
  }
}
