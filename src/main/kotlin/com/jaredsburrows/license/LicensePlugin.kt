package com.jaredsburrows.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.FeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import java.util.Locale

/** A [Plugin] which grabs the POM.xml files from maven dependencies. */
class LicensePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.add("licenseReport", LicenseReportExtension::class.java)

    project.plugins.all {
      when (it) {
        is JavaPlugin -> configureJavaProject(project)
        is FeaturePlugin -> {
          project.extensions.getByType(FeatureExtension::class.java).run {
            configureAndroidProject(project)
            configureAndroidProject(project)
          }
        }
        is LibraryPlugin -> {
          project.extensions.getByType(LibraryExtension::class.java).run {
            configureAndroidProject(project)
          }
        }
        is AppPlugin -> {
          project.extensions.getByType(AppExtension::class.java).run {
            configureAndroidProject(project)
          }
        }
      }
    }
  }

  /** Configure for Java projects. */
  private fun configureJavaProject(project: Project) {
    project.tasks.register("licenseReport", LicenseReportTask::class.java)
  }

  /** Configure for Android projects. */
  private fun configureAndroidProject(
    project: Project,
    variants: DomainObjectSet<out BaseVariant>? = null
  ) {
    // Configure tasks for all variants
    variants?.all { variant ->
      val name = variant.name.replaceFirstChar {
        if (it.isLowerCase()) {
          it.titlecase(Locale.getDefault())
        } else {
          it.toString()
        }
      }

      // Create tasks based on variant
      project.tasks.register("license${name}Report", LicenseReportTask::class.java) {
        it.assetDirs = (project.extensions.getByName("android") as BaseExtension)
          .sourceSets
          .getByName("main")
          .assets
          .srcDirs
          .toList()
        it.buildType = variant.buildType.name
        it.variantName = variant.name
        it.productFlavors = variant.productFlavors
      }
    }
  }
}
