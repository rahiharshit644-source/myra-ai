package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.SubtitlesCard
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AssistantState
import com.example.viewmodel.MahiMood
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun myra_subtitles_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        SubtitlesCard(
          assistantState = AssistantState.SPEAKING,
          mood = MahiMood.FLIRTY,
          liveTranscript = "Hey Myra, roast me!",
          mahiResponse = "Oh darling, your outfit already did that today! Just kidding, you're fabulous.",
          onInterrupt = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

