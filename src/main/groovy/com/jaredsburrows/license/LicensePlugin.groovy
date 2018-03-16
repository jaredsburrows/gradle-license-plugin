package com.jaredsburrows.license

import org.gradle.api.Plugin
import org.gradle.api.Project

final class LicensePlugin implements Plugin<Project> {
  final static def ANDROID_PLUGINS = ["com.android.application", "com.android.library", "com.android.test"]
  final static def JVM_PLUGINS = ["groovy", "java", "java-library"]

  @Override void apply(Project project) {
    if (isAndroidProject(project)) {
      configureAndroidProject(project)
    } else if (isJavaProject(project)) {
      configureJavaProject(project)
    } else {
      throw new IllegalStateException(
        "License report plugin can only be applied to android or java projects.")
    }
  }

  /**
   * Configure project and all variants for Android.
   */
  private static configureAndroidProject(project) {
    // Get correct plugin - Check for android library, default to application variant for application/test plugin
    final def variants = getAndroidVariants(project)
    final def configuration = project.extensions.create("licenseReport", LicenseReportExtension)

    // Configure tasks for all variants
    variants.all { variant ->
      final def variantName = variant.name.capitalize()
      final def taskName = "license${variantName}Report"
      final def path = "${project.buildDir}/reports/licenses/$taskName"

      // Create tasks based on variant
      final LicenseReportTask task = project.tasks.create("$taskName", LicenseReportTask)
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
   * Configure project for Groovy/Java.
   */
  private static configureJavaProject(project) {
    final def taskName = "licenseReport"
    final def path = "${project.buildDir}/reports/licenses/$taskName"
    final def configuration = project.extensions.create("licenseReport", LicenseReportExtension)

    // Create tasks
    final LicenseReportTask task = project.tasks.create("$taskName", LicenseReportTask)
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
   * Check for the android library plugin, default to application variants for applications and test plugin.
   */
  private static getAndroidVariants(project) {
    (project.android.hasProperty("libraryVariants")
      ? project.android.libraryVariants
      : project.android.applicationVariants)
  }

  /**
   * Check if the project has Android plugins.
   */
  private static isAndroidProject(project) {
    ANDROID_PLUGINS.find { plugin -> project.plugins.hasPlugin(plugin) }
  }

  /**
   * Check if project has Java plugins.
   */
  private static isJavaProject(project) {
    JVM_PLUGINS.find { plugin -> project.plugins.hasPlugin(plugin) }
  }
}
