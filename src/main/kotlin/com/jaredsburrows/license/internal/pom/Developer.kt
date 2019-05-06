package com.jaredsburrows.license.internal.pom

/**
 * Represents developer information in a POM file.
 * See: https://maven.apache.org/guides/introduction/introduction-to-the-pom.html
 *
 * @param name Name of the Developer in the POM.
 */
data class Developer(
  var name: String? = null
)
