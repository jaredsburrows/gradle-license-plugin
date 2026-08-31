package com.jaredsburrows.license.testapp.fulljson

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test

/** Drives the screen the way a user would, against the report generated into the app assets. */
@RunWith(AndroidJUnit4::class)
class LicensesScreenTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun `lists the library names of the report`() {
    composeRule.setContent { LicensesScreen() }

    composeRule.onNodeWithText("Full JSON report", substring = true).assertIsDisplayed()
    // The rows are the library name alone, the version and the license name are not shown
    composeRule.onNodeWithText("Activity Compose").assertIsDisplayed()
    composeRule.onNodeWithText("Activity Compose (", substring = true).assertDoesNotExist()
  }

  @Test
  fun `opening a library shows nothing but its full license text`() {
    composeRule.setContent { LicensesScreen() }

    composeRule
      .onNode(hasScrollAction())
      .performScrollToNode(hasText(GIF_DRAWABLE, substring = true))
    composeRule.onNodeWithText(GIF_DRAWABLE, substring = true).performClick()

    // The full text comes from license_texts, not from a URL or a WebView
    composeRule.onNodeWithText("Permission is hereby granted, free of charge", substring = true).assertExists()
    // The detail screen is the license itself, nothing else
    composeRule.onNodeWithText("pl.droidsonroids.gif:android-gif-drawable", substring = true).assertDoesNotExist()
  }

  private companion object {
    private const val GIF_DRAWABLE = "android-gif-drawable"
  }
}
