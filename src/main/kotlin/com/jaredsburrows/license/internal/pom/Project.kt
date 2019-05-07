package com.jaredsburrows.license.internal.pom

/**
 * Represents the information that is used to make HTML and JSON reports.
 *
 * @param name Name of the library in the POM.
 * @param description Description of the library in the POM.
 * @param version Version of the library in the POM.
 * @param licenses List of Licenses of the library listed in the POM.
 * @param url URL of the library listed in the POM.
 * @param developers List of Developers of the library listed in the POM.
 * @param year Year of the library in the POM.
 * @param gav Group, Artifact and Version.
 */
data class Project(
  var name: String = "",
  var description: String = "",
  var version: String = "",
  var licenses: List<License> = listOf(),
  var url: String = "",
  var developers: List<Developer> = listOf(),
  var year: String = "",
  var gav: String = "" // group/artifact/version
)
