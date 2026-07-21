package com.jaredsburrows.license

import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.codehaus.plexus.util.ReaderFactory
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.reporting.ReportingExtension
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import java.io.File
import java.security.MessageDigest

/** Returns true if plugin exists in project. */
internal fun Project.hasPlugin(list: List<String>): Boolean = list.any { plugins.hasPlugin(it) }

private fun Project.includeParentPomFilesRecursively(
  mavenReader: MavenXpp3Reader,
  rootPomFiles: List<File>,
  coordinateToFile: MutableMap<String, String>,
) {
  var pomFilesToInspect = rootPomFiles
  val visitedParentCoordinates = hashSetOf<String>()

  while (pomFilesToInspect.isNotEmpty()) {
    // Collect every not-yet-resolved parent coordinate referenced by the current batch of POMs.
    val parentCoordinates = linkedSetOf<String>()
    pomFilesToInspect.forEach { pomFile ->
      val model =
        try {
          mavenReader.read(ReaderFactory.newXmlReader(pomFile), false)
        } catch (_: Exception) {
          return@forEach
        }

      val parent = model.parent ?: return@forEach
      val parentGroupId = parent.groupId.orEmpty().trim()
      val parentArtifactId = parent.artifactId.orEmpty().trim()
      val parentVersion = parent.version.orEmpty().trim()
      if (parentGroupId.isEmpty() || parentArtifactId.isEmpty() || parentVersion.isEmpty()) {
        return@forEach
      }

      val parentCoordinate = "$parentGroupId:$parentArtifactId:$parentVersion"
      if (parentCoordinate !in coordinateToFile && visitedParentCoordinates.add(parentCoordinate)) {
        parentCoordinates += parentCoordinate
      }
    }

    if (parentCoordinates.isEmpty()) {
      break
    }

    // Resolve every parent POM for this level in a SINGLE batched resolution (instead of one
    // metadata query per parent), then recurse on the grandparents found by parsing them.
    val nextPomFiles = mutableListOf<File>()
    resolvePomFiles(parentCoordinates).forEach { (coordinate, pomFile) ->
      coordinateToFile[coordinate] = pomFile.absolutePath
      nextPomFiles += pomFile
    }
    pomFilesToInspect = nextPomFiles
  }
}

/** Resolve the POM files for the given GAV coordinates in a single batched dependency resolution. */
private fun Project.resolvePomFiles(coordinates: Collection<String>): Map<String, File> {
  if (coordinates.isEmpty()) {
    return emptyMap()
  }

  val pomDependencies =
    coordinates
      .map { dependencies.create("$it@pom") }
      .toTypedArray()
  val detachedConfiguration =
    configurations.detachedConfiguration(*pomDependencies).apply {
      isTransitive = false
    }

  val resolved = linkedMapOf<String, File>()
  detachedConfiguration.incoming
    .artifactView { it.isLenient = true }
    .artifacts
    .forEach { artifact ->
      val id = artifact.id.componentIdentifier
      if (id is ModuleComponentIdentifier) {
        resolved["${id.group}:${id.module}:${id.version}"] = artifact.file
      }
    }
  return resolved
}

/** Configure common configuration for both Java and Android tasks. */
internal fun Project.configureCommon(
  task: LicenseReportTask,
  configurationNames: List<String>,
) {
  val reportingExtension = extensions.getByType(ReportingExtension::class.java)
  val licenseExtension = extensions.getByType(LicenseReportExtension::class.java)

  // Defer all resolution until the task inputs are queried at execution time; resolving during
  // task configuration triggers AGP's "resolved during configuration time" warning (#804).
  val pomInput = lazy { buildPomInput(configurationNames) }
  val rootCoordinatesProvider = provider { pomInput.value.rootCoordinates }
  val coordinateToFileProvider = provider { pomInput.value.coordinateToFile }
  val contentHashesProvider = provider { pomInput.value.contentHashes }

  task.apply {
    outputDir =
      reportingExtension.baseDirectory
        .dir("licenses")
        .get()
        .asFile

    rootCoordinates.set(rootCoordinatesProvider)
    pomCoordinatesToFile.set(coordinateToFileProvider)
    pomContentHashes.set(contentHashesProvider)

    generateCsvReport = licenseExtension.generateCsvReport
    generateHtmlReport = licenseExtension.generateHtmlReport
    generateJsonReport = licenseExtension.generateJsonReport
    generateTextReport = licenseExtension.generateTextReport
    copyCsvReportToAssets = licenseExtension.copyCsvReportToAssets
    copyHtmlReportToAssets = licenseExtension.copyHtmlReportToAssets
    copyJsonReportToAssets = licenseExtension.copyJsonReportToAssets
    copyTextReportToAssets = licenseExtension.copyTextReportToAssets
    useVariantSpecificAssetDirs = licenseExtension.useVariantSpecificAssetDirs
    ignoredPatterns = licenseExtension.ignoredPatterns
    showVersions = licenseExtension.showVersions
  }
}

private data class PomInput(
  val rootCoordinates: List<String>,
  val coordinateToFile: Map<String, String>,
  val contentHashes: Map<String, String>,
)

/** Content hash tracked as a task input; a stable path (e.g. mavenLocal) can hide POM changes. */
private fun File.contentHash(): String =
  try {
    MessageDigest
      .getInstance("SHA-256")
      .digest(readBytes())
      .joinToString("") { "%02x".format(it) }
  } catch (_: Exception) {
    // Unreadable file: fall back to metadata so it still contributes a distinguishing value.
    "$absolutePath:${length()}:${lastModified()}"
  }

private fun Project.buildPomInput(configurationNames: List<String>): PomInput {
  val rootPomFiles = mutableListOf<File>()
  val coordinateToFile = sortedMapOf<String, String>()
  val roots = linkedSetOf<String>()

  val componentIdentifiers = linkedSetOf<ComponentIdentifier>()

  configurationNames
    .mapNotNull { configurations.findByName(it) }
    .filter { it.isCanBeResolved }
    .forEach { configuration ->
      configuration
        .incoming
        .resolutionResult
        .allComponents
        .map { it.id }
        .filterIsInstance<ModuleComponentIdentifier>()
        .forEach { componentIdentifiers += it }
    }

  if (componentIdentifiers.isNotEmpty()) {
    val pomArtifactResult =
      dependencies
        .createArtifactResolutionQuery()
        .forComponents(componentIdentifiers)
        .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
        .execute()

    pomArtifactResult.resolvedComponents.forEach { component ->
      val componentId = component.id
      if (componentId !is ModuleComponentIdentifier) {
        return@forEach
      }

      val pomFile =
        component
          .getArtifacts(MavenPomArtifact::class.java)
          .filterIsInstance<ResolvedArtifactResult>()
          .firstOrNull()
          ?.file
          ?: return@forEach

      val coordinate = "${componentId.group}:${componentId.module}:${componentId.version}"
      coordinateToFile.putIfAbsent(coordinate, pomFile.absolutePath)
      roots += coordinate
      rootPomFiles += pomFile
    }
  }

  val mavenReader = MavenXpp3Reader()

  includeParentPomFilesRecursively(
    mavenReader = mavenReader,
    rootPomFiles = rootPomFiles,
    coordinateToFile = coordinateToFile,
  )

  return PomInput(
    rootCoordinates = roots.toList().sorted(),
    coordinateToFile = coordinateToFile,
    contentHashes = coordinateToFile.mapValues { (_, path) -> File(path).contentHash() },
  )
}
