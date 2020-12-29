package com.jaredsburrows.license.internal.report

/** Used to be the base configuration for each report. */
interface Report {
  /** Return a pretty print of the report. */
  override fun toString(): String

  /** Return the report with license or empty report if there are none. */
  fun report(): String

  /** Return the full report if are open source licenses are found. */
  fun fullReport(): String

  /** Return the empty report if no open source licenses are found. */
  fun emptyReport(): String

  fun String.valueOrNull(): String? = if (this.isNotEmpty()) this else null
}
