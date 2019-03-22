package com.jaredsburrows.license.internal.pom

/**
 * Represents the information that is used to make HTML and JSON reports.
 */
class Project {
  /** Name of the library in the POM. */
  var name: String? = null
  /** Description of the library in the POM. */
  var description: String? = null
  /** Version of the library in the POM. */
  var version: String? = null
  /** List of Licenses of the library listed in the POM. */
  var licenses: List<License>? = null
  /** URL of the library listed in the POM. */
  var url: String? = null
  /** List of Developers of the library listed in the POM. */
  var developers: List<Developer>? = null
  /** Year of the library in the POM. */
  var year: String? = null
  /** Group, Artifact and Version. */
  var gav: String? = null // group/artifact/version
}
