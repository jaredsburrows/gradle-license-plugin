package com.jaredsburrows.license.testapp.fulljson

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.runner.RunWith

/**
 * Reads the report that `licenseDebugReport` generated into the app assets, so a change to the
 * report format is caught here instead of at runtime.
 */
@RunWith(AndroidJUnit4::class)
class OpenSourceReportTest {
  private val report = ApplicationProvider.getApplicationContext<Context>().readOpenSourceReport()

  @Test
  fun `every dependency has a name and coordinates`() {
    assertTrue(report.dependencies.isNotEmpty(), "the report is empty")
    report.dependencies.forEach { library ->
      assertTrue(library.name.isNotBlank(), "missing name for ${library.dependency}")
      assertTrue(library.dependency.orEmpty().isNotBlank(), "missing coordinates for ${library.name}")
    }
  }

  @Test
  fun `each license text is stored once and referenced by key`() {
    val licensedDependencies = report.dependencies.count { library -> library.licenses.any { it.key != null } }

    assertTrue(licensedDependencies > 0, "nothing references a bundled license")
    assertTrue(
      report.licenseTexts.size < licensedDependencies,
      "expected the texts to be interned, got ${report.licenseTexts.size} for $licensedDependencies dependencies",
    )
    report.licenseTexts.forEach { (key, text) ->
      assertTrue(text.isNotBlank(), "empty license text for $key")
    }
  }

  @Test
  fun `every license key resolves to a text`() {
    report.dependencies.forEach { library ->
      library.licenses.filter { it.key != null }.forEach { license ->
        assertNotNull(report.textFor(license), "no text for ${license.key} of ${library.name}")
      }
    }
  }

  @Test
  fun `the MIT licensed dependency carries the MIT text`() {
    val gifDrawable = report.dependencies.single { it.dependency.orEmpty().startsWith("pl.droidsonroids.gif:") }
    val license = gifDrawable.licenses.single()

    assertEquals("The MIT License", license.name)
    assertEquals("mit", license.key)
    assertTrue(report.textFor(license).orEmpty().contains("Permission is hereby granted, free of charge"))
  }
}
