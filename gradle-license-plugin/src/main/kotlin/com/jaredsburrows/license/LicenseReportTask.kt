package com.jaredsburrows.license

import com.jaredsburrows.license.internal.ConsoleRenderer
import com.jaredsburrows.license.internal.report.CsvReport
import com.jaredsburrows.license.internal.report.HtmlReport
import com.jaredsburrows.license.internal.report.JsonReport
import com.jaredsburrows.license.internal.report.Report
import com.jaredsburrows.license.internal.report.TextReport
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.codehaus.plexus.util.ReaderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.net.URL
import java.util.Locale
import javax.inject.Inject

/** A [org.gradle.api.Task] that creates HTML and JSON reports of the current projects dependencies. */
@DisableCachingByDefault(because = "Reports are copied to asset directories outside the declared output directory")
internal abstract class LicenseReportTask
  @Inject
  constructor(
    objectFactory: ObjectFactory,
  ) : DefaultTask() {
    // Never file collections: Gradle resolves their providers during scheduling, which is still
    // configuration time (#804).
    @get:Input
    val rootCoordinates: ListProperty<String> = objectFactory.listProperty(String::class.java)

    @get:Input
    val pomCoordinatesToFile: MapProperty<String, String> =
      objectFactory.mapProperty(String::class.java, String::class.java)

    // Never read by the action; re-runs the task when POM content changes at a stable path
    // (e.g. mavenLocal).
    @get:Input
    val pomContentHashes: MapProperty<String, String> =
      objectFactory.mapProperty(String::class.java, String::class.java)

    @Input
    var assetDirs = emptyList<File>()

    @Optional
    @Input
    var variantName: String? = null

    @get:OutputDirectory
    lateinit var outputDir: File

    @Input
    var generateCsvReport = false

    @Input
    var generateHtmlReport = false

    @Input
    var generateJsonReport = false

    @Input
    var generateTextReport = false

    @Input
    var copyCsvReportToAssets = false

    @Input
    var copyHtmlReportToAssets = false

    @Input
    var copyJsonReportToAssets = false

    @Input
    var copyTextReportToAssets = false

    @Input
    var useVariantSpecificAssetDirs = false

    @Input
    var ignoredPatterns = setOf<String>()

    @Input
    var showVersions = false

    private val projects = mutableListOf<Model>()

    init {
      // From DefaultTask
      description = "Outputs licenses report for $name."
      group = "Reporting"
    }

    @TaskAction
    fun licenseReport() {
      val mavenReader = MavenXpp3Reader()

      val loggedMissingParentPomCoordinates = hashSetOf<String>()
      projects.clear()
      generatePOMInfo(mavenReader, loggedMissingParentPomCoordinates)

      // Create CSV report
      if (generateCsvReport) {
        val csvReport = CsvReport(projects)
        val csvFile = File(outputDir, "$name.${csvReport.extension()}")
        createReport(file = csvFile) { csvReport }

        // If android project and copy enabled, copy to asset directory
        if (!variantName.isNullOrEmpty() && copyCsvReportToAssets) {
          copyReport(file = csvFile) { csvReport }
        }
      }

      // Create HTML report
      if (generateHtmlReport) {
        val htmlReport = HtmlReport(projects, showVersions)
        val htmlFile = File(outputDir, "$name.${htmlReport.extension()}")
        createReport(file = htmlFile) { htmlReport }

        // If android project and copy enabled, copy to asset directory
        if (!variantName.isNullOrEmpty() && copyHtmlReportToAssets) {
          copyReport(file = htmlFile) { htmlReport }
        }
      }

      // Create JSON report
      if (generateJsonReport) {
        val jsonReport = JsonReport(projects)
        val jsonFile = File(outputDir, "$name.${jsonReport.extension()}")
        createReport(file = jsonFile) { jsonReport }

        // If android project and copy enabled, copy to asset directory
        if (!variantName.isNullOrEmpty() && copyJsonReportToAssets) {
          copyReport(file = jsonFile) { jsonReport }
        }
      }

      // Create Text report
      if (generateTextReport) {
        val textReport = TextReport(projects)
        val textFile = File(outputDir, "$name.${textReport.extension()}")
        createReport(file = textFile) { textReport }

        // If android project and copy enabled, copy to asset directory
        if (!variantName.isNullOrEmpty() && copyTextReportToAssets) {
          copyReport(file = textFile) { textReport }
        }
      }
    }

    /** Get POM information from the pre-resolved dependency artifacts. */
    private fun generatePOMInfo(
      mavenReader: MavenXpp3Reader,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ) {
      rootCoordinates
        .get()
        .asSequence()
        .distinct()
        .mapNotNull { coordinate ->
          val pomFilePath = pomCoordinatesToFile.get()[coordinate] ?: return@mapNotNull null
          coordinate to File(pomFilePath)
        }.filter { (coordinate, _) ->
          ignoredPatterns.none { coordinate.contains(it) }
        }.forEach { (coordinate, pomFile) ->
          val model = readModel(mavenReader, pomFile) ?: return@forEach

          val (groupId, artifactId, version) = parseCoordinate(coordinate)

          var licenses = findLicenses(mavenReader, pomFile, loggedMissingParentPomCoordinates)
          if (licenses.isEmpty()) {
            logger.warn("Dependency '$artifactId' does not have a license.")
            licenses = mutableListOf()
          }

          val project =
            Model().apply {
              this.groupId = groupId
              this.artifactId = artifactId
              this.version = version
              this.name = model.pomName(mavenReader, pomFile, loggedMissingParentPomCoordinates)
              this.description = model.pomDescription()
              this.url = model.pomUrl()
              this.inceptionYear = model.pomInceptionYear()
              this.licenses = licenses
              this.developers = model.pomDevelopers()
            }

          projects += project
        }

      // Collapse duplicate developers and the same library reported more than once (different
      // versions from compile vs runtime, or Kotlin Multiplatform variants like foo / foo-android).
      deduplicate()

      // Sort POM information by name and id (:group:module:packaging:version) to have a deterministic order.
      projects.sortWith(compareBy({ it.name.lowercase(Locale.getDefault()) }, { it.id }))
    }

    /**
     * Reduce duplication in the collected projects:
     *  1. Remove repeated developers within a single project (some POMs list an author twice).
     *  2. Collapse entries that describe the same library but appear more than once because they
     *     were resolved at different versions (compile vs runtime) or as Kotlin Multiplatform
     *     platform variants (e.g. `foo` and `foo-android`). The highest version is kept; for equal
     *     versions the shorter (root) artifact id wins. Genuinely different artifacts that merely
     *     share a display name (sibling artifact ids, neither a prefix of the other) are preserved.
     */
    private fun deduplicate() {
      projects.forEach { model ->
        model.developers = model.developers.orEmpty().distinctBy { it.id.orEmpty() }
      }

      val deduped = mutableListOf<Model>()
      projects.forEach { model ->
        val existingIndex = deduped.indexOfFirst { it.isSameLibraryAs(model) }
        if (existingIndex < 0) {
          deduped += model
        } else if (model.isPreferredOver(deduped[existingIndex])) {
          deduped[existingIndex] = model
        }
      }
      projects.clear()
      projects.addAll(deduped)
    }

    /**
     * True if [this] and [other] are the same library: the same module at any versions (display
     * names may change between versions, e.g. "Okio" vs "okio"), or a Kotlin Multiplatform
     * platform artifact of it (annotation / annotation-jvm). Other "-suffix" siblings (foo-ktx,
     * kotlin-stdlib-jdk7) are distinct libraries and only collapse when their display names match.
     */
    private fun Model.isSameLibraryAs(other: Model): Boolean {
      if (groupId.orEmpty() != other.groupId.orEmpty()) return false

      val thisArtifact = artifactId.orEmpty()
      val otherArtifact = other.artifactId.orEmpty()
      if (thisArtifact == otherArtifact) return true

      val root = if (thisArtifact.length <= otherArtifact.length) thisArtifact else otherArtifact
      val variant = if (root === thisArtifact) otherArtifact else thisArtifact
      if (!variant.startsWith("$root-")) return false

      return variant.removePrefix("$root-") in PLATFORM_ARTIFACT_SUFFIXES ||
        name.orEmpty() == other.name.orEmpty()
    }

    /** Prefer the higher version; for equal versions prefer the shorter (root) artifact id. */
    private fun Model.isPreferredOver(other: Model): Boolean {
      val comparison = compareVersions(version.orEmpty(), other.version.orEmpty())
      return comparison > 0 ||
        (comparison == 0 && artifactId.orEmpty().length < other.artifactId.orEmpty().length)
    }

    private fun compareVersions(
      left: String,
      right: String,
    ): Int {
      val leftParts = left.split('.', '-', '_')
      val rightParts = right.split('.', '-', '_')
      for (index in 0 until maxOf(leftParts.size, rightParts.size)) {
        val leftPart = leftParts.getOrNull(index)?.toIntOrNull() ?: 0
        val rightPart = rightParts.getOrNull(index)?.toIntOrNull() ?: 0
        if (leftPart != rightPart) {
          return leftPart.compareTo(rightPart)
        }
      }
      return 0
    }

    private fun <T : Report> createReport(
      file: File,
      report: () -> T,
    ) {
      val newReport = report()

      file.apply {
        // Remove existing file
        delete()

        // Write report for file
        parentFile.mkdirs()
        writeText(newReport.toString())
      }

      // Log output directory for user
      logger.lifecycle(
        "Wrote ${newReport.name()} report to ${ConsoleRenderer().asClickableFileUrl(file)}.",
      )
    }

    private fun <T : Report> copyReport(
      file: File,
      report: () -> T,
    ) {
      val newReport = report()

      // Iterate through all asset directories
      assetDirs.forEach { directory ->
        val licenseFile = File(directory.path, "$OPEN_SOURCE_LICENSES.${newReport.extension()}")

        licenseFile.apply {
          // Remove existing file
          delete()

          // Write report for file
          parentFile.mkdirs()
          writeText(file.readText())
        }

        // Log output directory for user
        logger.lifecycle(
          "Copied ${newReport.name()} report to ${ConsoleRenderer().asClickableFileUrl(licenseFile)}.",
        )
      }
    }

    private fun findVersion(
      mavenReader: MavenXpp3Reader,
      pomFile: File?,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): String {
      if (pomFile.isNullOrEmpty()) {
        return ""
      }
      val model = pomFile?.let { readModel(mavenReader, it) } ?: return ""

      // If the POM is missing a name, do not record it
      val name = model.pomName(mavenReader, pomFile, loggedMissingParentPomCoordinates)
      if (name.isEmpty()) {
        logger.warn("POM file is missing a name: $pomFile")
        return ""
      }

      val version = model.pomVersion()
      if (version.isNotEmpty()) {
        return version.trim()
      }

      if (model.parent.artifactId
          .orEmpty()
          .trim()
          .isNotEmpty()
      ) {
        val parentPomFile = getParentPomFile(model, loggedMissingParentPomCoordinates)
        if (parentPomFile != null) {
          return findVersion(
            mavenReader,
            parentPomFile,
            loggedMissingParentPomCoordinates,
          )
        }
      }
      return ""
    }

    private fun findLicenses(
      mavenReader: MavenXpp3Reader,
      pomFile: File?,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): List<License> {
      if (pomFile.isNullOrEmpty()) {
        return emptyList()
      }
      val model = pomFile?.let { readModel(mavenReader, it) } ?: return emptyList()

      // If the POM is missing a name, do not record it
      val name = model.pomName(mavenReader, pomFile, loggedMissingParentPomCoordinates)
      if (name.isEmpty()) {
        logger.warn("POM file is missing a name: $pomFile")
        return emptyList()
      }

      if (ANDROID_SUPPORT_GROUP_ID == model.groupId.orEmpty().trim()) {
        return listOf(
          License().apply {
            this.name = APACHE_LICENSE_NAME
            url = APACHE_LICENSE_URL
          },
        )
      }

      // License information found
      return model.licenses
        .orEmpty()
        .map { license ->
          License().apply {
            this.name = license.name.orEmpty().trim()
            this.url = license.url.orEmpty().trim()
          }
        }.filter {
          it.name.isNotEmpty() || it.url.isUrlValid()
        }.ifEmpty {
          logger.info("Project, $name, has no license in POM file.")
          model.parent?.artifactId.orEmpty().trim().takeIf { it.isNotEmpty() }?.let {
            val parentPomFile = getParentPomFile(model, loggedMissingParentPomCoordinates)
            if (parentPomFile != null) {
              findLicenses(
                mavenReader,
                parentPomFile,
                loggedMissingParentPomCoordinates,
              )
            } else {
              emptyList()
            }
          } ?: emptyList()
        }
    }

    private fun String.isUrlValid(): Boolean =
      try {
        URL(this).toURI()
        true
      } catch (e: Exception) {
        logger.warn("Dependency has an invalid license URL '$this': ${e.shortMessage()}")
        logger.debug("Dependency has an invalid license URL '$this'", e)
        false
      }

    private fun Model.pomName(
      mavenReader: MavenXpp3Reader,
      pomFile: File?,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): String {
      val rawName = name.orEmpty().trim().ifEmpty { artifactId.orEmpty().trim() }
      if (!rawName.contains("\${")) {
        return rawName
      }

      val effectiveGroupId = resolveEffectiveGroupId(mavenReader, pomFile, loggedMissingParentPomCoordinates)
      val effectiveArtifactId = artifactId.orEmpty().trim()
      val effectiveVersion = resolveEffectiveVersion(mavenReader, pomFile, loggedMissingParentPomCoordinates)

      val placeholderToValue =
        mapOf(
          "project.groupId" to effectiveGroupId,
          "pom.groupId" to effectiveGroupId,
          "groupId" to effectiveGroupId,
          "project.artifactId" to effectiveArtifactId,
          "pom.artifactId" to effectiveArtifactId,
          "artifactId" to effectiveArtifactId,
          "project.name" to effectiveArtifactId,
          "project.version" to effectiveVersion,
          "pom.version" to effectiveVersion,
          "version" to effectiveVersion,
        )

      var interpolatedName = rawName
      placeholderToValue.forEach { (key, value) ->
        if (value.isNotEmpty()) {
          interpolatedName = interpolatedName.replace("\${$key}", value)
        }
      }

      // Resolve user-defined POM properties (e.g. ${extension.name}), including ones inherited from
      // parent POMs (where projects like javax.* commonly define them).
      collectProperties(mavenReader, loggedMissingParentPomCoordinates).forEach { (key, value) ->
        if (value.isNotEmpty()) {
          interpolatedName = interpolatedName.replace("\${$key}", value)
        }
      }

      // Fall back to the artifact id when placeholders cannot be resolved, so the report never shows
      // a raw "${...}" placeholder.
      return if (interpolatedName.contains("\${")) {
        artifactId.orEmpty().trim()
      } else {
        interpolatedName.trim()
      }
    }

    /** Collect this POM's properties merged with those inherited from its parent chain. */
    private fun Model.collectProperties(
      mavenReader: MavenXpp3Reader,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): Map<String, String> {
      val merged = linkedMapOf<String, String>()
      // Parent properties first so this POM's own properties take precedence.
      getParentPomFile(this, loggedMissingParentPomCoordinates)?.let { parentPomFile ->
        readModel(mavenReader, parentPomFile)?.let { parentModel ->
          merged.putAll(parentModel.collectProperties(mavenReader, loggedMissingParentPomCoordinates))
        }
      }
      properties.stringPropertyNames().forEach { key ->
        merged[key] = properties.getProperty(key).orEmpty()
      }
      return merged
    }

    private fun Model.pomDescription(): String = description.orEmpty().trim()

    private fun Model.pomUrl(): String = url.orEmpty().trim()

    private fun Model.pomVersion(): String = version.orEmpty().trim()

    private fun Model.pomInceptionYear(): String = inceptionYear.orEmpty().trim()

    private fun Model.pomDevelopers(): List<Developer> =
      developers.orEmpty().map { developer ->
        Developer().apply {
          id = developer.name.orEmpty().trim()
        }
      }

    /**
     * Parent POM resolution is performed outside the task; this only looks up the already-provided mapping.
     * Logs each missing parent coordinate only once to avoid noisy repeated warnings.
     */
    private fun getParentPomFile(
      model: Model,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): File? {
      val parent = model.parent ?: return null
      val groupId = parent.groupId.orEmpty().trim()
      val artifactId = parent.artifactId.orEmpty().trim()
      val version = parent.version.orEmpty().trim()

      if (groupId.isEmpty() || artifactId.isEmpty() || version.isEmpty()) {
        return null
      }

      val coordinate = "$groupId:$artifactId:$version"
      val pomFilePath = pomCoordinatesToFile.get()[coordinate]
      if (pomFilePath == null) {
        if (loggedMissingParentPomCoordinates.add(coordinate)) {
          logger.warn("Parent POM $groupId:$artifactId:$version@pom not found")
        }
        return null
      }

      return File(pomFilePath)
    }

    private fun readModel(
      mavenReader: MavenXpp3Reader,
      pomFile: File,
    ): Model? =
      try {
        mavenReader.read(ReaderFactory.newXmlReader(pomFile), false)
      } catch (e: Exception) {
        logger.warn("Failed to read POM file '$pomFile': ${e.shortMessage()}")
        null
      }

    private fun parseCoordinate(coordinate: String): Triple<String, String, String> {
      val parts = coordinate.split(":")
      if (parts.size != 3) {
        return Triple("", coordinate, "")
      }
      return Triple(parts[0].trim(), parts[1].trim(), parts[2].trim())
    }

    private fun resolveEffectiveGroupId(
      mavenReader: MavenXpp3Reader,
      pomFile: File?,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): String {
      val model = pomFile?.let { readModel(mavenReader, it) } ?: return ""
      val groupId = model.groupId.orEmpty().trim()
      if (groupId.isNotEmpty()) {
        return groupId
      }

      val parentFile = getParentPomFile(model, loggedMissingParentPomCoordinates) ?: return ""
      return resolveEffectiveGroupId(mavenReader, parentFile, loggedMissingParentPomCoordinates)
    }

    private fun resolveEffectiveVersion(
      mavenReader: MavenXpp3Reader,
      pomFile: File?,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): String {
      val model = pomFile?.let { readModel(mavenReader, it) } ?: return ""
      val version = model.version.orEmpty().trim()
      if (version.isNotEmpty()) {
        return version
      }

      val parentFile = getParentPomFile(model, loggedMissingParentPomCoordinates) ?: return ""
      return resolveEffectiveVersion(mavenReader, parentFile, loggedMissingParentPomCoordinates)
    }

    private fun File?.isNullOrEmpty(): Boolean = this?.length() == 0L

    private fun Exception.shortMessage(): String =
      (message ?: "<no message>").let {
        if (it.length > MAX_EXCEPTION_MESSAGE_LENGTH) {
          "${it.take(MAX_EXCEPTION_MESSAGE_LENGTH)}... (see --debug for complete message)"
        } else {
          it
        }
      }

    private companion object {
      // Kotlin Multiplatform platform-artifact suffixes safe to treat as the same library.
      // "-android" is excluded: distinct products use it (dagger / dagger-android).
      private val PLATFORM_ARTIFACT_SUFFIXES = setOf("jvm")
      private const val ANDROID_SUPPORT_GROUP_ID = "com.android.support"
      private const val APACHE_LICENSE_NAME = "The Apache Software License"
      private const val APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
      private const val OPEN_SOURCE_LICENSES = "open_source_licenses"
      private const val MAX_EXCEPTION_MESSAGE_LENGTH = 200
    }
  }
