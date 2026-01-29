package io.github.omochice.pinosu

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.omochice.pinosu.core.navigation.AppInfo
import io.github.omochice.pinosu.core.navigation.Bookmark
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
import io.github.omochice.pinosu.feature.appinfo.presentation.ui.AppInfoScreen
import io.github.omochice.pinosu.feature.auth.presentation.ui.LoginScreen
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginViewModel
import io.github.omochice.pinosu.feature.bookmark.presentation.ui.BookmarkScreen
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkViewModel
import io.github.omochice.pinosu.feature.license.presentation.ui.LicenseScreen
import io.github.omochice.pinosu.feature.main.presentation.ui.MainScreen
import io.github.omochice.pinosu.feature.postbookmark.presentation.ui.PostBookmarkScreen
import io.github.omochice.pinosu.feature.postbookmark.presentation.viewmodel.PostBookmarkViewModel
import io.github.omochice.pinosu.feature.settings.presentation.ui.SettingsScreen
import io.github.omochice.pinosu.feature.settings.presentation.viewmodel.SettingsViewModel
import io.github.omochice.pinosu.ui.drawer.AppDrawer
import io.github.omochice.pinosu.ui.theme.PinosuTheme
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Main Activity for Pinosu application
 *
 * Entry point of the app that sets up Hilt dependency injection and Compose UI. Handles NIP-55
 * signer integration for Nostr authentication.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val loginViewModel: LoginViewModel by viewModels()

  @Inject lateinit var nip55SignerClient: Nip55SignerClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    loginViewModel.checkLoginState()

    setContent {
      PinosuTheme { PinosuApp(viewModel = loginViewModel, nip55SignerClient = nip55SignerClient) }
    }
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
 */
@Composable
fun PinosuApp(viewModel: LoginViewModel, nip55SignerClient: Nip55SignerClient) {
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

  ModalNavigationDrawer(
      drawerState = drawerState,
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
                      navController.navigate(Bookmark) { popUpTo<Login> { inclusive = true } }
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
                    onAddBookmark = { navController.navigate(PostBookmark) },
                    viewModel = bookmarkViewModel)
              }

          composable<PostBookmark>(
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) {
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
                AppInfoScreen(onNavigateUp = { navController.navigateUp() })
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
