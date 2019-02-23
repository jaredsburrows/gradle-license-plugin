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
  static final def POM_CONFIGURATION = "poms"
  static final def TEMP_POM_CONFIGURATION = "tempPoms"
  private static final String ANDROID_SUPPORT_GROUP_ID = "com.android.support"
  private static final String APACHE_LICENSE_NAME = "The Apache Software License"
  private static final String APACHE_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
  private static final String OPEN_SOURCE_LICENSES = "open_source_licenses"
  static final String HTML_EXT = ".html"
  static final String JSON_EXT = ".json"
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

  @TaskAction public void licenseReport() {
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
  private void setupEnvironment() {
    // Create temporary configuration in order to store POM information
    getProject().getConfigurations().create(POM_CONFIGURATION)

    getProject().getConfigurations().every {
      try {
        it.canBeResolved = true
      } catch (Exception ignore) { }
    }
  }

  /**
   * Iterate through all configurations and collect dependencies.
   */
  private void collectDependencies() {
    // Add POM information to our POM configuration
    Set<Configuration> configurations = new LinkedHashSet<>()

    // Add "compile" configuration older java and android gradle plugins
    if (getProject().getConfigurations().find { it.getName() == "compile" }) configurations << getProject().getConfigurations()."compile"

    // Add "api" and "implementation" configurations for newer java-library and android gradle plugins
    if (getProject().getConfigurations().find { it.getName() == "api" }) configurations << getProject().getConfigurations()."api"
    if (getProject().getConfigurations().find { it.getName() == "implementation" }) configurations << getProject().getConfigurations()."implementation"

    // If Android project, add extra configurations
    if (variant) {
      // Add buildType configurations
      if (getProject().getConfigurations().find { it.getName() == "compile" }) configurations << getProject().getConfigurations()."${buildType}Compile"
      if (getProject().getConfigurations().find { it.getName() == "api" }) configurations << getProject().getConfigurations()."${buildType}Api"
      if (getProject().getConfigurations().find { it.getName() == "implementation" }) configurations << getProject().getConfigurations()."${buildType}Implementation"

      // Add productFlavors configurations
      productFlavors.each { flavor ->
        // Works for productFlavors and productFlavors with dimensions
        if (variant.capitalize().contains(flavor.name.capitalize())) {
          if (getProject().getConfigurations().find { it.getName() == "compile" }) configurations << getProject().getConfigurations()."${flavor.name}Compile"
          if (getProject().getConfigurations().find { it.getName() == "api" }) configurations << getProject().getConfigurations()."${flavor.name}Api"
          if (getProject().getConfigurations().find { it.getName() == "implementation" }) configurations << getProject().getConfigurations()."${flavor.name}Implementation"
        }
      }
    }

    // Iterate through all the configurations's dependencies
    configurations.each { configuration ->
      configuration.canBeResolved &&
        configuration.getResolvedConfiguration().getLenientConfiguration().getArtifacts()*.getModuleVersion().id.collect { id ->
          "$id.group:$id.name:$id.version@pom"
        }.each { pom ->
          getProject().getConfigurations()."$POM_CONFIGURATION".dependencies.add(
            getProject().getDependencies().add("$POM_CONFIGURATION", pom)
          )
        }
    }
  }

  /**
   * Get POM information from the dependency artifacts.
   */
  private void generatePOMInfo() {
    // Iterate through all POMs in order from our custom POM configuration
    getProject().getConfigurations()."$POM_CONFIGURATION".getResolvedConfiguration().getLenientConfiguration().getArtifacts().each { pom ->
      File pomFile = pom.file
      Node pomText = new XmlParser().parse(pomFile)

      // License information
      def name = getName(pomText)
      String version = pomText.version?.text()
      String description = pomText.description?.text()
      List<Developer> developers = []
      if (pomText.developers) {
        developers = pomText.developers.developer?.collect { developer ->
          new Developer(name: developer?.name?.text()?.trim())
        }
      }

      String url = pomText.url?.text()
      String year = pomText.inceptionYear?.text()

      // Clean up and format
      name = name?.capitalize()
      version = version?.trim()
      description = description?.trim()
      url = url?.trim()
      year = year?.trim()

      List<License> licenses = findLicenses(pomFile)
      if (!licenses) {
        getLogger().log(LogLevel.WARN, "${name} dependency does not have a license.")
        licenses = []
      }

      // Store the information that we need
      Project project = new Project(
        name: name,
        description: description,
        version: version,
        developers: developers,
        licenses: licenses,
        url: url,
        year: year,
        gav: pom.owner
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

  List<License> findLicenses(File pomFile) {
    if (!pomFile) {
      return null
    }
    Node pomText = new XmlParser().parse(pomFile)

    // If the POM is missing a name, do not record it
    def name = getName(pomText)
    if (!name) {
      getLogger().log(LogLevel.WARN, "POM file is missing a name: ${pomFile}")
      return null
    }

    if (ANDROID_SUPPORT_GROUP_ID == pomText.groupId?.text()) {
      return [ new License(name: APACHE_LICENSE_NAME, url: APACHE_LICENSE_URL) ]
    }

    // License information found
    if (pomText.licenses) {
      List<License> licenses = new ArrayList<>()
      pomText.licenses[0].license.each { license ->
        String licenseName = license.name?.text()
        String licenseUrl = license.url?.text()
        try {
          //noinspection GroovyResultOfObjectAllocationIgnored
          new URL(licenseUrl)
          licenseName = licenseName?.trim()?.capitalize()
          licenseUrl = licenseUrl?.trim()
          licenses << new License(name: licenseName, url: licenseUrl)
        } catch (Exception ignore) {
          getLogger().log(LogLevel.WARN, "${name} dependency has an invalid license URL; skipping license")
        }
      }
      return licenses
    }
    getLogger().log(LogLevel.INFO, "Project, ${name}, has no license in POM file.")

    def hasParent = pomText.parent != null
    if (hasParent) {
      File parentPomFile = getParentPomFile(pomText)
      return findLicenses(parentPomFile)
    }
    return null
  }

  /**
   * Use Parent POM information when individual dependency license information is missing.
   */
  private File getParentPomFile(def pomText) {
    // Get parent POM information
    String groupId = pomText?.parent?.groupId?.text()
    String artifactId = pomText?.parent?.artifactId?.text()
    String version = pomText?.parent?.version?.text()
    String dependency = "$groupId:$artifactId:$version@pom"

    // Add dependency to temporary configuration
    getProject().getConfigurations().create(TEMP_POM_CONFIGURATION)
    getProject().getConfigurations()."$TEMP_POM_CONFIGURATION".dependencies.add(
      getProject().getDependencies().add(TEMP_POM_CONFIGURATION, dependency)
    )

    File pomFile = getProject().getConfigurations()."$TEMP_POM_CONFIGURATION".getResolvedConfiguration().getLenientConfiguration().getArtifacts()?.file[0]

    // Reset dependencies in temporary configuration
    getProject().getConfigurations().remove(getProject().getConfigurations()."$TEMP_POM_CONFIGURATION")

    return pomFile
  }

  /**
   * Generated HTML report.
   */
  private void createHtmlReport() {
    // Remove existing file
    getProject().file(htmlFile).delete()

    // Create directories and write report for file
    htmlFile.getParentFile().mkdirs()
    htmlFile.createNewFile()
    htmlFile.withOutputStream { outputStream ->
      PrintStream printStream = new PrintStream(outputStream)
      printStream.print(new HtmlReport(projects).string())
      printStream.println() // Add new line to file
    }

    // Log output directory for user
    getLogger().log(LogLevel.LIFECYCLE, "Wrote HTML report to ${getClickableFileUrl(htmlFile)}.")
  }

  /**
   * Generated JSON report.
   */
  private void createJsonReport() {
    // Remove existing file
    getProject().file(jsonFile).delete()

    // Create directories and write report for file
    jsonFile.getParentFile().mkdirs()
    jsonFile.createNewFile()
    jsonFile.withOutputStream { outputStream ->
      PrintStream printStream = new PrintStream(outputStream)
      printStream.println new JsonReport(projects).string()
      printStream.println() // Add new line to file
    }

    // Log output directory for user
    getLogger().log(LogLevel.LIFECYCLE, "Wrote JSON report to ${getClickableFileUrl(jsonFile)}.")
  }

  private void copyHtmlReport() {
    // Iterate through all asset directories
    assetDirs.each { directory ->
      File licenseFile = new File(directory.getPath(), OPEN_SOURCE_LICENSES + HTML_EXT)

      // Remove existing file
      getProject().file(licenseFile).delete()

      // Create new file
      licenseFile.getParentFile().mkdirs()
      licenseFile.createNewFile()

      // Copy HTML file to the assets directory
      getProject().file(licenseFile << getProject().file(htmlFile).getText())
    }
  }

  private void copyJsonReport() {
    // Iterate through all asset directories
    assetDirs.each { directory ->
      File licenseFile = new File(directory.getPath(), OPEN_SOURCE_LICENSES + JSON_EXT)

      // Remove existing file
      getProject().file(licenseFile).delete()

      // Create new file
      licenseFile.getParentFile().mkdirs()
      licenseFile.createNewFile()

      // Copy JSON file to the assets directory
      getProject().file(licenseFile << getProject().file(jsonFile).getText())
    }
  }

  private static String getClickableFileUrl(File file) {
    return new URI("file", "", file.toURI().getPath(), null, null).toString()
  }
}
