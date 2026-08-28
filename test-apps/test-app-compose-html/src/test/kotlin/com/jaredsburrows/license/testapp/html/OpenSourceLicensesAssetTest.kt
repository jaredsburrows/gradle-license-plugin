package com.jaredsburrows.license.testapp.html

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * The HTML report is displayed as-is, so the only thing to verify is that `licenseDebugReport`
 * copied a usable report into the assets the WebView loads.
 */
@RunWith(RobolectricTestRunner::class)
class OpenSourceLicensesAssetTest {
  private val html =
    RuntimeEnvironment
      .getApplication()
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
