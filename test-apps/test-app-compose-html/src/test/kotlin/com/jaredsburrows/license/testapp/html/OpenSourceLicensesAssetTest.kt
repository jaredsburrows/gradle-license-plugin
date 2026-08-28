package com.jaredsburrows.license.testapp.html

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

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
    assertTrue("no license anchor", html.contains("<pre id="))
    assertTrue("missing the Apache license text", html.contains("Apache License"))
    assertTrue("missing the MIT license text", html.contains("Permission is hereby granted, free of charge"))
  }
}
