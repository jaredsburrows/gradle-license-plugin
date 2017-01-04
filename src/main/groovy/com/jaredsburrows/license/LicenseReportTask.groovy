package com.jaredsburrows.license

import com.jaredsburrows.license.internal.License
import com.jaredsburrows.license.internal.Project
import com.jaredsburrows.license.internal.report.json.JsonReport
import org.gradle.api.DefaultTask
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
    final def configurations = new LinkedHashSet<>()

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
      def projectAuthors = text.developers?.text()?.join(",")
      def projectURL = text.scm?.url?.text()
      def projectYear = text.inceptionYear?.text()
      def licenseName = text.licenses?.license?.name?.text()
      def licenseURL = text.licenses?.license?.url?.text()

      // If the POM is missing a name, do not record it
      if (!projectName) return

      if (projectName) projectName = projectName.trim()
      if (projectAuthors) projectAuthors = projectAuthors.trim()
      if (projectURL) projectURL = projectURL.trim()
      if (projectYear) projectYear = projectYear.trim()
      if (licenseName) licenseName = licenseName.trim()
      if (licenseURL) licenseURL = licenseURL.trim()

      // For all "com.android.support" libraries, add Apache 2
      if (!licenseName || !licenseURL) {
        logger.log(LogLevel.INFO, String.format("Project, %s, has no license in the POM file.", projectName))

        if (ANDROID_SUPPORT_GROUP_ID == text.groupId.text()) {
          licenseName = APACHE_LICENSE_NAME
          licenseURL = APACHE_LICENSE_URL
        } else return
      }

      // Update formatting
      projectName = projectName.capitalize()
      licenseName = licenseName.capitalize()

      final def license = License.builder()
        .name(licenseName)
        .url(licenseURL)
        .build()
      final def project = Project.builder()
        .name(projectName)
        .developers(projectAuthors)
        .license(license)
        .url(projectURL)
        .year(projectYear)
        .build()

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
    if (project.file(htmlFile).exists()) project.file(htmlFile).delete()

    // Create directories and write report for file
    htmlFile.parentFile.mkdirs()
    htmlFile.createNewFile()
    htmlFile.withOutputStream { outputStream ->
      final def printStream = new PrintStream(outputStream)

      printStream.print("<html><head><style>body{font-family:sans-serif;}pre{background-color:#eeeeee;padding:1em;" +
        "white-space:pre-wrap;}</style><title>Open source licenses</title></head><body>")

      if (projects.empty) {
        logger.log(LogLevel.INFO, "No open source libraries.")

        printStream.print("<h3>No open source libraries</h3></body></html>")
        printStream.println()
        return
      }

      printStream.print("<h3>Notice for libraries:</h3><ul>")

      // Print libraries first
      final def licenses = new HashSet<>()
      projects.each { pomInfo ->
        licenses << pomInfo.license

        printStream.print(String.format("<li><a href=\"#%s\">%s</a></li>", pomInfo.license.hashCode(), pomInfo.name))
      }
      printStream.print("</ul>")

      // Print licenses second
      licenses.each { license ->
        final def licenseName = license.name
        final def licenseUrl = license.url
        final def licenseNameUrl = String.format("%s, %s", licenseName, licenseUrl)

        printStream.print(String.format("<h3><a name=\"%s\"></a>%s</h3><pre>%s</pre>", license.hashCode(), licenseName, licenseNameUrl))
      }
      printStream.print("</body></html>")
      printStream.println() // Add new line to file
    }

    // If Android project, copy to asset directory
    if (!isJavaProject) {
      // Iterate through all asset directories
      assetDirs.each { directory ->
        final def licenseFile = new File(directory.path, OPEN_SOURCE_LICENSES + HTML_EXT)

        // Remove existing file
        if (project.file(licenseFile).exists()) project.file(licenseFile).delete()

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
    if (project.file(jsonFile).exists()) project.file(jsonFile).delete()

    // Create directories and write report for file
    jsonFile.parentFile.mkdirs()
    jsonFile.createNewFile()
    jsonFile.withOutputStream { outputStream ->
      final def printStream = new PrintStream(outputStream)

      printStream.println(new JsonReport(projects).toJson())
      printStream.println() // Add new line to file

      // Log output directory for user
      logger.log(LogLevel.LIFECYCLE, String.format("Wrote JSON report to %s.", jsonFile.absolutePath))
    }
  }
}
