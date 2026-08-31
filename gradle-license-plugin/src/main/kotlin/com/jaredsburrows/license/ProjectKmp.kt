package com.jaredsburrows.license

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

/** Returns true if a JetBrains Kotlin Multiplatform project. */
internal fun Project.isKmpProject(): Boolean = hasPlugin(listOf("org.jetbrains.kotlin.multiplatform"))

/**
 * Configure for Kotlin Multiplatform projects.
 *
 * A multiplatform project has no single classpath the way a JVM one does, so this registers a
 * report per target, the way the Android side registers one per variant: `licenseJvmReport`,
 * `licenseJsReport` and so on.
 *
 * The Kotlin Gradle Plugin is `compileOnly`, exactly as AGP is for [configureAndroidProject], and
 * the plugin is matched by id rather than by class, so these types are never loaded for a project
 * that is not multiplatform.
 */
internal fun Project.configureKmpProject() {
  // Declared inside the function, not at file level. A file-level `val` is initialized in this
  // file's facade class, which also carries [isKmpProject] -- so every project, multiplatform or
  // not, would resolve KotlinPlatformType on the way to asking whether it is multiplatform, and
  // fail with NoClassDefFoundError wherever the Kotlin Gradle Plugin is absent. It is compileOnly.
  //
  // Targets deliberately left alone, matched by platform type rather than by target name so a
  // rename cannot silently turn the filter off:
  //
  // - common is the metadata target, which resolves no dependencies of its own.
  // - androidJvm belongs to [configureAndroidProject]. An android target only exists when an
  //   Android plugin is applied, and that path reports it per variant and wires up the asset
  //   directories, which this one cannot. Both resolve the same configurations, so registering
  //   here as well would produce a second task with identical output under a different name.
  val skippedPlatformTypes = setOf(KotlinPlatformType.common, KotlinPlatformType.androidJvm)

  extensions
    .getByType(KotlinMultiplatformExtension::class.java)
    .targets
    .filterNot { it.platformType in skippedPlatformTypes }
    .forEach { target ->
      val name = target.name.replaceFirstChar { it.uppercase() }

      tasks.register("license${name}Report", LicenseReportTask::class.java) {
        // Ask the target for its configuration names rather than assembling them from the target
        // name: only the JVM-like targets are "<target>CompileClasspath". A native target compiles
        // against "<target>CompileKlibraries" and has no runtime configuration at all, so building
        // the names by hand asked for configurations that do not exist and reported nothing.
        val main = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

        configureCommon(
          it,
          listOfNotNull(
            main.compileDependencyConfigurationName,
            main.runtimeDependencyConfigurationName,
          ),
        )
      }
    }
}
