package com.jaredsburrows.license

import com.jaredsburrows.license.internal.License
import com.jaredsburrows.license.internal.Project
import com.jaredsburrows.license.internal.report.html.HtmlReport
import com.jaredsburrows.license.internal.report.json.JsonReport
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
class LicenseReportTask extends DefaultTask {
  final static def POM_CONFIGURATION = "poms"
  final static def ANDROID_SUPPORT_GROUP_ID = "com.android.support"
  final static def APACHE_LICENSE_NAME = "The Apache Software License"
  final static def APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
  final static def OPEN_SOURCE_LICENSES = "open_source_licenses"
  final static def HTML_EXT = ".html"
  final static def JSON_EXT = ".json"
  final List<Project> projects = []
  File[] assetDirs = []
  def buildType
  def variant
  def productFlavors = []
  boolean isJavaProject
  @OutputFile File htmlFile
  @OutputFile File jsonFile

  @TaskAction def licenseReport() {
    isJavaProject = !variant

    generatePOMInfo()
    createHTMLReport()
    createJsonReport()
  }

  def generatePOMInfo() {
    // Create temporary configuration in order to store POM information
    project.configurations.create(POM_CONFIGURATION)

    // Add POM information to our POM configuration
    final Set<Configuration> configurations = new LinkedHashSet<>()

    // Add default compile configuration
    configurations << project.configurations.compile

    // If Android project, add extra configurations
    if (!isJavaProject) {
      // Add buildType compile configuration
      configurations << project.configurations."${buildType}Compile"
      // Add productFlavors compile configuration
      productFlavors.each { flavor ->
        // Works for productFlavors and productFlavors with dimensions
        if (variant.capitalize().contains(flavor.name.capitalize()))
          configurations << project.configurations."${flavor.name}Compile"
      }
    }

    // Iterate through all "compile" configurations's dependencies
    configurations.each { configuration ->
      configuration.dependencies.each { dependency ->
        project.dependencies {
          poms(
            group: dependency.group,
            name: dependency.name,
            version: dependency.version,
            ext: "pom"
          )
        }
      }
    }

    // Iterate through all POMs in order from our custom POM configuration
    project.configurations.poms.each { pom ->
      final def text = new XmlParser().parse(pom)

      def projectName = text.name?.text() ? text.name?.text() : text.artifactId?.text()
      def projectDevelopers = text.developers?.developer?.collect { developer -> developer?.name?.text() }?.join(", ")
      def projectURL = text.scm?.url?.text()
      def projectYear = text.inceptionYear?.text()
      def licenseName = text.licenses?.license?.name?.text()
      def licenseURL = text.licenses?.license?.url?.text()

      // If the POM is missing a name, do not record it
      if (!projectName) return

      projectName = projectName?.trim()
      projectDevelopers = projectDevelopers?.trim()
      projectURL = projectURL?.trim()
      projectYear = projectYear?.trim()
      licenseName = licenseName?.trim()
      licenseURL = licenseURL?.trim()

      // For all "com.android.support" libraries, add Apache 2
      if (!licenseName || !licenseURL) {
        logger.log(LogLevel.INFO, String.format("Project, %s, has no license in the POM file.", projectName))

        if (ANDROID_SUPPORT_GROUP_ID == text.groupId?.text()) {
          licenseName = APACHE_LICENSE_NAME
          licenseURL = APACHE_LICENSE_URL
        } else return
      }

      // Update formatting
      projectName = projectName?.capitalize()
      licenseName = licenseName?.capitalize()

      final def license = new License(name: licenseName,
        url: licenseURL)
      final def project = new Project(name: projectName,
        developers: projectDevelopers,
        license: license,
        url: projectURL,
        year: projectYear)

      projects << project
    }

    // Sort POM information by name
    projects.sort { project -> project.name }
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
      final def printStream = new PrintStream(outputStream)

      printStream.print(new HtmlReport(projects).string())
      printStream.println() // Add new line to file
    }

    // If Android project, copy to asset directory
    if (!isJavaProject) {
      // Iterate through all asset directories
      assetDirs.each { directory ->
        final def licenseFile = new File(directory.path, OPEN_SOURCE_LICENSES + HTML_EXT)

        // Remove existing file
        project.file(licenseFile).delete()

        // Create new file
        licenseFile.parentFile.mkdirs()
        licenseFile.createNewFile()

        // Copy HTML file to the assets directory
        project.file(licenseFile) << project.file(htmlFile).text
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
      final def printStream = new PrintStream(outputStream)

      printStream.println(new JsonReport(projects).string())
      printStream.println() // Add new line to file
    }

    // Log output directory for user
    logger.log(LogLevel.LIFECYCLE, String.format("Wrote JSON report to %s.", jsonFile.absolutePath))
  }
}
