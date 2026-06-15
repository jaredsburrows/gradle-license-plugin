package com.jaredsburrows.license

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.BasePlugin
import org.gradle.api.Project
import java.io.File
import java.util.*

private fun String.capitalizeText(): String {
  if (isEmpty()) return this
  return replaceFirstChar { it.titlecase(Locale.getDefault()) }
}

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
 * Configure for Android projects.
 *
 * AppPlugin - "android", "com.android.application"
 * LibraryPlugin - "android-library", "com.android.library"
 * TestPlugin - "com.android.test"
 */
internal fun Project.configureAndroidProject() {
  plugins.all {
    when (it) {
      is BasePlugin -> {
        extensions.getByType(CommonExtension::class.java).run {
          val flavorDimensions = flavorDimensions
          val flavorValues = flavorDimensions.map { dimension ->
            dimension to productFlavors.filter { it.dimension == dimension }.map { it.name }
          }
          val buildTypes = buildTypes.names
          val targetTypes = if (this is LibraryExtension || this is ApplicationExtension) {
            listOf("", "androidTest", "unitTest")
          } else {
            listOf("")
          }
          generateTaskForAllVariants(
            flavorValues,
            buildTypes.toList(),
            targetTypes
          )
        }
      }
    }
  }
}

internal fun Project.generateTaskForAllVariants(
  flavorDimensionValues: List<Pair<String, List<String>>>,
  buildTypes: List<String>,
  targetVariants: List<String>,
) {
  fun generateNameVariants(possibleValues: List<List<String>>, prefix: String = "", processGenerated: (String) -> Unit) {
    if (possibleValues.isEmpty()) {
      processGenerated(prefix)
    } else {
      for (value in possibleValues.first()) {
        generateNameVariants(possibleValues.drop(1), prefix + value.capitalizeText(), processGenerated)
      }
    }
  }

  val generatedVariants = buildList {
    val nameComponents = flavorDimensionValues.map { it.second }.toMutableList()
    nameComponents.add(buildTypes)
    nameComponents.add(targetVariants)

    generateNameVariants(nameComponents, processGenerated = this::add)
  }

  logger.info("Generated ${generatedVariants.size} variants for project ${this.name}: $generatedVariants")
  generatedVariants.forEach { configureVariant(it) }
}

private fun Project.configureVariant(
  variantName: String,
) {
  // Configure tasks for all variants
  val name = variantName.capitalizeText()

  tasks.register("license${name}Report", LicenseReportTask::class.java) { report ->
    // Apply common task configuration first
    configureCommon(
      report,
      listOf(
        "${variantName}CompileClasspath",
        "${variantName}RuntimeClasspath",
      ),
    )

    // Custom for Android tasks
    val sourceSetName = if (report.useVariantSpecificAssetDirs) variantName else "main"
    extensions.getByType(CommonExtension::class.java).apply {
      sourceSets.findByName(sourceSetName)?.let {
        report.assetDirs = it.assets.directories.map { File(it) }
      } ?: run {
        report.assetDirs = emptyList()
      }
    }

    report.variantName = variantName
  }
}
