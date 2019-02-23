package com.jaredsburrows.license.internal.pom

/**
 * Represents license information in a POM file.
 */
class License {
  var name: String? = null
  var url: String? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is License) return false
    if (url != other.url) return false
    return true
  }

  override fun hashCode(): Int {
    return url?.hashCode() ?: 0
  }
}
