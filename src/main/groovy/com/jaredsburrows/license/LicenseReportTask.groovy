package com.jaredsburrows.license

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import javax.annotation.Nullable
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.logging.LogLevel

class LicenseReportTask extends LicenseReportTaskKt {
  private static final XmlParser xmlParser = new XmlParser(false, false)

  /**
   * Get POM information from the dependency artifacts.
   */
  @Override protected void generatePOMInfo() {
    // Iterate through all POMs in order from our custom POM configuration
    for (ResolvedArtifact resolvedArtifact : getProject()
      .getConfigurations()
      .getByName(POM_CONFIGURATION)
      .getResolvedConfiguration()
      .getLenientConfiguration()
      .getArtifacts()) {
      File pomFile = resolvedArtifact.getFile()
      Node node = xmlParser.parse(pomFile)

      // License information
      String name = getName(node).trim()
      String version = node.getAt("version").text().trim()
      String description = node.getAt("description").text().trim()
      List<Developer> developers = new ArrayList<>()
      if (!node.getAt("developers").isEmpty()) {
        for (Node developer : node.getAt("developers").getAt("developer")) {
          developers.add(new Developer(name: developer.getAt("name").text().trim()))
        }
      }

      String url = node.getAt("url").text().trim()
      String inceptionYear = node.getAt("inceptionYear").text().trim()

      // Search for licenses
      List<License> licenses = findLicenses(pomFile)
      if (licenses.isEmpty()) {
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
      return ""
    }
    Node node = xmlParser.parse(pomFile)

    // If the POM is missing a name, do not record it
    String name = getName(node)
    if (name == null || name.isEmpty()) {
      getLogger().log(LogLevel.WARN, "POM file is missing a name: ${pomFile}")
      return ""
    }

    if (!node.getAt("version").isEmpty()) {
      return node.getAt("version").text().trim()
    }

    if (!node.getAt("parent").isEmpty()) {
      return findVersion(getParentPomFile(node))
    }
    return ""
  }

  private String getName(Node node) {
    String name = !node.getAt("name").text().isEmpty() ? node.getAt("name").text() :
      node.getAt("artifactId").text()
    return name.trim()
  }

  private List<License> findLicenses(File pomFile) {
    if (pomFile == null || pomFile.length() == 0) {
      return new ArrayList<License>()
    }
    Node node = xmlParser.parse(pomFile)

    // If the POM is missing a name, do not record it
    String name = getName(node)
    if (name == null || name.isEmpty()) {
      getLogger().log(LogLevel.WARN, "POM file is missing a name: ${pomFile}")
      return new ArrayList<License>()
    }

    if (ANDROID_SUPPORT_GROUP_ID == node.getAt("groupId").text()) {
      return Arrays.asList(new License(name: APACHE_LICENSE_NAME, url: APACHE_LICENSE_URL))
    }

    // License information found
    if (!node.getAt("licenses").isEmpty()) {
      List<License> licenses = new ArrayList<>()
      for (Node license : node.getAt("licenses").get(0).getAt("license")) {
        String licenseName = license.getAt("name").text().trim()
        String licenseUrl = license.getAt("url").text().trim()
        if (isUrlValid(licenseUrl)) {
          licenses.add(new License(name: licenseName, url: licenseUrl))
        }
      }
      return licenses
    }
    getLogger().log(LogLevel.INFO, "Project, ${name}, has no license in POM file.")

    if (!node.getAt("parent").isEmpty()) {
      return findLicenses(getParentPomFile(node))
    }
    return new ArrayList<License>()
  }
}
