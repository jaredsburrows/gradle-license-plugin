package com.jaredsburrows.license.testapp.json

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Reads the report that `licenseDebugReport` generated into the app assets, so a change to the
 * report format is caught here instead of at runtime.
 */
@RunWith(AndroidJUnit4::class)
class OpenSourceLibraryTest {
  private val libraries = ApplicationProvider.getApplicationContext<Context>().readOpenSourceLibraries()

  @Test
  fun `every library has a name and a license`() {
    assertTrue(libraries.isNotEmpty(), "the report is empty")
    libraries.forEach { library ->
      assertTrue(library.name.isNotBlank(), "missing name for $library")
      assertTrue(library.licenses.isNotEmpty(), "missing license for ${library.name}")
    }
  }

  @Test
  fun `the MIT licensed dependency keeps its name and url`() {
    val gifDrawable = libraries.single { it.name == "android-gif-drawable" }
    val license = gifDrawable.licenses.single()

    // Deliberately not the exact version: Renovate bumps the dependency and the report follows it,
    // so asserting a literal here only breaks the build on an unrelated upgrade.
    assertTrue(gifDrawable.version.orEmpty().isNotBlank(), "the report should carry a version")
    assertEquals("The MIT License", license.name)
    assertEquals("https://spdx.org/licenses/MIT.html", license.url)
  }

  @Test
  fun `missing values are read as null rather than the string null`() {
    val libraries =
      parseOpenSourceLibraries(
        """
        [{"project":"Example","description":null,"version":null,"developers":[],"url":null,
          "year":null,"licenses":[],"dependency":"group:example:1.0.0"}]
        """.trimIndent(),
      )

    assertEquals("Example", libraries.single().name)
    assertNull(libraries.single().version)
    assertNull(libraries.single().url)
    assertTrue(libraries.single().licenses.isEmpty())
  }
}
