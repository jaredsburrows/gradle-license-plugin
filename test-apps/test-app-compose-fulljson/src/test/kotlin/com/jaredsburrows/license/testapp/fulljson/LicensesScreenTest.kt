package com.jaredsburrows.license.testapp.fulljson

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Drives the screen the way a user would, against the report generated into the app assets. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w400dp-h900dp")
class LicensesScreenTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun `lists the libraries of the report`() {
    composeRule.setContent { LicensesScreen() }

    composeRule.onNodeWithText("Full JSON report", substring = true).assertIsDisplayed()
    composeRule.onNodeWithText("Activity Compose", substring = true).assertIsDisplayed()
  }

  @Test
  fun `opening a library shows its full license text offline`() {
    composeRule.setContent { LicensesScreen() }

    composeRule
      .onNode(hasScrollAction())
      .performScrollToNode(hasText(GIF_DRAWABLE, substring = true))
    composeRule.onNodeWithText(GIF_DRAWABLE, substring = true).performClick()

    // The full text comes from license_texts, not from a URL or a WebView
    composeRule.onNodeWithText("Permission is hereby granted, free of charge", substring = true).assertExists()
    composeRule.onNodeWithText("pl.droidsonroids.gif:android-gif-drawable", substring = true).assertExists()
  }

  private companion object {
    private const val GIF_DRAWABLE = "android-gif-drawable"
  }
}
