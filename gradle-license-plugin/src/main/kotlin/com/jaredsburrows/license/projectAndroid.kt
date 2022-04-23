package com.jaredsburrows.license

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.FeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import java.util.Locale

/** Returns true if Android Gradle project */
internal fun Project.isAndroidProject(): Boolean {
  return hasPlugin(
    listOf(
      // AppPlugin
      "android",
      "com.android.application",
      // FeaturePlugin
      "com.android.feature",
      // LibraryPlugin
      "android-library",
      "com.android.library",
      // TestPlugin
      "com.android.test",
    )
  )
}

/**
 * Configure for Android projects.
 *
 * AppPlugin - "android", "com.android.application"
 * FeaturePlugin - "com.android.feature"
 * LibraryPlugin - "android-library", "com.android.library"
 * TestPlugin - "com.android.test"
 */
internal fun Project.configureAndroidProject() {
  project.plugins.all {
    when (it) {
      is AppPlugin -> {
        project.extensions.getByType(AppExtension::class.java).run {
          configureVariant(applicationVariants)
          configureVariant(testVariants)
          configureVariant(unitTestVariants)
        }
      }
      is FeaturePlugin -> {
        project.extensions.getByType(FeatureExtension::class.java).run {
          configureVariant(featureVariants)
          configureVariant(libraryVariants)
          configureVariant(testVariants)
          configureVariant(unitTestVariants)
        }
      }
      is LibraryPlugin -> {
        project.extensions.getByType(LibraryExtension::class.java).run {
          configureVariant(libraryVariants)
          configureVariant(testVariants)
          configureVariant(unitTestVariants)
        }
      }
      is TestPlugin -> {
        project.extensions.getByType(TestExtension::class.java).run {
          configureVariant(applicationVariants)
        }
      }
    }
  }
}

private fun Project.configureVariant(variants: DomainObjectSet<out BaseVariant>? = null) {
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
