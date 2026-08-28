package com.jaredsburrows.license.testapp.json

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Drives the screen the way a user would, against the report generated into the app assets. */
@RunWith(AndroidJUnit4::class)
class LicensesScreenTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun `lists the libraries of the report`() {
    composeRule.setContent { LicensesScreen() }

    composeRule.onNodeWithText("JSON report", substring = true).assertIsDisplayed()
    composeRule.onNodeWithText("Activity Compose", substring = true).assertIsDisplayed()
  }

  @Test
  fun `shows the license name of a library`() {
    composeRule.setContent { LicensesScreen() }

    composeRule
      .onNode(hasScrollAction())
      .performScrollToNode(hasText("android-gif-drawable", substring = true))
    composeRule.onNodeWithText("The MIT License", substring = true).assertExists()
  }
}
