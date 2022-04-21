package com.jaredsburrows.license.internal.report

/** Used to be the base configuration for each report. */
internal interface Report {
  /** Return a pretty print of the report. */
  override fun toString(): String

  /** Return the tag/name of the report */
  fun name(): String

  /** Return the extension of the report */
  fun extension(): String

  /** Return the report with license or empty report if there are none. */
  fun report(): String

  /** Return the full report if are open source licenses are found. */
  fun fullReport(): String

  /** Return the empty report if no open source licenses are found. */
  fun emptyReport(): String

  /** Return null if value does not exist. */
  fun String.valueOrNull(): String? = this.ifEmpty { null }
}
