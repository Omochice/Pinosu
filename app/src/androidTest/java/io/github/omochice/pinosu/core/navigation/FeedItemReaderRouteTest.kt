package io.github.omochice.pinosu.core.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Rule

class FeedItemReaderRouteTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `feed url and item id survive navigation even with characters that urls reserve`() {
    val sent =
        FeedItemReader(
            feedUrl = "https://a.example/feed?format=rss&lang=ja#top",
            itemId = "tag:a.example,2024:/posts/a b%20c?d=1&e#f+g/日本語")
    var received: FeedItemReader? = null
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = Feed) {
        composable<Feed> { LaunchedEffect(Unit) { navController.navigate(sent) } }
        composable<FeedItemReader> { entry ->
          received = entry.toRoute<FeedItemReader>()
          Text("reader")
        }
      }
    }

    composeTestRule.onNodeWithText("reader").assertExists()

    assertEquals(sent, received)
  }
}
