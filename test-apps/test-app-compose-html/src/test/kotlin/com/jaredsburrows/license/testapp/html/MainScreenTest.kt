package com.jaredsburrows.license.testapp.html

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Drives the screen the way a user would. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w400dp-h900dp")
class MainScreenTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun `the licenses dialog opens on demand`() {
    composeRule.setContent { MainScreen() }

    composeRule.onNodeWithText("Open source licenses").assertDoesNotExist()
    composeRule.onNodeWithText("Show open source licenses").performClick()
    composeRule.onNodeWithText("Open source licenses").assertIsDisplayed()
  }
}
