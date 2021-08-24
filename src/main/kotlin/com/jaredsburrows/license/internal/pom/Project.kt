package com.jaredsburrows.license.internal.pom

/**
 * Represents the information that is used to make reports.
 *
 * @property name name of the library in the POM.
 * @property description description of the library in the POM.
 * @property version version of the library in the POM.
 * @property licenses list of licenses of the library listed in the POM.
 * @property url URL of the library listed in the POM.
 * @property developers list of developers of the library listed in the POM.
 * @property year year of the library in the POM.
 * @property gav group, artifact and version.
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
