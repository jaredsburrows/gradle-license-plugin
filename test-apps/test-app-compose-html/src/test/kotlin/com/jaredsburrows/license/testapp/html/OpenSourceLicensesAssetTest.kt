package com.jaredsburrows.license.testapp.html

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The HTML report is displayed as-is, so the only thing to verify is that `licenseDebugReport`
 * copied a usable report into the assets the WebView loads.
 */
@RunWith(AndroidJUnit4::class)
class OpenSourceLicensesAssetTest {
  private val html =
    ApplicationProvider
      .getApplicationContext<Context>()
      .assets
      .open("open_source_licenses.html")
      .bufferedReader()
      .use { it.readText() }

  @Test
  fun `the asset is the generated html report`() {
    assertTrue(html.startsWith("<!DOCTYPE html>"))
    assertTrue(html.contains("<title>Open source licenses</title>"))
    assertTrue(html.contains("Notice for packages:"))
  }

  @Test
  fun `the report contains the license texts the dependencies point at`() {
    assertTrue(html.contains("<pre id="), "no license anchor")
    assertTrue(html.contains("Apache License"), "missing the Apache license text")
    assertTrue(html.contains("Permission is hereby granted, free of charge"), "missing the MIT license text")
  }
}
