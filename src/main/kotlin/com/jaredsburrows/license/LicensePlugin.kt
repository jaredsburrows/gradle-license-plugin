package com.jaredsburrows.license

import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project

class LicensePlugin : Plugin<Project> {
  companion object {
    // Handles pre-3.0 and 3.0+, "com.android.base" was added in AGP 3.0
    private val ANDROID_IDS = arrayOf(
      "com.android.application",
      "com.android.feature",
      "com.android.instantapp",
      "com.android.library",
      "com.android.test")
  }

  override fun apply(project: Project) {
    project.extensions.create("licenseReport", LicenseReportExtension::class.java)

    project.plugins.withId("java") { configureJavaProject(project) }

    ANDROID_IDS.forEach { id ->
      project.plugins.withId(id) { configureAndroidProject(project) }
    }
  }

  /**
   * Configure for Java projects.
   */
  private fun configureJavaProject(project: Project) {
    val taskName = "licenseReport"
    val path = "${project.buildDir}/reports/licenses/$taskName"
    val configuration = project.extensions.getByType(LicenseReportExtension::class.java)

    // Create tasks
    project.tasks.create(taskName, LicenseReportTask::class.java).apply {
      description = "Outputs licenses report."
      group = "Reporting"
      htmlFile = project.file(path + LicenseReportTask.HTML_EXT)
      jsonFile = project.file(path + LicenseReportTask.JSON_EXT)
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
    val configuration = project.extensions.getByType(LicenseReportExtension::class.java)

    // Configure tasks for all variants
    variants?.all { variant ->
      val variantName = variant.name.capitalize()
      val taskName = "license${variantName}Report"
      val path = "${project.buildDir}/reports/licenses/$taskName"

      // Create tasks based on variant
      project.tasks.create(taskName, LicenseReportTask::class.java).apply {
        description = "Outputs licenses report for $variantName variant."
        group = "Reporting"
        htmlFile = project.file(path + LicenseReportTask.HTML_EXT)
        jsonFile = project.file(path + LicenseReportTask.JSON_EXT)
        generateHtmlReport = configuration.generateHtmlReport
        generateJsonReport = configuration.generateJsonReport
        copyHtmlReportToAssets = configuration.copyHtmlReportToAssets
        copyJsonReportToAssets = configuration.copyJsonReportToAssets
        // assetDirs = project.android.sourceSets.main.assets.srcDirs
        buildType = variant.buildType.name
        // variant = variant.name
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
