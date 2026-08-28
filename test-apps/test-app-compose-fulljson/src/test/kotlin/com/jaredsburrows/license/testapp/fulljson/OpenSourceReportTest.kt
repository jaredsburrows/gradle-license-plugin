package com.jaredsburrows.license.testapp.fulljson

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
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
    assertTrue("the report is empty", report.dependencies.isNotEmpty())
    report.dependencies.forEach { library ->
      assertTrue("missing name for ${library.dependency}", library.name.isNotBlank())
      assertTrue("missing coordinates for ${library.name}", library.dependency.orEmpty().isNotBlank())
    }
  }

  @Test
  fun `each license text is stored once and referenced by key`() {
    val licensedDependencies = report.dependencies.count { library -> library.licenses.any { it.key != null } }

    assertTrue("nothing references a bundled license", licensedDependencies > 0)
    assertTrue(
      "expected the texts to be interned, got ${report.licenseTexts.size} for $licensedDependencies dependencies",
      report.licenseTexts.size < licensedDependencies,
    )
    report.licenseTexts.forEach { (key, text) ->
      assertTrue("empty license text for $key", text.isNotBlank())
    }
  }

  @Test
  fun `every license key resolves to a text`() {
    report.dependencies.forEach { library ->
      library.licenses.filter { it.key != null }.forEach { license ->
        assertNotNull("no text for ${license.key} of ${library.name}", report.textFor(license))
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
