package com.jaredsburrows.license.internal.pom

/**
 * Represents license information in a POM file.
 * See: https://maven.apache.org/guides/introduction/introduction-to-the-pom.html
 *
 * @param name Name of the License in the POM.
 * @param url URL of the License in the POM.
 */
data class License(
  var name: String = "",
  var url: String = ""
) {
  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is License -> false
      url != other.url -> false
      else -> true
    }
  }

  override fun hashCode(): Int = url.hashCode()
}
