package com.jaredsburrows.license

import org.gradle.api.Plugin
import org.gradle.api.Project

final class LicensePlugin implements Plugin<Project> {
  // Handles pre-3.0 and 3.0+, "com.android.base" was added in AGP 3.0
  private static final String[] ANDROID_IDS = [
    "com.android.application",
    "com.android.feature",
    "com.android.instantapp",
    "com.android.library",
    "com.android.test"]

  @Override public void apply(Project project) {
    project.getPlugins().withId("java", { configureJavaProject(project) })

    ANDROID_IDS.each { id ->
      project.getPlugins().withId(id, { configureAndroidProject(project) })
    }
  }

  /**
   * Configure for Java projects.
   */
  private static void configureJavaProject(Project project) {
    String taskName = "licenseReport"
    String path = "${project.getBuildDir()}/reports/licenses/$taskName"
    LicenseReportExtension configuration = project.getExtensions().create("licenseReport", LicenseReportExtension)

    // Create tasks
    LicenseReportTask task = project.getTasks().create("$taskName", LicenseReportTask)
    task.setDescription("Outputs licenses report.")
    task.setGroup("Reporting")
    task.setHtmlFile(project.file(path + LicenseReportTask.HTML_EXT))
    task.setJsonFile(project.file(path + LicenseReportTask.JSON_EXT))
    task.setGenerateHtmlReport(configuration.getGenerateHtmlReport())
    task.setGenerateJsonReport(configuration.getGenerateJsonReport())
    task.setCopyHtmlReportToAssets(false)
    task.setCopyJsonReportToAssets(false)
    // Make sure update on each run
    task.getOutputs().upToDateWhen { false }
  }

  /**
   * Configure for Android projects.
   */
  private static void configureAndroidProject(Project project) {
    // Get correct plugin - Check for android library, default to application variant for application/test plugin
    def variants = getAndroidVariants(project)
    LicenseReportExtension configuration = project.getExtensions().create("licenseReport", LicenseReportExtension)

    // Configure tasks for all variants
    variants.all { variant ->
      String variantName = variant.name.capitalize()
      String taskName = "license${variantName}Report"
      String path = "${project.getBuildDir()}/reports/licenses/$taskName"

      // Create tasks based on variant
      LicenseReportTask task = project.getTasks().create("$taskName", LicenseReportTask)
      task.setDescription("Outputs licenses report for ${variantName} variant.")
      task.setGroup("Reporting")
      task.setHtmlFile(project.file(path + LicenseReportTask.HTML_EXT))
      task.setJsonFile(project.file(path + LicenseReportTask.JSON_EXT))
      task.setGenerateHtmlReport(configuration.getGenerateHtmlReport())
      task.setGenerateJsonReport(configuration.getGenerateJsonReport())
      task.setCopyHtmlReportToAssets(configuration.getCopyHtmlReportToAssets())
      task.setCopyJsonReportToAssets(configuration.getCopyJsonReportToAssets())
      task.assetDirs = project.android.sourceSets.main.assets.srcDirs
      task.setBuildType(variant.buildType.name)
      task.setVariant(variant.name)
      task.setProductFlavors(variant.productFlavors)
      // Make sure update on each run
      task.getOutputs().upToDateWhen { false }
    }
  }

  /**
   * Check for the android library plugin, default to application variants for applications and test plugin.
   */
  private static getAndroidVariants(Project project) {
    return project.android.hasProperty("libraryVariants")
      ? project.android.libraryVariants
      : project.android.applicationVariants
  }
}
