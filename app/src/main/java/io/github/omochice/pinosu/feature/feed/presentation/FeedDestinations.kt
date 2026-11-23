package io.github.omochice.pinosu.feature.feed.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.github.omochice.pinosu.core.navigation.Feed
import io.github.omochice.pinosu.core.navigation.FeedItemReader
import io.github.omochice.pinosu.core.navigation.FeedSubscriptions
import io.github.omochice.pinosu.core.navigation.Login
import io.github.omochice.pinosu.core.navigation.PostBookmark
import io.github.omochice.pinosu.core.navigation.defaultEnterTransition
import io.github.omochice.pinosu.core.navigation.defaultExitTransition
import io.github.omochice.pinosu.core.navigation.defaultPopEnterTransition
import io.github.omochice.pinosu.core.navigation.defaultPopExitTransition
import io.github.omochice.pinosu.feature.feed.presentation.ui.FeedItemReaderScreen
import io.github.omochice.pinosu.feature.feed.presentation.ui.FeedScreen
import io.github.omochice.pinosu.feature.feed.presentation.ui.FeedSubscriptionsCallbacks
import io.github.omochice.pinosu.feature.feed.presentation.ui.FeedSubscriptionsScreen
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedItemReaderViewModel
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedSubscriptionsViewModel
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedViewModel

private const val KEY_SUBSCRIPTIONS_CHANGED = "feed_subscriptions_changed"

/**
 * Add the feed timeline destination and the subscribed feeds and item reader destinations opened
 * from it
 *
 * The login state is passed as functions because the navigation graph outlives a single
 * composition, so a plain value would go stale.
 *
 * @param navController Controller used to move between and leave the feed screens
 * @param isLoggedIn Whether a user is logged in; the screens return to login when this turns false
 * @param isReadOnly Whether the user logged in read-only, which hides every action that publishes
 * @param onOpenDrawer Callback when the hamburger menu is clicked to open the drawer
 */
fun NavGraphBuilder.feedDestinations(
    navController: NavController,
    isLoggedIn: () -> Boolean,
    isReadOnly: () -> Boolean,
    onOpenDrawer: () -> Unit,
) {
  composable<Feed>(
      enterTransition = { defaultEnterTransition },
      exitTransition = { defaultExitTransition },
      popEnterTransition = { defaultPopEnterTransition },
      popExitTransition = { defaultPopExitTransition }) { backStackEntry ->
        FeedTimelineDestination(
            navController = navController,
            backStackEntry = backStackEntry,
            isLoggedIn = isLoggedIn(),
            isReadOnly = isReadOnly(),
            onOpenDrawer = onOpenDrawer)
      }

  composable<FeedSubscriptions>(
      enterTransition = { defaultEnterTransition },
      exitTransition = { defaultExitTransition },
      popEnterTransition = { defaultPopEnterTransition },
      popExitTransition = { defaultPopExitTransition }) {
        FeedSubscriptionsDestination(
            navController = navController, isLoggedIn = isLoggedIn(), isReadOnly = isReadOnly())
      }

  composable<FeedItemReader>(
      enterTransition = { defaultEnterTransition },
      exitTransition = { defaultExitTransition },
      popEnterTransition = { defaultPopEnterTransition },
      popExitTransition = { defaultPopExitTransition }) { backStackEntry ->
        FeedItemReaderDestination(
            navController = navController,
            route = backStackEntry.toRoute<FeedItemReader>(),
            isLoggedIn = isLoggedIn(),
            isReadOnly = isReadOnly())
      }
}

@Composable
private fun FeedTimelineDestination(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    isLoggedIn: Boolean,
    isReadOnly: Boolean,
    onOpenDrawer: () -> Unit,
) {
  val feedViewModel: FeedViewModel = hiltViewModel()
  val feedUiState by feedViewModel.uiState.collectAsStateWithLifecycle()
  val hasSubscriptionsChanged by
      backStackEntry.savedStateHandle
          .getStateFlow(KEY_SUBSCRIPTIONS_CHANGED, false)
          .collectAsStateWithLifecycle()

  LaunchedEffect(isLoggedIn) {
    if (!isLoggedIn) {
      navController.navigate(Login) { popUpTo<Feed> { inclusive = true } }
    }
  }

  LaunchedEffect(hasSubscriptionsChanged) {
    if (hasSubscriptionsChanged) {
      backStackEntry.savedStateHandle[KEY_SUBSCRIPTIONS_CHANGED] = false
      feedViewModel.reloadAfterSubscriptionChange()
    }
  }

  FeedScreen(
      uiState = feedUiState,
      onRefresh = feedViewModel::refresh,
      onOpenDrawer = onOpenDrawer,
      onBookmarkItem = { link, title ->
        navController.navigate(PostBookmark(sharedUrl = link, sharedTitle = title))
      },
      onManageSubscriptions = {
        navController.navigate(FeedSubscriptions) { launchSingleTop = true }
      },
      isReadOnly = isReadOnly,
      onOpenItem = { item ->
        navController.navigate(FeedItemReader(feedUrl = item.feedUrl, itemId = item.id)) {
          launchSingleTop = true
        }
      },
      onSelectCategory = feedViewModel::selectCategory)
}

@Composable
private fun FeedItemReaderDestination(
    navController: NavController,
    route: FeedItemReader,
    isLoggedIn: Boolean,
    isReadOnly: Boolean,
) {
  val viewModel =
      hiltViewModel<FeedItemReaderViewModel, FeedItemReaderViewModel.Factory> { factory ->
        factory.create(route)
      }

  LaunchedEffect(isLoggedIn) {
    if (!isLoggedIn) {
      navController.navigate(Login) { popUpTo<FeedItemReader> { inclusive = true } }
    }
  }

  FeedItemReaderScreen(
      item = viewModel.item,
      onNavigateUp = { navController.navigateUp() },
      onBookmark = { link, title ->
        navController.navigate(PostBookmark(sharedUrl = link, sharedTitle = title))
      },
      isReadOnly = isReadOnly)
}

@Composable
private fun FeedSubscriptionsDestination(
    navController: NavController,
    isLoggedIn: Boolean,
    isReadOnly: Boolean,
) {
  val viewModel: FeedSubscriptionsViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val lifecycleOwner = LocalLifecycleOwner.current
  val signLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
        ->
        viewModel.processSignedEvent(result.resultCode, result.data)
      }
  val callbacks =
      remember(viewModel) {
        FeedSubscriptionsCallbacks(
            onNavigateUp = { navController.navigateUp() },
            onRetry = viewModel::reload,
            onAdd = viewModel::add,
            onRemove = viewModel::remove,
            onSetCategory = viewModel::setCategory,
            onConfirmPublicNotice = viewModel::confirmPublicNotice,
            onDismissDialog = viewModel::dismissDialog,
            onMessageShown = viewModel::dismissMessage)
      }

  LaunchedEffect(isLoggedIn) {
    if (!isLoggedIn) {
      navController.navigate(Login) { popUpTo<FeedSubscriptions> { inclusive = true } }
    }
  }

  LaunchedEffect(viewModel, lifecycleOwner) {
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
      viewModel.signRequests.collect { signLauncher.launch(it) }
    }
  }

  LaunchedEffect(uiState.hasPublished) {
    if (uiState.hasPublished) {
      navController.previousBackStackEntry?.savedStateHandle?.set(KEY_SUBSCRIPTIONS_CHANGED, true)
    }
  }

  FeedSubscriptionsScreen(uiState = uiState, callbacks = callbacks, isReadOnly = isReadOnly)
}
