package com.jaredsburrows.license.internal

import org.gradle.api.UncheckedIOException
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

/**
 * Renders information in a format suitable for logging to the console.
 *
 * Taken from: https://github.com/gradle/gradle/blob/f3828bbb3350292dcbea7f505464eb5d30cb9d44/platforms/core-runtime/logging/src/main/java/org/gradle/internal/logging/ConsoleRenderer.java
 */
class ConsoleRenderer {
  /** Renders a path name as a file URL that is likely recognized by consoles. */
  fun asClickableFileUrl(path: File): String {
    // File.toURI().toString() leads to an URL like this on Mac: file:/reports/index.html
    // This URL is not recognized by the Mac console (too few leading slashes). We solve
    // this be creating an URI with an empty authority.
    try {
      return URI("file", "", path.toURI().path, null, null).toString()
    } catch (e: URISyntaxException) {
      throw UncheckedException.throwAsUncheckedException(e)
    }
  }
}

/**
 * Wraps a checked exception. Carries no other context.
 *
 * Taken from: https://github.com/gradle/gradle/blob/f3828bbb3350292dcbea7f505464eb5d30cb9d44/platforms/core-runtime/base-services/src/main/java/org/gradle/internal/UncheckedException.java
 */
private class UncheckedException : RuntimeException {
  constructor(cause: Throwable) : super(cause)
  constructor(message: String, cause: Throwable) : super(message, cause)

  companion object {
    /** Always throws the failure in some form. The return value is to keep the compiler happy. */
    fun throwAsUncheckedException(
      t: Throwable,
      preserveMessage: Boolean = false,
    ): RuntimeException {
      if (t is RuntimeException) {
        throw t
      }

      if (t is Error) {
        throw t
      }

      if (t is IOException) {
        if (preserveMessage) {
          throw UncheckedIOException(t.message.orEmpty(), t)
        } else {
          throw UncheckedIOException(t)
        }
      }

      if (preserveMessage) {
        throw UncheckedException(t.message.orEmpty(), t)
      } else {
        throw UncheckedException(t)
      }
    }
  }
}
