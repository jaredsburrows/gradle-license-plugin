package com.jaredsburrows.license

import com.jaredsburrows.license.internal.pom.Developer
import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.logging.LogLevel

class LicenseReportTask extends LicenseReportTaskKt {
//  private static final XmlParser xmlParser = new XmlParser(false, false)
//
//  /**
//   * Get POM information from the dependency artifacts.
//   */
//  @Override protected void generatePOMInfo() {
//    // Iterate through all POMs in order from our custom POM configuration
//    for (ResolvedArtifact resolvedArtifact : getProject()
//      .getConfigurations()
//      .getByName(POM_CONFIGURATION)
//      .getResolvedConfiguration()
//      .getLenientConfiguration()
//      .getArtifacts()) {
//      File pomFile = resolvedArtifact.getFile()
//      Node node = xmlParser.parse(pomFile)
//
//      // License information
//      String name = getName(node).trim()
//      String version = node.getAt("version").text().trim()
//      String description = node.getAt("description").text().trim()
//      List<Developer> developers = new ArrayList<>()
//      if (!node.getAt("developers").isEmpty()) {
//        for (Node developer : node.getAt("developers").getAt("developer")) {
//          developers.add(new Developer(name: developer.getAt("name").text().trim()))
//        }
//      }
//
//      String url = node.getAt("url").text().trim()
//      String inceptionYear = node.getAt("inceptionYear").text().trim()
//
//      // Search for licenses
//      List<License> licenses = findLicenses(pomFile)
//      if (licenses.isEmpty()) {
//        getLogger().log(LogLevel.WARN, "${name} dependency does not have a license.")
//        licenses = new ArrayList<>()
//      }
//
//      // Search for version
//      if (version == null || version.isEmpty()) {
//        version = findVersion(pomFile)
//      }
//
//      // Store the information that we need
//      Project project = new Project(
//        name: name,
//        description: description,
//        version: version,
//        developers: developers,
//        licenses: licenses,
//        url: url,
//        year: inceptionYear,
//        gav: resolvedArtifact.owner
//      )
//
//      projects.add(project)
//    }
//
//    // Sort POM information by name
//    projects.sort { left, right -> left.getName().compareToIgnoreCase(right.getName()) }
//  }
}
