package com.jaredsburrows.license

import com.jaredsburrows.license.internal.License
import com.jaredsburrows.license.internal.Project
import org.gradle.api.DefaultTask
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
  final static def PLUGIN_REPOSITORY = "https://plugins.gradle.org/m2/"
  final static def ANDROID_REPOSITORY = "file://${System.env.ANDROID_HOME}/extras/android/m2repository"
  final static def OPEN_SOURCE_LICENSES = "open_source_licenses.html"
  def projects = []
  def assetDirs = []
  def buildType
  def variant
  def productFlavors = []
  @OutputFile File htmlFile

  @TaskAction def licenseReport() {
    generatePOMInfo()
    createHTMLFile()
  }

  def generatePOMInfo() {
    // Create temporary configuration in order to store POM information
    project.configurations.create(POM_CONFIGURATION)

    // Add repositories for Android repositories
    project.repositories {
      maven { url ANDROID_REPOSITORY }  // Local dependencies downloaded from Android SDK manager
      maven { url PLUGIN_REPOSITORY }   // Plugin repository Should proxy jcenter()/mavenCentral()
      mavenCentral()                    // If not in plugin repository, try maven central next
      jcenter()                         // If all else fails, try jcenter
    }

    // Add POM information to our POM configuration
    def configurations = new LinkedHashSet<>()
    configurations << project.configurations.compile                    // Add default compile configuration
    configurations << project.configurations."${buildType}Compile"      // Add buildType compile configuration
    productFlavors.each { flavor ->                                     // Add productFlavors compile configuration
      // Works for productFlavors and productFlavors with dimensions
      if (variant.capitalize().contains(flavor.name.capitalize()))
        configurations << project.configurations."${flavor.name}Compile"
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

      def projectName = text.name.text().trim() ? text.name.text() : text.artifactId.text()
      def licenseName = text.licenses?.license?.name?.text()
      def licenseURL = text.licenses?.license?.url?.text()

      projectName = projectName.trim()
      licenseName = licenseName.trim()
      licenseURL = licenseURL.trim()

      // If the POM is missing a name, do not record it
      if (!projectName) return

      // For all "com.android.support" libraries, add Apache 2
      if (!licenseName || !licenseURL) {
        if (ANDROID_SUPPORT_GROUP_ID == text.groupId.text()) {
          licenseName = APACHE_LICENSE_NAME
          licenseURL = APACHE_LICENSE_URL
        } else {
          return
        }
      }

      // Update formatting
      projectName = projectName.capitalize()
      licenseName = licenseName.capitalize()

      final def license = new License.Builder().name(licenseName).url(licenseURL).build()
      final def info = new Project.Builder().name(projectName).license(license).build()

      projects << info
    }

    // Sort POM information by name
    projects = projects.sort { project -> project.name }
  }

  /**
   * Generated HTML license file.
   */
  def createHTMLFile() {
    htmlFile.parentFile.mkdirs()
    htmlFile.createNewFile()
    htmlFile.withOutputStream { outputStream ->
      final def printStream = new PrintStream(outputStream)

      printStream.print("<html><head><style>body{font-family:sans-serif;}pre{background-color:#eeeeee;padding:1em;" +
        "white-space:pre-wrap;}</style><title>Open source licenses</title></head><body>")

      if (projects.empty) {
        printStream.print("<h3>No open source libraries</h3>")
        printStream.print("</body></html>")
        printStream.println()
        return
      }

      printStream.print("<h3>Notice for libraries:</h3><ul>")

      // Print libraries first
      final def licenses = new HashSet<>()
      projects.each { pomInfo ->
        final def projectName = pomInfo.name

        licenses << pomInfo.license

        printStream.print("<li><a href=\"#")
        printStream.print(pomInfo.license.hashCode())
        printStream.print("\">")
        printStream.print(projectName)
        printStream.print("</a></li>")
      }
      printStream.println("</ul>")

      // Print licenses second
      licenses.each { license ->
        final def licenseName = license.name
        final def licenseUrl = license.url
        final def licenseNameUrl = licenseName + ", " + licenseUrl

        printStream.print("<h3><a name=\"")
        printStream.print(license.hashCode())
        printStream.print("\"></a>")
        printStream.print(licenseName)
        printStream.print("</h3><pre>")
        printStream.print(licenseNameUrl)
        printStream.print("</pre>")
      }
      printStream.print("</body></html>")
      printStream.println()
    }

    // Copy HTML file to the assets directory
    assetDirs.each { directory ->
      final def licenseFile = new File(directory.path, OPEN_SOURCE_LICENSES)
      licenseFile.parentFile.mkdirs()
      licenseFile.createNewFile()

      project.file(licenseFile) << project.file(htmlFile).text
    }
  }
}
