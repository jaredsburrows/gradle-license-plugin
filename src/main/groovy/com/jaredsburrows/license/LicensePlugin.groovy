package com.jaredsburrows.license

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicensePlugin implements Plugin<Project> {
  final static def ANDROID_APPLICATION_PLUGIN = "com.android.application"
  final static def ANDROID_LIBRARY_PLUGIN = "com.android.library"
  final static def ANDROID_TEST_PLUGIN = "com.android.test"
  final static def GROOVY_PLUGIN = "groovy"
  final static def JAVA_PLUGIN = "java"

  @Override void apply(Project project) {
    project.evaluationDependsOnChildren()

    if (isAndroidProject(project)) configureAndroidProject project
    else if (isJavaProject(project)) configureJavaProject project
    else throw new IllegalStateException("License report plugin can only be applied to android or java projects.")
  }

  /**
   * Configure project and all variants for Android.
   */
  static def configureAndroidProject(project) {
    // Get correct plugin - Check for android library, default to application variant for application/test plugin
    final def variants = getAndroidVariants project

    // Configure tasks for all variants
    variants.all { variant ->
      final def variantName = variant.name.capitalize()
      final def taskName = "license${variantName}Report"
      final def path = "${project.buildDir}/reports/licenses/$taskName"

      // Create tasks based on variant
      final LicenseReportTask task = project.tasks.create "$taskName", LicenseReportTask
      task.description = "Outputs licenses report for ${variantName} variant."
      task.group = "Reporting"
      task.htmlFile = project.file path + LicenseReportTask.HTML_EXT
      task.jsonFile = project.file path + LicenseReportTask.JSON_EXT
      task.assetDirs = project.android.sourceSets.main.assets.srcDirs
      task.buildType = variant.buildType.name
      task.variant = variant.name
      task.productFlavors = variant.productFlavors
      task.outputs.upToDateWhen { false } // Make sure to not to use cache license file, update each run
    }
  }

  /**
   * Configure project for Groovy/Java.
   */
  static def configureJavaProject(project) {
    final def taskName = "licenseReport"
    final def path = "${project.buildDir}/reports/licenses/$taskName"

    // Create tasks
    final LicenseReportTask task = project.tasks.create "$taskName", LicenseReportTask
    task.description = "Outputs licenses report."
    task.group = "Reporting"
    task.htmlFile = project.file path + LicenseReportTask.HTML_EXT
    task.jsonFile = project.file path + LicenseReportTask.JSON_EXT
    task.outputs.upToDateWhen { false } // Make sure to not to use cache license file, update each run
  }

  /**
   * Get correct plugin - Check for the android library plugin, default to application variants for applications and
   * test plugin.
   */
  static def getAndroidVariants(project) {
    (project.plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN)
      ? project.android.libraryVariants
      : project.android.applicationVariants)
  }

  /**
   * Check if the project has Android plugins.
   */
  static def isAndroidProject(project) {
    (project.plugins.hasPlugin(ANDROID_APPLICATION_PLUGIN)
      || project.plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN)
      || project.plugins.hasPlugin(ANDROID_TEST_PLUGIN))
  }

  /**
   * Check if project has Java plugins.
   */
  static def isJavaProject(project) {
    (project.plugins.hasPlugin(GROOVY_PLUGIN)
      || project.plugins.hasPlugin(JAVA_PLUGIN))
  }
}
