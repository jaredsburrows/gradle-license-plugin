package com.jaredsburrows.license

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project

final class LicensePlugin extends LicensePluginKt {
  /**
   * Configure for Java projects.
   */
  @Override protected void configureJavaProject(Project project) {
    String taskName = "licenseReport"
    String path = "${project.getBuildDir()}/reports/licenses/$taskName"
    LicenseReportExtension configuration = project.getExtensions().
      create("licenseReport", LicenseReportExtension)

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
  @Override protected void configureAndroidProject(Project project) {
    // Get correct plugin - Check for android library, default to application variant for application/test plugin
    DomainObjectCollection<BaseVariant> variants = getAndroidVariant(project)
    LicenseReportExtension configuration = project.getExtensions().
      create("licenseReport", LicenseReportExtension)

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
      task.setVariantName(variant.name)
      task.setProductFlavors(variant.productFlavors)
      // Make sure update on each run
      task.getOutputs().upToDateWhen { false }
    }
  }
}
