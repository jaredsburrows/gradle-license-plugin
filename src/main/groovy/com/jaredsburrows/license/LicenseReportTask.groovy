package com.jaredsburrows.license

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import com.jaredsburrows.license.internal.report.HtmlReport
import com.jaredsburrows.license.internal.report.JsonReport
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.*
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
class LicenseReportTask extends DefaultTask {
  final static POM_CONFIGURATION = "poms"
  final static TEMP_POM_CONFIGURATION = "tempPoms"
  final static ANDROID_SUPPORT_GROUP_ID = "com.android.support"
  final static APACHE_LICENSE_NAME = "The Apache Software License"
  final static APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
  final static OPEN_SOURCE_LICENSES = "open_source_licenses"
  final static HTML_EXT = ".html"
  final static JSON_EXT = ".json"
  @Internal final List<Project> projects = []
  @Optional @Input File[] assetDirs = []
  @Optional @Input buildType
  @Optional @Input variant
  @Optional @Internal productFlavors = []
  @OutputFile File htmlFile
  @OutputFile File jsonFile

  @SuppressWarnings("GroovyUnusedDeclaration") @TaskAction licenseReport() {
    setupEnvironment()
    collectDependencies()
    generatePOMInfo()
    createHTMLReport()
    createJsonReport()
  }

  /**
   * Setup configurations to collect dependencies.
   */
  def setupEnvironment() {
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
  def collectDependencies() {
    // Add POM information to our POM configuration
    final Set<Configuration> configurations = new LinkedHashSet<>()

    // Add 'compile' configuration older java and android gradle plugins
    if (project.configurations.find { it.name == "compile" }) configurations << project.configurations."compile"

    // Add 'api' and 'implementation' configurations for newer java-library and android gradle plugins
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
  def generatePOMInfo() {
    // Iterate through all POMs in order from our custom POM configuration
    project.configurations."$POM_CONFIGURATION".resolvedConfiguration.lenientConfiguration.artifacts.each { pom ->
      final pomFile = pom.file
      final pomText = new XmlParser().parse(pomFile)

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
        logger.log(LogLevel.WARN, String.format("%s dependency does not have a license.", name))
        return
      }

      // Store the information that we need
      final project = new Project(
        name: name,
        description: description,
        version: version,
        developers: developers,
        licenses: licenses,
        url: url,
        year: year
      )

      projects << project
    }

    // Sort POM information by name
    projects.sort { project -> project.name }
  }

  def getName(def pomText) {
    def name = pomText.name?.text() ? pomText.name?.text() : pomText.artifactId?.text()
    return name?.trim()
  }

  def findLicenses(File pomFile) {
    if (!pomFile) {
      return null
    }
    final pomText = new XmlParser().parse(pomFile)

    // If the POM is missing a name, do not record it
    final name = getName(pomText)
    if (!name) {
      logger.log(LogLevel.WARN, String.format("POM file is missing a name: %s", pomFile))
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
          new URL(licenseUrl)
            licenseName = licenseName?.trim().capitalize()
            licenseUrl = licenseUrl?.trim()
            licenses << new License(name: licenseName, url: licenseUrl)
        } catch (Exception ignore) {
          logger.log(LogLevel.WARN, String.format("%s dependency has an invalid license URL; skipping license", name))
        }
      }
      return licenses
    }
    logger.log(LogLevel.INFO, String.format("Project, %s, has no license in POM file.", name))

    final hasParent = pomText.parent != null
    if (hasParent) {
      final parentPomFile = getParentPomFile(pomText)
      return findLicenses(parentPomFile)
    }
    return null
  }

  /**
   * Use Parent POM information when individual dependency license information is missing.
   */
  def getParentPomFile(def pomText) {
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
  def createHTMLReport() {
    // Remove existing file
    project.file(htmlFile).delete()

    // Create directories and write report for file
    htmlFile.parentFile.mkdirs()
    htmlFile.createNewFile()
    htmlFile.withOutputStream { outputStream ->
      final printStream = new PrintStream(outputStream)
      printStream.print(new HtmlReport(projects).string())
      printStream.println() // Add new line to file
    }

    // If Android project, copy to asset directory
    if (variant) {
      // Iterate through all asset directories
      assetDirs.each { directory ->
        final licenseFile = new File(directory.path, OPEN_SOURCE_LICENSES + HTML_EXT)

        // Remove existing file
        project.file(licenseFile).delete()

        // Create new file
        licenseFile.parentFile.mkdirs()
        licenseFile.createNewFile()

        // Copy HTML file to the assets directory
        project.file(licenseFile << project.file(htmlFile).text)
      }
    }

    // Log output directory for user
    logger.log(LogLevel.LIFECYCLE, String.format("Wrote HTML report to %s.", htmlFile.absolutePath))
  }

  /**
   * Generated JSON report.
   */
  def createJsonReport() {
    // Remove existing file
    project.file(jsonFile).delete()

    // Create directories and write report for file
    jsonFile.parentFile.mkdirs()
    jsonFile.createNewFile()
    jsonFile.withOutputStream { outputStream ->
      final printStream = new PrintStream(outputStream)
      printStream.println new JsonReport(projects).string()
      printStream.println() // Add new line to file
    }

    // Log output directory for user
    logger.log(LogLevel.LIFECYCLE, String.format("Wrote JSON report to %s.", jsonFile.absolutePath))
  }
}
