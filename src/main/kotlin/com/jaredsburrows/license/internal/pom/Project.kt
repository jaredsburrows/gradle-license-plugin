package com.jaredsburrows.license.internal.pom

/**
 * Represents the information that is used to make HTML and JSON reports.
 */
class Project {
  var name: String? = null
  var description: String? = null
  var version: String? = null
  var licenses: List<License>? = null
  var url: String? = null
  var developers: List<Developer>? = null
  var year: String? = null
  var gav: String? = null // group/artifact/version
}
