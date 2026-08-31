package com.jaredsburrows.license

import com.jaredsburrows.license.internal.ConsoleRenderer
import com.jaredsburrows.license.internal.readPom
import com.jaredsburrows.license.internal.report.CsvReport
import com.jaredsburrows.license.internal.report.HtmlReport
import com.jaredsburrows.license.internal.report.JsonFullReport
import com.jaredsburrows.license.internal.report.JsonReport
import com.jaredsburrows.license.internal.report.Report
import com.jaredsburrows.license.internal.report.TextReport
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
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

    /**
     * The copies written into the asset directories.
     *
     * Deliberately not @OutputFiles. These land in src/<variant>/assets, which AGP treats as a
     * source directory, so declaring them makes every AGP task that reads assets - lint model
     * generation among them - fail validation for using an output of this task without declaring a
     * dependency. An upToDateWhen check in init gets the re-run without claiming the location.
     */
    @get:Internal
    val copiedReportFiles: List<File>
      get() {
        if (variantName.isNullOrEmpty()) {
          return emptyList()
        }
        val extensions =
          buildList {
            if (generateCsvReport && copyCsvReportToAssets) add("csv")
            if (generateHtmlReport && copyHtmlReportToAssets) add("html")
            if (generateJsonReport && copyJsonReportToAssets) add("json")
            if (generateJsonFullReport && copyJsonFullReportToAssets) add("full.json")
            if (generateTextReport && copyTextReportToAssets) add("txt")
          }
        return assetDirs.flatMap { directory ->
          extensions.map { File(directory, "$OPEN_SOURCE_LICENSES.$it") }
        }
      }

    @Input
    var generateCsvReport = false

    @Input
    var generateHtmlReport = false

    @Input
    var generateJsonReport = false

    @Input
    var generateJsonFullReport = false

    @Input
    var generateTextReport = false

    @Input
    var copyCsvReportToAssets = false

    @Input
    var copyHtmlReportToAssets = false

    @Input
    var copyJsonReportToAssets = false

    @Input
    var copyJsonFullReportToAssets = false

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

      // The asset copies are not declared outputs, so deleting one leaves Gradle seeing nothing
      // missing. Without this the task stays UP-TO-DATE and the app ships with no licenses file.
      outputs.upToDateWhen { copiedReportFiles.all { file -> file.exists() } }
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

      // Create full JSON report
      if (generateJsonFullReport) {
        val jsonFullReport = JsonFullReport(projects)
        val jsonFullFile = File(outputDir, "$name.${jsonFullReport.extension()}")
        createReport(file = jsonFullFile) { jsonFullReport }

        // If android project and copy enabled, copy to asset directory
        if (!variantName.isNullOrEmpty() && copyJsonFullReportToAssets) {
          copyReport(file = jsonFullFile) { jsonFullReport }
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
          ignoredPatterns.none { coordinate.matchesIgnoredPattern(it) }
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
      val variant = if (root == thisArtifact) otherArtifact else thisArtifact
      if (!variant.startsWith("$root-")) return false

      val suffix = variant.removePrefix("$root-")
      return suffix in PLATFORM_ARTIFACT_SUFFIXES ||
        (suffix in SHARED_PLATFORM_ARTIFACT_SUFFIXES && name.orEmpty() == other.name.orEmpty())
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

    /**
     * This POM and its ancestors, nearest first, lazily. Stops at a POM already seen, so a
     * self-parent or an A -> B -> A cycle terminates instead of overflowing the stack.
     */
    private fun parentChain(
      mavenReader: MavenXpp3Reader,
      pomFile: File?,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): Sequence<Pair<File, Model>> =
      // Sequence {} rather than a shared generateSequence: each iteration needs its own visited
      // set, or consuming the chain twice would yield nothing the second time.
      Sequence {
        val visited = hashSetOf<String>()

        fun node(file: File?): Pair<File, Model>? {
          val candidate = file ?: return null
          if (candidate.isNullOrEmpty() || !visited.add(candidate.absolutePath)) {
            return null
          }
          return readModel(mavenReader, candidate)?.let { candidate to it }
        }

        generateSequence(node(pomFile)) { (_, model) ->
          node(getParentPomFile(model, loggedMissingParentPomCoordinates))
        }.take(MAX_PARENT_DEPTH)
          .iterator()
      }

    private fun findLicenses(
      mavenReader: MavenXpp3Reader,
      pomFile: File?,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): List<License> {
      // Pre-20.0.0 support library POMs declare no license at all; they are all Apache 2.0. The
      // flag is set as the chain is walked, matching the recursive version, which applied the
      // fallback at whichever level declared the support group id.
      var isSupportLibrary = false

      for ((file, model) in parentChain(mavenReader, pomFile, loggedMissingParentPomCoordinates)) {
        // If the POM is missing a name, do not record it
        val name = model.pomName(mavenReader, file, loggedMissingParentPomCoordinates)
        if (name.isEmpty()) {
          logger.warn("POM file is missing a name: $file")
          break
        }

        // License information found
        val licenses =
          model.licenses
            .orEmpty()
            .map { license ->
              License().apply {
                this.name = license.name.orEmpty().trim()
                this.url = license.url.orEmpty().trim()
              }
            }.filter {
              it.name.isNotEmpty() || it.url.isUrlValid()
            }
        if (licenses.isNotEmpty()) {
          return licenses
        }

        logger.info("Project, $name, has no license in POM file.")
        if (ANDROID_SUPPORT_GROUP_ID == model.groupId.orEmpty().trim()) {
          isSupportLibrary = true
        }
      }

      return if (isSupportLibrary) {
        listOf(
          License().apply {
            name = APACHE_LICENSE_NAME
            url = APACHE_LICENSE_URL
          },
        )
      } else {
        emptyList()
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
      // Guarded like parentChain, but seeded from a Model rather than a File, so it cannot reuse it.
      val visited = hashSetOf<String>()
      return generateSequence(this) { model ->
        getParentPomFile(model, loggedMissingParentPomCoordinates)
          ?.takeIf { visited.add(it.absolutePath) }
          ?.let { readModel(mavenReader, it) }
      }.take(MAX_PARENT_DEPTH)
        .toList()
        // Root-most first, so a nearer POM's properties overwrite an ancestor's.
        .asReversed()
        .flatMap { model ->
          model.properties.stringPropertyNames().map { key ->
            key to model.properties.getProperty(key).orEmpty()
          }
        }.toMap()
    }

    private fun Model.pomDescription(): String = description.orEmpty().trim()

    private fun Model.pomUrl(): String = url.orEmpty().trim()

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
        mavenReader.readPom(pomFile)
      } catch (e: Exception) {
        logger.warn("Failed to read POM file '$pomFile': ${e.shortMessage()}")
        null
      }

    /**
     * True when [pattern] occurs in this coordinate aligned to segment boundaries (':', '.' or
     * either end), so ignoring "foo:bar" does not also ignore "foo:bar-extra" (#397).
     */
    private fun String.matchesIgnoredPattern(pattern: String): Boolean {
      if (pattern.isEmpty()) {
        return false
      }

      var index = indexOf(pattern)
      while (index >= 0) {
        val end = index + pattern.length
        val startsAtBoundary = index == 0 || this[index - 1].isBoundary() || pattern.first().isBoundary()
        val endsAtBoundary = end == length || this[end].isBoundary() || pattern.last().isBoundary()
        if (startsAtBoundary && endsAtBoundary) {
          return true
        }
        index = indexOf(pattern, index + 1)
      }
      return false
    }

    private fun Char.isBoundary(): Boolean = this == ':' || this == '.'

    private fun parseCoordinate(coordinate: String): Triple<String, String, String> {
      val parts = coordinate.split(":")
      if (parts.size != 3) {
        return Triple("", coordinate, "")
      }
      return Triple(parts[0].trim(), parts[1].trim(), parts[2].trim())
    }

    /** The group id of the nearest POM in the chain that states one, as Maven inherits it. */
    private fun resolveEffectiveGroupId(
      mavenReader: MavenXpp3Reader,
      pomFile: File?,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): String =
      parentChain(mavenReader, pomFile, loggedMissingParentPomCoordinates)
        .firstNotNullOfOrNull { (_, model) ->
          model.groupId
            .orEmpty()
            .trim()
            .ifEmpty { null }
        }.orEmpty()

    /** The version of the nearest POM in the chain that states one, as Maven inherits it. */
    private fun resolveEffectiveVersion(
      mavenReader: MavenXpp3Reader,
      pomFile: File?,
      loggedMissingParentPomCoordinates: MutableSet<String>,
    ): String =
      parentChain(mavenReader, pomFile, loggedMissingParentPomCoordinates)
        .firstNotNullOfOrNull { (_, model) ->
          model.version
            .orEmpty()
            .trim()
            .ifEmpty { null }
        }.orEmpty()

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
      // Kotlin Multiplatform target names no separate product is published under, so
      // "<root>-<target>" is always the same library built for one target.
      private val PLATFORM_ARTIFACT_SUFFIXES =
        setOf(
          "jvm",
          "js",
          "wasm-js",
          "wasm-wasi",
          "linuxx64",
          "linuxarm64",
          "macosx64",
          "macosarm64",
          "mingwx64",
          "iosx64",
          "iosarm64",
          "iossimulatorarm64",
          "tvosx64",
          "tvosarm64",
          "tvossimulatorarm64",
          "watchosx64",
          "watchosarm32",
          "watchosarm64",
          "watchosdevicearm64",
          "watchossimulatorarm64",
        )

      // Target names that separate products also use, so these collapse only when the display
      // names agree too. Without this gate every "-suffix" sibling could collapse on a shared
      // name alone, which is how androidx.test:core-ktx vanished behind androidx.test:core.
      private val SHARED_PLATFORM_ARTIFACT_SUFFIXES = setOf("android", "desktop", "native")
      private const val ANDROID_SUPPORT_GROUP_ID = "com.android.support"
      private const val APACHE_LICENSE_NAME = "The Apache Software License"
      private const val APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
      private const val OPEN_SOURCE_LICENSES = "open_source_licenses"

      // A parent chain is a handful deep in practice; the cap only stops a malformed POM
      // (a self-parent or an A -> B -> A cycle) from walking forever.
      const val MAX_PARENT_DEPTH = 100

      private const val MAX_EXCEPTION_MESSAGE_LENGTH = 200
    }
  }
