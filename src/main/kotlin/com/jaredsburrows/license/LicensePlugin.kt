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
        is JavaPlugin -> project.configureJavaProject()
        is FeaturePlugin -> {
          project.extensions.getByType(FeatureExtension::class.java).run {
            project.configureAndroidProject(featureVariants)
            project.configureAndroidProject(libraryVariants)
          }
        }
        is LibraryPlugin -> {
          project.extensions.getByType(LibraryExtension::class.java).run {
            project.configureAndroidProject(libraryVariants)
          }
        }
        is AppPlugin -> {
          project.extensions.getByType(AppExtension::class.java).run {
            project.configureAndroidProject(applicationVariants)
          }
        }
      }
    }
  }
}

/** Configure for Java projects. */
private fun Project.configureJavaProject() {
  tasks.register("licenseReport", LicenseReportTask::class.java)
}

/** Configure for Android projects. */
private fun Project.configureAndroidProject(variants: DomainObjectSet<out BaseVariant>? = null) {
  // Configure tasks for all variants
  variants?.all { variant ->
    val name = variant.name.replaceFirstChar {
      if (it.isLowerCase()) {
        it.titlecase(Locale.getDefault())
      } else {
        it.toString()
      }
    }

    tasks.register("license${name}Report", LicenseReportTask::class.java) {
      it.assetDirs = (extensions.getByName("android") as BaseExtension)
        .sourceSets
        .getByName("main")
        .assets
        .srcDirs
        .toList()
      it.variantName = variant.name
    }
  }
}
