package com.jaredsburrows.license

import org.gradle.api.Plugin
import org.gradle.api.Project

/** A [Plugin] which grabs the POM.xml files from maven dependencies. */
class LicensePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.add("licenseReport", LicenseReportExtension::class.java)

    val javaPlugins = listOf(
      // JavaPlugin, also applies JavaBasePlugin,
      "java",
      // JavaLibraryPlugin, also applies JavaPlugin
      "java-library"
    )
    for (plugin in javaPlugins) {
      if (project.plugins.hasPlugin(plugin)) {
        project.configureJavaProject()
        return
      }
    }

    val androidPlugins = listOf(
      // AppPlugin
      "android",
      "com.android.application",
      // InstantAppPlugin
      "com.android.instantapp",
      // DynamicFeaturePlugin
      "com.android.dynamic-feature",
      // FeaturePlugin
      "com.android.feature",
      // LibraryPlugin
      "android-library",
      "com.android.library",
      // TestPlugin
      "com.android.test",
    )
    for (plugin in androidPlugins) {
      if (project.plugins.hasPlugin(plugin)) {
        project.configureAndroidProject()
        return
      }
    }

    throw UnsupportedOperationException("'com.jaredsburrows.license' requires  Java or Android Gradle Plugins.")
  }
}
