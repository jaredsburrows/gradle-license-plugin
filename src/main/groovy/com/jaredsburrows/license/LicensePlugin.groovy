package com.jaredsburrows.license

import org.gradle.api.Plugin
import org.gradle.api.Project

final class LicensePlugin implements Plugin<Project> {
  // Handles pre-3.0 and 3.0+, "com.android.base" was added in AGP 3.0
  private static final def ANDROID_IDS = [
    "com.android.application",
    "com.android.feature",
    "com.android.instantapp",
    "com.android.library",
    "com.android.test"]

  @Override void apply(Project project) {
    project.plugins.withId("java") { configureJavaProject(project) }

    ANDROID_IDS.each { id ->
      project.plugins.withId(id) { configureAndroidProject(project) }
    }
  }

  /**
   * Configure for Java projects.
   */
  private static configureJavaProject(def project) {
    def taskName = "licenseReport"
    def path = "${project.buildDir}/reports/licenses/$taskName"
    def configuration = project.extensions.create("licenseReport", LicenseReportExtension)

    // Create tasks
    LicenseReportTask task = project.tasks.create("$taskName", LicenseReportTask)
    task.description = "Outputs licenses report."
    task.group = "Reporting"
    task.htmlFile = project.file(path + LicenseReportTask.HTML_EXT)
    task.jsonFile = project.file(path + LicenseReportTask.JSON_EXT)
    task.generateHtmlReport = configuration.generateHtmlReport
    task.generateJsonReport = configuration.generateJsonReport
    task.copyHtmlReportToAssets = false
    task.copyJsonReportToAssets = false
    // Make sure update on each run
    task.outputs.upToDateWhen { false }
  }

  /**
   * Configure for Android projects.
   */
  private static configureAndroidProject(def project) {
    // Get correct plugin - Check for android library, default to application variant for application/test plugin
    def variants = getAndroidVariants(project)
    def configuration = project.extensions.create("licenseReport", LicenseReportExtension)

    // Configure tasks for all variants
    variants.all { variant ->
      def variantName = variant.name.capitalize()
      def taskName = "license${variantName}Report"
      def path = "${project.buildDir}/reports/licenses/$taskName"

      // Create tasks based on variant
      LicenseReportTask task = project.tasks.create("$taskName", LicenseReportTask)
      task.description = "Outputs licenses report for ${variantName} variant."
      task.group = "Reporting"
      task.htmlFile = project.file(path + LicenseReportTask.HTML_EXT)
      task.jsonFile = project.file(path + LicenseReportTask.JSON_EXT)
      task.generateHtmlReport = configuration.generateHtmlReport
      task.generateJsonReport = configuration.generateJsonReport
      task.copyHtmlReportToAssets = configuration.copyHtmlReportToAssets
      task.copyJsonReportToAssets = configuration.copyJsonReportToAssets
      task.assetDirs = project.android.sourceSets.main.assets.srcDirs
      task.buildType = variant.buildType.name
      task.variant = variant.name
      task.productFlavors = variant.productFlavors
      // Make sure update on each run
      task.outputs.upToDateWhen { false }
    }
  }

  /**
   * Check for the android library plugin, default to application variants for applications and test plugin.
   */
  private static getAndroidVariants(def project) {
    (project.android.hasProperty("libraryVariants")
      ? project.android.libraryVariants
      : project.android.applicationVariants)
  }
}
