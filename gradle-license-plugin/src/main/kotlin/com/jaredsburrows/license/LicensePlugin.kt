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
      if (!project.isAndroidProject()) {
        when {
          project.isJavaProject() -> project.configureJavaProject()
          else -> throw UnsupportedOperationException(
            "'com.jaredsburrows.license' requires Java, Kotlin or Android Gradle based plugins.",
          )
        }
      }
    }
  }
}
