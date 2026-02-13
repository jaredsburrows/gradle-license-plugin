package com.jaredsburrows.license

import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.codehaus.plexus.util.ReaderFactory
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.reporting.ReportingExtension
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import java.io.File

/** Returns true if plugin exists in project. */
internal fun Project.hasPlugin(list: List<String>): Boolean = list.any { plugins.hasPlugin(it) }

private fun Project.includeParentPomFilesRecursively(
  mavenReader: MavenXpp3Reader,
  fileCollection: ConfigurableFileCollection,
  coordinateToFile: MutableMap<String, String>,
) {
  val pomFilesToInspect = ArrayDeque<File>()
  fileCollection.files.forEach { pomFilesToInspect.addLast(it) }

  val visitedPomFiles = hashSetOf<File>()
  val visitedParentCoordinates = hashSetOf<String>()

  while (pomFilesToInspect.isNotEmpty()) {
    val pomFile = pomFilesToInspect.removeFirst()
    if (!visitedPomFiles.add(pomFile)) {
      continue
    }

    val model =
      try {
        mavenReader.read(ReaderFactory.newXmlReader(pomFile), false)
      } catch (_: Exception) {
        continue
      }

    val parent = model.parent ?: continue
    val parentGroupId = parent.groupId.orEmpty().trim()
    val parentArtifactId = parent.artifactId.orEmpty().trim()
    val parentVersion = parent.version.orEmpty().trim()

    if (parentGroupId.isEmpty() || parentArtifactId.isEmpty() || parentVersion.isEmpty()) {
      continue
    }

    val parentCoordinate = "$parentGroupId:$parentArtifactId:$parentVersion"
    if (!visitedParentCoordinates.add(parentCoordinate)) {
      continue
    }
    if (coordinateToFile.containsKey(parentCoordinate)) {
      continue
    }

    val parentPomFile = resolvePomFile(parentGroupId, parentArtifactId, parentVersion) ?: continue
    coordinateToFile[parentCoordinate] = parentPomFile.absolutePath
    fileCollection.from(parentPomFile)
    pomFilesToInspect.addLast(parentPomFile)
  }
}

private fun Project.resolvePomFile(
  groupId: String,
  artifactId: String,
  version: String,
): File? {
  val result =
    dependencies
      .createArtifactResolutionQuery()
      .forModule(groupId, artifactId, version)
      .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
      .execute()

  val resolvedPomFiles =
    buildList {
      for (component in result.resolvedComponents) {
        for (artifact in component.getArtifacts(MavenPomArtifact::class.java)) {
          if (artifact is ResolvedArtifactResult) {
            add(artifact.file)
          }
        }
      }
    }.distinct()

  return resolvedPomFiles.firstOrNull()
}

/** Configure common configuration for both Java and Android tasks. */
internal fun Project.configureCommon(
  task: LicenseReportTask,
  configurationNames: List<String>,
) {
  val reportingExtension = extensions.getByType(ReportingExtension::class.java)
  val licenseExtension = extensions.getByType(LicenseReportExtension::class.java)

  val pomInput = buildPomInput(configurationNames)

  task.apply {
    outputDir = reportingExtension.file("licenses")

    pomFiles.from(pomInput.files.files)
    pomFiles.disallowChanges()
    rootCoordinates = pomInput.rootCoordinates
    pomCoordinatesToFile = pomInput.coordinateToFile

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
  val files: ConfigurableFileCollection,
  val rootCoordinates: List<String>,
  val coordinateToFile: Map<String, String>,
)

private fun Project.buildPomInput(configurationNames: List<String>): PomInput {
  val fileCollection = objects.fileCollection()
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
      fileCollection.from(pomFile)
    }
  }

  val mavenReader = MavenXpp3Reader()

  includeParentPomFilesRecursively(
    mavenReader = mavenReader,
    fileCollection = fileCollection,
    coordinateToFile = coordinateToFile,
  )

  return PomInput(
    files = fileCollection,
    rootCoordinates = roots.toList().sorted(),
    coordinateToFile = coordinateToFile,
  )
}
