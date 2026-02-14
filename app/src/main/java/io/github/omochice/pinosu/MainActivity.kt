package io.github.omochice.pinosu

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import io.github.omochice.pinosu.core.navigation.AppInfo
import io.github.omochice.pinosu.core.navigation.Bookmark
import io.github.omochice.pinosu.core.navigation.BookmarkDetail
import io.github.omochice.pinosu.core.navigation.License
import io.github.omochice.pinosu.core.navigation.Login
import io.github.omochice.pinosu.core.navigation.Main
import io.github.omochice.pinosu.core.navigation.PostBookmark
import io.github.omochice.pinosu.core.navigation.Route
import io.github.omochice.pinosu.core.navigation.Settings
import io.github.omochice.pinosu.core.navigation.defaultEnterTransition
import io.github.omochice.pinosu.core.navigation.defaultExitTransition
import io.github.omochice.pinosu.core.navigation.defaultPopEnterTransition
import io.github.omochice.pinosu.core.navigation.defaultPopExitTransition
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.feature.appinfo.presentation.model.AppInfoUiState
import io.github.omochice.pinosu.feature.appinfo.presentation.ui.AppInfoScreen
import io.github.omochice.pinosu.feature.auth.presentation.ui.LoginScreen
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginViewModel
import io.github.omochice.pinosu.feature.bookmark.presentation.ui.BookmarkScreen
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkViewModel
import io.github.omochice.pinosu.feature.comment.presentation.ui.BookmarkDetailScreen
import io.github.omochice.pinosu.feature.comment.presentation.viewmodel.BookmarkDetailViewModel
import io.github.omochice.pinosu.feature.license.presentation.ui.LicenseScreen
import io.github.omochice.pinosu.feature.main.presentation.ui.MainScreen
import io.github.omochice.pinosu.feature.postbookmark.presentation.ui.PostBookmarkScreen
import io.github.omochice.pinosu.feature.postbookmark.presentation.viewmodel.PostBookmarkViewModel
import io.github.omochice.pinosu.feature.settings.presentation.ui.SettingsScreen
import io.github.omochice.pinosu.feature.settings.presentation.viewmodel.SettingsViewModel
import io.github.omochice.pinosu.feature.shareintent.domain.model.SharedContent
import io.github.omochice.pinosu.feature.shareintent.domain.usecase.ExtractSharedContentUseCase
import io.github.omochice.pinosu.ui.drawer.AppDrawer
import io.github.omochice.pinosu.ui.theme.PinosuTheme
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Main Activity for Pinosu application
 *
 * Entry point of the app that sets up Hilt dependency injection and Compose UI. Handles NIP-55
 * signer integration for Nostr authentication and ACTION_SEND intents for shared content.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val loginViewModel: LoginViewModel by viewModels()

  @Inject lateinit var nip55SignerClient: Nip55SignerClient

  @Inject lateinit var extractSharedContentUseCase: ExtractSharedContentUseCase

  @androidx.annotation.VisibleForTesting
  internal var pendingSharedContent by mutableStateOf<SharedContent?>(null)

  @androidx.annotation.VisibleForTesting internal var contentConsumed = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    loginViewModel.checkLoginState()

    contentConsumed = savedInstanceState?.getBoolean(KEY_CONTENT_CONSUMED) == true
    if (!contentConsumed) {
      pendingSharedContent = extractSharedContentUseCase(intent)
    }

    setContent {
      PinosuTheme {
        PinosuApp(
            viewModel = loginViewModel,
            nip55SignerClient = nip55SignerClient,
            pendingSharedContent = pendingSharedContent,
            onSharedContentConsumed = {
              pendingSharedContent = null
              contentConsumed = true
            })
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(KEY_CONTENT_CONSUMED, contentConsumed)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    contentConsumed = false
    pendingSharedContent = extractSharedContentUseCase(intent)
  }

  companion object {
    private const val KEY_CONTENT_CONSUMED = "shared_content_consumed"
  }
}

/**
 * Root composable for Pinosu application
 *
 * Sets up navigation and handles authentication state transitions between login, main, and bookmark
 * screens. Includes drawer navigation for authenticated screens.
 *
 * @param viewModel ViewModel managing login/logout state
 * @param nip55SignerClient Client for NIP-55 communication
 * @param pendingSharedContent Content received from an external share intent, or null
 * @param onSharedContentConsumed Callback invoked after navigating to consume the shared content
 */
@Composable
fun PinosuApp(
    viewModel: LoginViewModel,
    nip55SignerClient: Nip55SignerClient,
    pendingSharedContent: SharedContent? = null,
    onSharedContentConsumed: () -> Unit = {}
) {
  val navController = rememberNavController()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val mainUiState by viewModel.mainUiState.collectAsStateWithLifecycle()
  val loginUiState by viewModel.uiState.collectAsStateWithLifecycle()

  val nip55Launcher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.processNip55Response(result.resultCode, result.data)
          }

  val startDestination: Route = mainUiState.userPubkey?.let { Bookmark } ?: Login

  LaunchedEffect(mainUiState.userPubkey) {
    if (mainUiState.userPubkey != null) {
      val currentDestination = navController.currentBackStackEntry?.destination
      if (currentDestination?.hasRoute<Login>() ?: false) {
        navController.navigate(Bookmark) {
          popUpTo<Login> { inclusive = true }
          launchSingleTop = true
        }
      }
    }
  }

  LaunchedEffect(pendingSharedContent, mainUiState.userPubkey) {
    val content = pendingSharedContent
    if (content != null && mainUiState.userPubkey != null) {
      navController.navigate(
          PostBookmark(sharedUrl = content.url, sharedComment = content.comment)) {
            launchSingleTop = true
          }
      onSharedContentConsumed()
    }
  }

  ModalNavigationDrawer(
      drawerState = drawerState,
      gesturesEnabled = false,
      drawerContent = {
        AppDrawer(
            onNavigateToLicense = { navController.navigate(License) },
            onNavigateToAppInfo = { navController.navigate(AppInfo) },
            onNavigateToSettings = { navController.navigate(Settings) },
            onLogout = { viewModel.onLogoutButtonClicked() },
            onCloseDrawer = { scope.launch { drawerState.close() } })
      }) {
        NavHost(navController = navController, startDestination = startDestination) {
          composable<Login>(
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) {
                LoginScreen(
                    uiState = loginUiState,
                    onLoginButtonClick = {
                      viewModel.onLoginButtonClicked()
                      if (nip55SignerClient.checkNip55SignerInstalled()) {
                        val intent = nip55SignerClient.createPublicKeyIntent()
                        nip55Launcher.launch(intent)
                      }
                    },
                    onDismissDialog = { viewModel.dismissError() },
                    onInstallNip55Signer = {
                      // TODO: Implement Play Store link
                    },
                    onRetry = {
                      viewModel.onRetryLogin()
                      if (nip55SignerClient.checkNip55SignerInstalled()) {
                        val intent = nip55SignerClient.createPublicKeyIntent()
                        nip55Launcher.launch(intent)
                      }
                    },
                    onLoginSuccess = {
                      // Navigation is handled by LaunchedEffect(mainUiState.userPubkey)
                      // to avoid conflicting with share-intent navigation to PostBookmark.
                      viewModel.dismissError()
                    })
              }

          composable<Main>(
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) {
                LaunchedEffect(mainUiState.userPubkey) {
                  mainUiState.userPubkey
                      ?: navController.navigate(Login) { popUpTo<Main> { inclusive = true } }
                }

                MainScreen(
                    uiState = mainUiState,
                    onLogout = { viewModel.onLogoutButtonClicked() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavigateToLogin = {})
              }

          composable<Bookmark>(
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) {
                val bookmarkViewModel: BookmarkViewModel = hiltViewModel()
                val bookmarkUiState by bookmarkViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(mainUiState.userPubkey) {
                  mainUiState.userPubkey
                      ?: navController.navigate(Login) { popUpTo<Bookmark> { inclusive = true } }
                }

                BookmarkScreen(
                    uiState = bookmarkUiState,
                    onRefresh = { bookmarkViewModel.refresh() },
                    onLoad = { bookmarkViewModel.loadBookmarks() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onTabSelected = { tab -> bookmarkViewModel.selectTab(tab) },
                    onAddBookmark = { navController.navigate(PostBookmark()) },
                    onBookmarkDetailNavigate = { bookmark ->
                      navigateToBookmarkDetail(navController, bookmark)
                    })
              }

          composable<PostBookmark>(
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) { backStackEntry ->
                val postBookmarkRoute = backStackEntry.toRoute<PostBookmark>()
                val postBookmarkViewModel: PostBookmarkViewModel = hiltViewModel()
                val postBookmarkUiState by
                    postBookmarkViewModel.uiState.collectAsStateWithLifecycle()

                val signEventLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()) { result ->
                          postBookmarkViewModel.processSignedEvent(result.resultCode, result.data)
                        }

                LaunchedEffect(mainUiState.userPubkey) {
                  mainUiState.userPubkey
                      ?: navController.navigate(Login) {
                        popUpTo<PostBookmark> { inclusive = true }
                      }
                }

                LaunchedEffect(postBookmarkUiState.postSuccess) {
                  if (postBookmarkUiState.postSuccess) {
                    postBookmarkViewModel.resetPostSuccess()
                    navController.navigateUp()
                  }
                }

                LaunchedEffect(postBookmarkRoute) {
                  postBookmarkRoute.sharedUrl?.let { postBookmarkViewModel.updateUrl(it) }
                  postBookmarkRoute.sharedComment?.let { postBookmarkViewModel.updateComment(it) }
                }

                PostBookmarkScreen(
                    uiState = postBookmarkUiState,
                    onUrlChange = { postBookmarkViewModel.updateUrl(it) },
                    onTitleChange = { postBookmarkViewModel.updateTitle(it) },
                    onCategoriesChange = { postBookmarkViewModel.updateCategories(it) },
                    onCommentChange = { postBookmarkViewModel.updateComment(it) },
                    onPostClick = {
                      postBookmarkViewModel.prepareSignEventIntent { intent ->
                        intent?.let { signEventLauncher.launch(it) }
                      }
                    },
                    onNavigateBack = { navController.navigateUp() },
                    onDismissError = { postBookmarkViewModel.dismissError() })
              }

          composable<BookmarkDetail>(
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) { backStackEntry ->
                val route = backStackEntry.toRoute<BookmarkDetail>()
                val detailViewModel: BookmarkDetailViewModel = hiltViewModel()
                val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()

                val signCommentLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()) { result ->
                          detailViewModel.processSignedComment(result.resultCode, result.data)
                        }

                LaunchedEffect(mainUiState.userPubkey) {
                  mainUiState.userPubkey
                      ?: navController.navigate(Login) {
                        popUpTo<BookmarkDetail> { inclusive = true }
                      }
                }

                val loadComments = {
                  detailViewModel.loadComments(
                      rootPubkey = route.authorPubkey,
                      dTag = route.dTag,
                      rootEventId = route.eventId,
                      authorContent = route.content,
                      authorCreatedAt = route.createdAt)
                }

                LaunchedEffect(route) { loadComments() }

                LaunchedEffect(detailUiState.postSuccess) {
                  if (detailUiState.postSuccess) {
                    detailViewModel.resetPostSuccess()
                    loadComments()
                  }
                }

                BookmarkDetailScreen(
                    uiState = detailUiState,
                    title = route.title,
                    urls = route.urls,
                    createdAt = route.createdAt,
                    imageUrl = route.imageUrl,
                    onCommentInputChange = { detailViewModel.updateCommentInput(it) },
                    onPostComment = {
                      detailViewModel.prepareSignCommentIntent(
                          rootPubkey = route.authorPubkey,
                          dTag = route.dTag,
                          rootEventId = route.eventId) { intent ->
                            intent?.let { signCommentLauncher.launch(it) }
                          }
                    },
                    onNavigateBack = { navController.navigateUp() },
                    onDismissError = { detailViewModel.dismissError() },
                    onOpenUrlFailed = { detailViewModel.onOpenUrlFailed() })
              }

          composable<License>(
              enterTransition = { EnterTransition.None },
              exitTransition = { ExitTransition.None },
              popEnterTransition = { EnterTransition.None },
              popExitTransition = { ExitTransition.None }) {
                LicenseScreen(onNavigateUp = { navController.navigateUp() })
              }

          composable<AppInfo>(
              enterTransition = { EnterTransition.None },
              exitTransition = { ExitTransition.None },
              popEnterTransition = { EnterTransition.None },
              popExitTransition = { ExitTransition.None }) {
                AppInfoScreen(
                    uiState =
                        AppInfoUiState(
                            versionName = BuildConfig.VERSION_NAME,
                            commitHash = BuildConfig.COMMIT_HASH,
                        ),
                    onNavigateUp = { navController.navigateUp() },
                )
              }

          composable<Settings>(
              enterTransition = { EnterTransition.None },
              exitTransition = { ExitTransition.None },
              popEnterTransition = { EnterTransition.None },
              popExitTransition = { ExitTransition.None }) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

                SettingsScreen(
                    uiState = settingsUiState,
                    onNavigateUp = { navController.navigateUp() },
                    onDisplayModeChange = { mode -> settingsViewModel.setDisplayMode(mode) })
              }
        }
      }
}

/**
 * Navigate to bookmark detail screen if the bookmark has valid event data
 *
 * @param navController Navigation controller for navigation
 * @param bookmark Bookmark item to navigate to
 */
private fun navigateToBookmarkDetail(
    navController: androidx.navigation.NavController,
    bookmark: io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem,
) {
  val event = bookmark.event ?: return
  val dTag = event.tags.firstOrNull { it.isNotEmpty() && it[0] == "d" }?.getOrNull(1) ?: return
  val eventId = bookmark.eventId ?: return
  navController.navigate(
      BookmarkDetail(
          eventId = eventId,
          authorPubkey = event.author,
          dTag = dTag,
          title = bookmark.title,
          content = event.content,
          createdAt = event.createdAt,
          urls = bookmark.urls,
          imageUrl = bookmark.imageUrl,
      ))
}
