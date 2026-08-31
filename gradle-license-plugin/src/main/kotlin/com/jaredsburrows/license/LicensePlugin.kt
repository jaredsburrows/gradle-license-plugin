package com.jaredsburrows.license

import org.gradle.api.Plugin
import org.gradle.api.Project

/** A [Plugin] which grabs the POM.xml files from maven dependencies. */
class LicensePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.add("licenseReport", LicenseReportExtension::class.java)

    // Android must be wired during configuration: the modern Variant API (onVariants) has to be
    // registered before AGP finalizes its variants, so it cannot wait until afterEvaluate. This
    // only reacts to Android plugins (by id) and is otherwise a no-op.
    project.configureAndroidProject()

    // Java/Kotlin support and the "unsupported project" error depend on the final set of applied
    // plugins, which is only known after evaluation. Android was already handled above, so here we
    // only register the Java task or fail when no supported plugin is present.
    project.afterEvaluate {
      when {
        // Multiplatform first: it also applies a Kotlin plugin, but has per-target classpaths
        // rather than the single compileClasspath/runtimeClasspath a JVM project has.
        //
        // Checked before Android rather than after it. A multiplatform module commonly applies an
        // Android plugin too, and it needs both paths: configureAndroidProject above covers its
        // android target, and this covers every other one. Returning early on isAndroidProject()
        // left such a module with only the android report, which for a jvm + native library is
        // most of the report missing.
        project.isKmpProject() -> project.configureKmpProject()
        // Already wired by configureAndroidProject, which had to run during configuration.
        project.isAndroidProject() -> Unit
        project.isJvmProject() -> project.configureJvmProject()
        else -> throw UnsupportedOperationException(
          "'com.jaredsburrows.license' requires Java, Kotlin or Android Gradle based plugins.",
        )
      }
    }
  }
}
