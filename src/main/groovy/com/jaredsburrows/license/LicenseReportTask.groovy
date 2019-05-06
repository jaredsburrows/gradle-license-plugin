package com.jaredsburrows.license

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.logging.LogLevel

class LicenseReportTask extends LicenseReportTaskKt {
  private final XmlParser xmlParser = new XmlParser(false, false)

  /**
   * Get POM information from the dependency artifacts.
   */
  @Override protected void generatePOMInfo() {
    // Iterate through all POMs in order from our custom POM configuration
    for (ResolvedArtifact resolvedArtifact : getProject().getConfigurations()
      .getByName(POM_CONFIGURATION).getResolvedConfiguration().getLenientConfiguration()
      .getArtifacts()) {
      File pomFile = resolvedArtifact.getFile()
      Node node = xmlParser.parse(pomFile)

      // License information
      String name = getName(node)?.trim()
      String version = node.getAt("version").text()?.trim()
      String description = node.getAt("description").text()?.trim()
      List<Developer> developers = new ArrayList<>()
      if (node.getAt("developers") != null && !node.getAt("developers").isEmpty()) {
        developers = node.developers.developer?.collect { developer ->
          new Developer(name: developer?.name?.text()?.trim())
        }
      }

      String url = node.getAt("url").text()?.trim()
      String inceptionYear = node.getAt("inceptionYear").text()?.trim()

      // Search for licenses
      List<License> licenses = findLicenses(pomFile)
      if (licenses == null || licenses.isEmpty()) {
        getLogger().log(LogLevel.WARN, "${name} dependency does not have a license.")
        licenses = new ArrayList<>()
      }

      // Search for version
      if (version == null || version.isEmpty()) {
        version = findVersion(pomFile)
      }

      // Store the information that we need
      Project project = new Project(
        name: name,
        description: description,
        version: version,
        developers: developers,
        licenses: licenses,
        url: url,
        year: inceptionYear,
        gav: resolvedArtifact.owner
      )

      projects.add(project)
    }

    // Sort POM information by name
    projects.sort { left, right -> left.getName().compareToIgnoreCase(right.getName()) }
  }

  private String findVersion(File pomFile) {
    if (pomFile == null || pomFile.length() == 0) {
      return null
    }
    Node node = xmlParser.parse(pomFile)

    // If the POM is missing a name, do not record it
    String name = getName(node)
    if (name == null || name.isEmpty()) {
      getLogger().log(LogLevel.WARN, "POM file is missing a name: ${pomFile}")
      return null
    }

    if (node.getAt("version") != null && !node.getAt("version").isEmpty()) {
      return node.getAt("version").text()?.trim()
    }

    if (node.getAt("parent") != null) {
      return findVersion(getParentPomFile(node))
    }
    return null
  }

  private String getName(Node node) {
    String name = node.getAt("name").text() ? node.getAt("name").text() :
      node.getAt("artifactId").text()
    return name?.trim()
  }

  private List<License> findLicenses(File pomFile) {
    if (pomFile == null || pomFile.length() == 0) {
      return null
    }
    Node node = xmlParser.parse(pomFile)

    // If the POM is missing a name, do not record it
    String name = getName(node)
    if (name == null || name.isEmpty()) {
      getLogger().log(LogLevel.WARN, "POM file is missing a name: ${pomFile}")
      return null
    }

    if (ANDROID_SUPPORT_GROUP_ID == node.getAt("groupId").text()) {
      return [new License(name: APACHE_LICENSE_NAME, url: APACHE_LICENSE_URL)]
    }

    // License information found
    if (node.licenses != null && !node.licenses.isEmpty()) {
      List<License> licenses = new ArrayList<>()
      node.licenses[0].license.each { license ->
        String licenseName = license.name?.text()
        String licenseUrl = license.url?.text()
        try {
          new URL(licenseUrl)
          licenseName = licenseName?.trim()?.capitalize()
          licenseUrl = licenseUrl?.trim()
          licenses.add(new License(name: licenseName, url: licenseUrl))
        } catch (Exception ignored) {
          getLogger().log(LogLevel.WARN, "${name} dependency has an invalid license URL;" +
            " skipping license")
        }
      }
      return licenses
    }
    getLogger().log(LogLevel.INFO, "Project, ${name}, has no license in POM file.")

    if (node.getAt("parent") != null) {
      return findLicenses(getParentPomFile(node))
    }
    return null
  }

  /**
   * Use Parent POM information when individual dependency license information is missing.
   */
  @Override protected File getParentPomFile(Node node) {
    // Get parent POM information
    String groupId = node?.getAt("parent")?.getAt("groupId")?.text()
    String artifactId = node?.getAt("parent")?.getAt("artifactId")?.text()
    String version = node?.getAt("parent")?.getAt("version")?.text()
    String dependency = "$groupId:$artifactId:$version@pom"

    // Add dependency to temporary configuration
    getProject().getConfigurations().create(TEMP_POM_CONFIGURATION)
    getProject().getConfigurations().getByName(TEMP_POM_CONFIGURATION).dependencies.add(
      getProject().getDependencies().add(TEMP_POM_CONFIGURATION, dependency)
    )
    return super.getParentPomFile(node)
  }
}
