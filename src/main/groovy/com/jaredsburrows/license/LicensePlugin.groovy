package com.jaredsburrows.license

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class LicensePlugin implements Plugin<Project> {
  @Override void apply(Project project) {
    // Only allow Android projects for now
    if (!(project.plugins.hasPlugin(LibraryPlugin) || project.plugins.hasPlugin(AppPlugin))) {
      throw new IllegalStateException("License report plugin can only be applied to android projects")
    }

    // Get correct plugin
    final def variants = (project.plugins.hasPlugin(AppPlugin)
      ? project.android.applicationVariants
      : project.android.libraryVariants)

    // Configure tasks for all variants
    variants.all { variant ->
      final def variantName = variant.name.capitalize()
      final def taskName = "license${variantName}Report"
      final def path = "${project.buildDir}/reports/licenses/$taskName"

      // Create tasks based on variant
      final def task = project.tasks.create("$taskName", LicenseReportTask)
      task.description = "Outputs licenses for ${variantName} variant."
      task.group = "Reporting"
      task.htmlFile = project.file(path + LicenseReportTask.HTML_EXT)
      task.jsonFile = project.file(path + LicenseReportTask.JSON_EXT)
      task.assetDirs = project.android.sourceSets.main.assets.srcDirs
      task.buildType = variant.buildType.name
      task.variant = variant.name
      task.productFlavors = variant.productFlavors
      task.outputs.upToDateWhen { false } // Make sure to not to use cache license file, update each run

      // Run task
      variant.assemble.doLast { task.licenseReport() }
    }
  }
}
