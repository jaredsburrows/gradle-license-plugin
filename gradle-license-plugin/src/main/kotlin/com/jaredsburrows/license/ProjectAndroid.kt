package com.jaredsburrows.license

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.Component
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.TestAndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Project
import java.io.File
import java.util.Locale

/** Returns true if Android Gradle project. */
internal fun Project.isAndroidProject(): Boolean =
  hasPlugin(
    listOf(
      // AppPlugin
      "android",
      "com.android.application",
      // LibraryPlugin
      "android-library",
      "com.android.library",
      // TestPlugin
      "com.android.test",
    ),
  )

/**
 * Configure for Android projects using the modern Variant API
 * (`com.android.build.api.variant.AndroidComponentsExtension`) so the plugin works with both AGP 8
 * and AGP 9. AGP 9 removed the legacy variant accessors (applicationVariants / libraryVariants /
 * testVariants / unitTestVariants) from the `com.android.build.gradle.AppExtension` family.
 *
 * Plugins are matched by id (never by class), so AGP types are never referenced for non-Android
 * projects where AGP is not on the plugin classpath. The `onVariants` callback is registered during
 * configuration (not inside afterEvaluate), before AGP finalizes its variants.
 *
 * AppPlugin - "android", "com.android.application"
 * LibraryPlugin - "android-library", "com.android.library"
 * TestPlugin - "com.android.test"
 */
internal fun Project.configureAndroidProject() {
  // A project applies exactly one Android module plugin, but a single applied plugin can match
  // multiple ids (e.g. both "com.android.application" and the legacy "android" map to AppPlugin),
  // which would fire the callback twice. Guard so the variants are configured only once.
  var configured = false

  fun once(configure: () -> Unit) {
    if (!configured) {
      configured = true
      configure()
    }
  }
  plugins.withId("com.android.application") { once { configureApplicationVariants() } }
  plugins.withId("android") { once { configureApplicationVariants() } }
  plugins.withId("com.android.library") { once { configureLibraryVariants() } }
  plugins.withId("android-library") { once { configureLibraryVariants() } }
  plugins.withId("com.android.test") { once { configureTestVariants() } }
}

private fun Project.configureApplicationVariants() =
  extensions
    .getByType(ApplicationAndroidComponentsExtension::class.java)
    .run { onVariants(selector().all()) { configureVariant(it) } }

private fun Project.configureLibraryVariants() =
  extensions
    .getByType(LibraryAndroidComponentsExtension::class.java)
    .run { onVariants(selector().all()) { configureVariant(it) } }

private fun Project.configureTestVariants() =
  extensions
    .getByType(TestAndroidComponentsExtension::class.java)
    .run { onVariants(selector().all()) { configureVariant(it) } }

/**
 * Register a report task for [variant] and for each of its nested test components.
 *
 * [Variant.nestedComponents] is `@Incubating` but has been present and stable since AGP 7.x; it is
 * the version-stable way to reach unit/android/host/device test components across AGP 8 and AGP 9.
 */
@Suppress("UnstableApiUsage")
private fun Project.configureVariant(variant: Variant) {
  configureComponent(variant)
  variant.nestedComponents.forEach { configureComponent(it) }
}

private fun Project.configureComponent(component: Component) {
  val name =
    component.name.replaceFirstChar {
      if (it.isLowerCase()) {
        it.titlecase(Locale.getDefault())
      } else {
        it.toString()
      }
    }

  tasks.register("license${name}Report", LicenseReportTask::class.java) {
    // Apply common task configuration first
    configureCommon(
      it,
      listOf(
        "${component.name}CompileClasspath",
        "${component.name}RuntimeClasspath",
      ),
    )

    // Custom for Android tasks
    val sourceSetName = if (it.useVariantSpecificAssetDirs) component.name else "main"
    it.assetDirs = assetDirsForSourceSet(sourceSetName)
    it.variantName = component.name
  }
}

/**
 * Resolve the asset source directory for [sourceSetName] using AGP's default convention
 * (`src/<sourceSet>/assets`). This avoids depending on AGP-version-specific source-set DSL APIs and
 * therefore works uniformly across AGP 8 and AGP 9.
 */
private fun Project.assetDirsForSourceSet(sourceSetName: String): List<File> =
  listOf(layout.projectDirectory.dir("src/$sourceSetName/assets").asFile)
