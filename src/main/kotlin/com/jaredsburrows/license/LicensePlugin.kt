package com.jaredsburrows.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.FeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import java.io.File
import kotlin.reflect.KClass
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPlugin

/** A [Plugin] which grabs the POM.xml files from maven dependencies. */
class LicensePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("licenseReport", LicenseReportExtension::class.java)

    project.plugins.all {
      when (it) {
        is JavaPlugin -> configureJavaProject(project, extension)
        is FeaturePlugin -> {
          project.extensions[FeatureExtension::class].run {
            configureAndroidProject(project, extension, featureVariants)
            configureAndroidProject(project, extension, libraryVariants)
          }
        }
        is LibraryPlugin -> {
          project.extensions[LibraryExtension::class].run {
            configureAndroidProject(project, extension, libraryVariants)
          }
        }
        is AppPlugin -> {
          project.extensions[AppExtension::class].run {
            configureAndroidProject(project, extension, applicationVariants)
          }
        }
      }
    }
  }

  /** Configure for Java projects. */
  private fun configureJavaProject(project: Project, extension: LicenseReportExtension) {
    val taskName = "licenseReport"
    val path = "${project.buildDir}/reports/licenses/$taskName".replace('/', File.separatorChar)

    // Create tasks
    project.tasks.create(taskName, LicenseReportTask::class.java).apply {
      description = "Outputs licenses report."
      group = "Reporting"
      htmlFile = File(path + LicenseReportTask.HTML_EXT)
      jsonFile = File(path + LicenseReportTask.JSON_EXT)
      generateHtmlReport = extension.generateHtmlReport
      generateJsonReport = extension.generateJsonReport
      copyHtmlReportToAssets = false
      copyJsonReportToAssets = false
    }
  }

  /** Configure for Android projects. */
  private fun configureAndroidProject(
    project: Project,
    extension: LicenseReportExtension,
    variants: DomainObjectSet<out BaseVariant>? = null
  ) {
    // Configure tasks for all variants
    variants?.all { variant ->
      val name = variant.name.capitalize()
      val taskName = "license${name}Report"
      val path = "${project.buildDir}/reports/licenses/$taskName".replace('/', File.separatorChar)

      // Create tasks based on variant
      project.tasks.create(taskName, LicenseReportTask::class.java).apply {
        description = "Outputs licenses report for $name variant."
        group = "Reporting"
        htmlFile = File(path + LicenseReportTask.HTML_EXT)
        jsonFile = File(path + LicenseReportTask.JSON_EXT)
        generateHtmlReport = extension.generateHtmlReport
        generateJsonReport = extension.generateJsonReport
        copyHtmlReportToAssets = extension.copyHtmlReportToAssets
        copyJsonReportToAssets = extension.copyJsonReportToAssets
        assetDirs = (
          project
            .extensions
            .getByName("android") as BaseExtension
          )
          .sourceSets
          .getByName("main")
          .assets
          .srcDirs
          .toList()
        buildType = variant.buildType.name
        variantName = variant.name
        productFlavors = variant.productFlavors
      }
    }
  }

  private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
    return getByType(type.java)
  }
}
