package com.jaredsburrows.license

import org.gradle.api.Plugin
import org.gradle.api.Project

/** A [Plugin] which grabs the POM.xml files from maven dependencies. */
class LicensePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.add("licenseReport", LicenseReportExtension::class.java)

    project.afterEvaluate {
      when {
        project.isAndroidProject() -> project.configureAndroidProject()
        project.isJavaProject() -> project.configureJavaProject()
        else -> throw UnsupportedOperationException(
          "'com.jaredsburrows.license' requires Java, Kotlin or Android Gradle based plugins.",
        )
      }
    }
  }
}
