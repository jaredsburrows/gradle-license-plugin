package com.jaredsburrows.license.internal.pom

/**
 * Represents developer information in a
 * [POM](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html) file.
 *
 * @property name name of the [Developer] in the POM.
 */
data class Developer(
  var name: String = ""
)
