package com.jaredsburrows.license

import org.gradle.api.Project

/** Configure for Java projects. */
internal fun Project.configureJavaProject() {
  tasks.register("licenseReport", JavaLicenseReportTask::class.java)
}
