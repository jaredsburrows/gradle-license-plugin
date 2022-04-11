package com.jaredsburrows.license

import org.gradle.api.Plugin
import org.gradle.api.Project

/** A [Plugin] which grabs the POM.xml files from maven dependencies. */
class LicensePlugin : Plugin<Project> {
  val ANDROID_APPLICATION_PLUGIN = "com.android.application"
  val ANDROID_LIBRARY_PLUGIN = "com.android.library"
  val ANDROID_TEST_PLUGIN = "com.android.test"
  val GROOVY_PLUGIN = "groovy"
  val JAVA_PLUGIN = "java"
  override fun apply(project: Project) {
    project.extensions.add("licenseReport", LicenseReportExtension::class.java)

    project.afterEvaluate {
      if (project.plugins.hasPlugin(ANDROID_APPLICATION_PLUGIN)
        || project.plugins.hasPlugin(ANDROID_LIBRARY_PLUGIN)
        || project.plugins.hasPlugin(ANDROID_TEST_PLUGIN)
      ) project.configureAndroidProject() else
      if (project.plugins.hasPlugin(GROOVY_PLUGIN)
        || project.plugins.hasPlugin(JAVA_PLUGIN)
      ) project.configureJavaProject() else
      throw UnsupportedOperationException("'com.jaredsburrows.license' requires Java or Android Gradle Plugins.")
    }

//    project.plugins.all {
//      when (it) {
//        is JavaPlugin -> project.configureJavaProject()
//        is FeaturePlugin -> {
//          project.extensions.getByType(FeatureExtension::class.java).run {
//            project.configureAndroidProject(featureVariants)
//            project.configureAndroidProject(libraryVariants)
//          }
//        }
//        is LibraryPlugin -> {
//          project.extensions.getByType(LibraryExtension::class.java).run {
//            project.configureAndroidProject(libraryVariants)
//          }
//        }
//        is AppPlugin -> {
//          project.extensions.getByType(AppExtension::class.java).run {
//            project.configureAndroidProject(applicationVariants)
//          }
//        }
//      }
//    }

  }

}
