package com.jaredsburrows.license

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

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
  extensions
    .getByType(KotlinMultiplatformExtension::class.java)
    .targets
    // The common target resolves no runtime dependencies of its own.
    .filterNot { it.name == METADATA_TARGET }
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

private const val METADATA_TARGET = "metadata"
