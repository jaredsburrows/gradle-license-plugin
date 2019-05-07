package com.jaredsburrows.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
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
import java.io.File

class LicensePlugin : Plugin<Project> {
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

  /**
   * Configure for Java projects.
   */
  private fun configureJavaProject(project: Project) {
    val taskName = "licenseReport"
    val path = "${project.buildDir}/reports/licenses/$taskName"
    val configuration = project.extensions
      .create("licenseReport", LicenseReportExtension::class.java)

    // Create tasks
    project.tasks.create(taskName, LicenseReportTask::class.java).apply {
      description = "Outputs licenses report."
      group = "Reporting"
      htmlFile = File(path + LicenseReportTask.HTML_EXT)
      jsonFile = File(path + LicenseReportTask.JSON_EXT)
      generateHtmlReport = configuration.generateHtmlReport
      generateJsonReport = configuration.generateJsonReport
      copyHtmlReportToAssets = false
      copyJsonReportToAssets = false
      // Make sure update on each run
      outputs.upToDateWhen { false }
    }
  }

  /**
   * Configure for Android projects.
   */
  private fun configureAndroidProject(project: Project) {
    // Get correct plugin - Check for android library, default to application variant for application/test plugin
    val variants = getAndroidVariant(project)
    val configuration = project.extensions
      .create("licenseReport", LicenseReportExtension::class.java)

    // Configure tasks for all variants
    variants?.all { variant ->
      val name = variant.name.capitalize()
      val taskName = "license${name}Report"
      val path = "${project.buildDir}/reports/licenses/$taskName"

      // Create tasks based on variant
      project.tasks.create(taskName, LicenseReportTask::class.java).apply {
        description = "Outputs licenses report for $name variant."
        group = "Reporting"
        htmlFile = File(path + LicenseReportTask.HTML_EXT)
        jsonFile = File(path + LicenseReportTask.JSON_EXT)
        generateHtmlReport = configuration.generateHtmlReport
        generateJsonReport = configuration.generateJsonReport
        copyHtmlReportToAssets = configuration.copyHtmlReportToAssets
        copyJsonReportToAssets = configuration.copyJsonReportToAssets
        assetDirs = (project
          .extensions
          .getByName("android") as BaseExtension)
          .sourceSets
          .getByName("main")
          .assets
          .srcDirs
          .toList()
        buildType = variant.buildType.name
        variantName = variant.name
        productFlavors = variant.productFlavors
        // Make sure update on each run
        outputs.upToDateWhen { false }
      }
    }
  }

  /**
   * Check for the android library plugin, default to application variants for applications and
   * test plugin.
   */
  private fun getAndroidVariant(
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
