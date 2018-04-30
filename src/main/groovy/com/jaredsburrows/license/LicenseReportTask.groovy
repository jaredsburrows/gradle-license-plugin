package com.jaredsburrows.license

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.report.HtmlReport
import com.jaredsburrows.license.internal.report.JsonReport
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class LicenseReportTask extends DefaultTask {
  final static def POM_CONFIGURATION = "poms"
  final static def TEMP_POM_CONFIGURATION = "tempPoms"
  final static def ANDROID_SUPPORT_GROUP_ID = "com.android.support"
  final static def APACHE_LICENSE_NAME = "The Apache Software License"
  final static def APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
  final static def OPEN_SOURCE_LICENSES = "open_source_licenses"
  final static def HTML_EXT = ".html"
  final static def JSON_EXT = ".json"
  @Internal final List<Project> projects = []
  @Optional @Input File[] assetDirs = []
  @Optional @Input def generateHtmlReport
  @Optional @Input def generateJsonReport
  @Optional @Input def copyHtmlReportToAssets
  @Optional @Input def copyJsonReportToAssets
  @Optional @Input def buildType
  @Optional @Input def variant
  @Optional @Internal def productFlavors = []
  @OutputFile File htmlFile
  @OutputFile File jsonFile

  @SuppressWarnings("GroovyUnusedDeclaration") @TaskAction def licenseReport() {
    setupEnvironment()
    collectDependencies()
    generatePOMInfo()

    if (generateHtmlReport) {
      createHtmlReport()

        // If Android project and copy enabled, copy to asset directory
        if (variant && copyHtmlReportToAssets) {
          copyHtmlReport()
        }
      }

    if (generateJsonReport) {
      createJsonReport()

      // If Android project and copy enabled, copy to asset directory
      if (variant && copyJsonReportToAssets) {
        copyJsonReport()
      }
    }
  }

  /**
   * Setup configurations to collect dependencies.
   */
  private def setupEnvironment() {
    // Create temporary configuration in order to store POM information
    project.configurations.create(POM_CONFIGURATION)

    project.configurations.every {
      try {
        it.canBeResolved = true
      } catch (Exception ignore) { }
    }
  }

  /**
   * Iterate through all configurations and collect dependencies.
   */
  private def collectDependencies() {
    // Add POM information to our POM configuration
    final Set<Configuration> configurations = new LinkedHashSet<>()

    // Add "compile" configuration older java and android gradle plugins
    if (project.configurations.find { it.name == "compile" }) configurations << project.configurations."compile"

    // Add "api" and "implementation" configurations for newer java-library and android gradle plugins
    if (project.configurations.find { it.name == "api" }) configurations << project.configurations."api"
    if (project.configurations.find { it.name == "implementation" }) configurations << project.configurations."implementation"

    // If Android project, add extra configurations
    if (variant) {
      // Add buildType configurations
      if (project.configurations.find { it.name == "compile" }) configurations << project.configurations."${buildType}Compile"
      if (project.configurations.find { it.name == "api" }) configurations << project.configurations."${buildType}Api"
      if (project.configurations.find { it.name == "implementation" }) configurations << project.configurations."${buildType}Implementation"

      // Add productFlavors configurations
      productFlavors.each { flavor ->
        // Works for productFlavors and productFlavors with dimensions
        if (variant.capitalize().contains(flavor.name.capitalize())) {
          if (project.configurations.find { it.name == "compile" }) configurations << project.configurations."${flavor.name}Compile"
          if (project.configurations.find { it.name == "api" }) configurations << project.configurations."${flavor.name}Api"
          if (project.configurations.find { it.name == "implementation" }) configurations << project.configurations."${flavor.name}Implementation"
        }
      }
    }

    // Iterate through all the configurations's dependencies
    configurations.each { configuration ->
      configuration.canBeResolved &&
        configuration.resolvedConfiguration.lenientConfiguration.artifacts*.moduleVersion.id.collect { id ->
          "$id.group:$id.name:$id.version@pom"
        }.each { pom ->
          project.configurations."$POM_CONFIGURATION".dependencies.add(
            project.dependencies.add("$POM_CONFIGURATION", pom)
          )
        }
    }
  }

  /**
   * Get POM information from the dependency artifacts.
   */
  private def generatePOMInfo() {
    // Iterate through all POMs in order from our custom POM configuration
    project.configurations."$POM_CONFIGURATION".resolvedConfiguration.lenientConfiguration.artifacts.each { pom ->
      final def pomFile = pom.file
      final def pomText = new XmlParser().parse(pomFile)

      // License information
      def name = getName(pomText)
      def version = pomText.version?.text()
      def description = pomText.description?.text()
      def developers = []
      if (pomText.developers) {
        developers = pomText.developers.developer?.collect { developer ->
          new Developer(name: developer?.name?.text()?.trim())
        }
      }

      def url = pomText.url?.text()
      def year = pomText.inceptionYear?.text()

      // Clean up and format
      name = name?.capitalize()
      version = version?.trim()
      description = description?.trim()
      url = url?.trim()
      year = year?.trim()

      def licenses = findLicenses(pomFile)
      if (!licenses) {
        logger.log(LogLevel.WARN, "${name} dependency does not have a license.")
        licenses = []
      }

      // Store the information that we need
      final def project = new Project(
        name: name,
        description: description,
        version: version,
        developers: developers,
        licenses: licenses,
        url: url,
        year: year,
        dependencyString: pom.owner
      )

      projects << project
    }

    // Sort POM information by name
    projects.sort { project -> project.name }
  }

  static def getName(def pomText) {
    def name = pomText.name?.text() ? pomText.name?.text() : pomText.artifactId?.text()
    return name?.trim()
  }

  def findLicenses(def pomFile) {
    if (!pomFile) {
      return null
    }
    final def pomText = new XmlParser().parse(pomFile)

    // If the POM is missing a name, do not record it
    final def name = getName(pomText)
    if (!name) {
      logger.log(LogLevel.WARN, "POM file is missing a name: ${pomFile}")
      return null
    }

    if (ANDROID_SUPPORT_GROUP_ID == pomText.groupId?.text()) {
      return [ new License(name: APACHE_LICENSE_NAME, url: APACHE_LICENSE_URL) ]
    }

    // License information found
    if (pomText.licenses) {
      def licenses = []
      pomText.licenses[0].license.each { license ->
        def licenseName = license.name?.text()
        def licenseUrl = license.url?.text()
        try {
          //noinspection GroovyResultOfObjectAllocationIgnored
          new URL(licenseUrl)
            licenseName = licenseName?.trim()?.capitalize()
            licenseUrl = licenseUrl?.trim()
            licenses << new License(name: licenseName, url: licenseUrl)
        } catch (Exception ignore) {
          logger.log(LogLevel.WARN, "${name} dependency has an invalid license URL; skipping license")
        }
      }
      return licenses
    }
    logger.log(LogLevel.INFO, "Project, ${name}, has no license in POM file.")

    final def hasParent = pomText.parent != null
    if (hasParent) {
      final def parentPomFile = getParentPomFile(pomText)
      return findLicenses(parentPomFile)
    }
    return null
  }

  /**
   * Use Parent POM information when individual dependency license information is missing.
   */
  private def getParentPomFile(def pomText) {
    // Get parent POM information
    def groupId = pomText?.parent?.groupId?.text()
    def artifactId = pomText?.parent?.artifactId?.text()
    def version = pomText?.parent?.version?.text()
    def dependency = "$groupId:$artifactId:$version@pom"

    // Add dependency to temporary configuration
    project.configurations.create(TEMP_POM_CONFIGURATION)
    project.configurations."$TEMP_POM_CONFIGURATION".dependencies.add(
      project.dependencies.add(TEMP_POM_CONFIGURATION, dependency)
    )

    def pomFile = project.configurations."$TEMP_POM_CONFIGURATION".resolvedConfiguration.lenientConfiguration.artifacts?.file[0]

    // Reset dependencies in temporary configuration
    project.configurations.remove(project.configurations."$TEMP_POM_CONFIGURATION")

    return pomFile
  }

  /**
   * Generated HTML report.
   */
  private def createHtmlReport() {
    // Remove existing file
    project.file(htmlFile).delete()

    // Create directories and write report for file
    htmlFile.parentFile.mkdirs()
    htmlFile.createNewFile()
    htmlFile.withOutputStream { outputStream ->
      final def printStream = new PrintStream(outputStream)
      printStream.print(new HtmlReport(projects).string())
      printStream.println() // Add new line to file
    }

    // Log output directory for user
    logger.log(LogLevel.LIFECYCLE, "Wrote HTML report to ${getClickableFileUrl(htmlFile)}.")
  }

  /**
   * Generated JSON report.
   */
  private def createJsonReport() {
    // Remove existing file
    project.file(jsonFile).delete()

    // Create directories and write report for file
    jsonFile.parentFile.mkdirs()
    jsonFile.createNewFile()
    jsonFile.withOutputStream { outputStream ->
      final def printStream = new PrintStream(outputStream)
      printStream.println new JsonReport(projects).string()
      printStream.println() // Add new line to file
    }

    // Log output directory for user
    logger.log(LogLevel.LIFECYCLE, "Wrote JSON report to ${getClickableFileUrl(jsonFile)}.")
  }

  private def copyHtmlReport() {
   // Iterate through all asset directories
    assetDirs.each { directory ->
      final def licenseFile = new File(directory.path, OPEN_SOURCE_LICENSES + HTML_EXT)

      // Remove existing file
      project.file(licenseFile).delete()

      // Create new file
      licenseFile.parentFile.mkdirs()
      licenseFile.createNewFile()

      // Copy HTML file to the assets directory
      project.file(licenseFile << project.file(htmlFile).text)
    }
  }

  private def copyJsonReport() {
    // Iterate through all asset directories
    assetDirs.each { directory ->
      final def licenseFile = new File(directory.path, OPEN_SOURCE_LICENSES + JSON_EXT)

      // Remove existing file
      project.file(licenseFile).delete()

      // Create new file
      licenseFile.parentFile.mkdirs()
      licenseFile.createNewFile()

      // Copy JSON file to the assets directory
      project.file(licenseFile << project.file(jsonFile).text)
    }
  }

  private static def getClickableFileUrl(path) {
    new URI("file", "", path.toURI().getPath(), null, null).toString()
  }
}
