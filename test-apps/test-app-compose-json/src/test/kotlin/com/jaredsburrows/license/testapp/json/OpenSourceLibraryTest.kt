package com.jaredsburrows.license.testapp.json

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Reads the report that `licenseDebugReport` generated into the app assets, so a change to the
 * report format is caught here instead of at runtime.
 */
@RunWith(RobolectricTestRunner::class)
class OpenSourceLibraryTest {
  private val libraries = RuntimeEnvironment.getApplication().readOpenSourceLibraries()

  @Test
  fun `every library has a name and a license`() {
    assertTrue("the report is empty", libraries.isNotEmpty())
    libraries.forEach { library ->
      assertTrue("missing name for $library", library.name.isNotBlank())
      assertTrue("missing license for ${library.name}", library.licenses.isNotEmpty())
    }
  }

  @Test
  fun `the MIT licensed dependency keeps its name and url`() {
    val gifDrawable = libraries.single { it.name == "android-gif-drawable" }
    val license = gifDrawable.licenses.single()

    assertEquals("1.2.29", gifDrawable.version)
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
