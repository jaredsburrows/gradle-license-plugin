package com.jaredsburrows.license

import org.gradle.api.Project

/** Returns true if a JetBrains Kotlin Multiplatform project. */
internal fun Project.isKmpProject(): Boolean = hasPlugin(listOf("org.jetbrains.kotlin.multiplatform"))

/**
 * Configure for Kotlin Multiplatform projects.
 *
 * A multiplatform project has no single classpath the way a JVM one does, so this registers a
 * report per target, the way the Android side registers one per variant: `licenseJvmReport`,
 * `licenseJsReport` and so on. The plugin is matched by id and the target names are read from the
 * `kotlin` extension reflectively, so the Kotlin Gradle Plugin is never on the compile classpath
 * and a non-multiplatform project never touches this.
 */
internal fun Project.configureKmpProject() {
  val kotlinExtension = extensions.findByName("kotlin") ?: return
  val targets =
    runCatching {
      @Suppress("UNCHECKED_CAST")
      val container = kotlinExtension.javaClass.getMethod("getTargets").invoke(kotlinExtension) as Iterable<Any>
      container.mapNotNull { target ->
        target.javaClass.methods
          .firstOrNull { it.name == "getName" && it.parameterCount == 0 }
          ?.invoke(target) as? String
      }
    }.getOrDefault(emptyList())

  targets
    // "metadata" is the common source set, which resolves no runtime dependencies of its own.
    .filterNot { it == METADATA_TARGET }
    .forEach { target ->
      val name = target.replaceFirstChar { it.uppercase() }
      tasks.register("license${name}Report", LicenseReportTask::class.java) {
        configureCommon(
          it,
          listOf(
            "${target}CompileClasspath",
            "${target}RuntimeClasspath",
          ),
        )
      }
    }
}

private const val METADATA_TARGET = "metadata"
