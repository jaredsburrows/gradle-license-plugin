package com.jaredsburrows.license.testapp.html

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

/** Drives the screen the way a user would. */
@RunWith(AndroidJUnit4::class)
class MainScreenTest {
  @get:Rule
  val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `the report fills the screen instead of hiding behind a button`() {
    composeRule.onNodeWithText("HTML report").assertIsDisplayed()
    composeRule.onNodeWithText("Show open source licenses").assertDoesNotExist()
  }

  @Test
  fun `the webview loads the report generated into the assets`() {
    val webView = composeRule.activity.findViewById<View>(android.R.id.content).findWebView()

    assertNotNull("no WebView on screen", webView)
    assertEquals(OPEN_SOURCE_LICENSES, shadowOf(webView).lastLoadedUrl)
  }

  private fun View.findWebView(): WebView? =
    when {
      this is WebView -> this
      this is ViewGroup -> (0 until childCount).firstNotNullOfOrNull { getChildAt(it).findWebView() }
      else -> null
    }
}
