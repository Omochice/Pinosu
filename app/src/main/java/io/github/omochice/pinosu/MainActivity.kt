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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.presentation.navigation.APP_INFO_ROUTE
import io.github.omochice.pinosu.presentation.navigation.BOOKMARK_ROUTE
import io.github.omochice.pinosu.presentation.navigation.LICENSE_ROUTE
import io.github.omochice.pinosu.presentation.navigation.LOGIN_ROUTE
import io.github.omochice.pinosu.presentation.navigation.MAIN_ROUTE
import io.github.omochice.pinosu.presentation.navigation.defaultEnterTransition
import io.github.omochice.pinosu.presentation.navigation.defaultExitTransition
import io.github.omochice.pinosu.presentation.navigation.defaultPopEnterTransition
import io.github.omochice.pinosu.presentation.navigation.defaultPopExitTransition
import io.github.omochice.pinosu.presentation.ui.AppInfoScreen
import io.github.omochice.pinosu.presentation.ui.BookmarkScreen
import io.github.omochice.pinosu.presentation.ui.LicenseScreen
import io.github.omochice.pinosu.presentation.ui.LoginScreen
import io.github.omochice.pinosu.presentation.ui.MainScreen
import io.github.omochice.pinosu.presentation.ui.drawer.AppDrawer
import io.github.omochice.pinosu.presentation.viewmodel.BookmarkViewModel
import io.github.omochice.pinosu.presentation.viewmodel.LoginViewModel
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

  val mainUiState by viewModel.mainUiState.collectAsState()
  val loginUiState by viewModel.uiState.collectAsState()

  val nip55Launcher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.processNip55Response(result.resultCode, result.data)
          }

  val startDestination = if (mainUiState.userPubkey != null) BOOKMARK_ROUTE else LOGIN_ROUTE

  ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        AppDrawer(
            onNavigateToLicense = { navController.navigate(LICENSE_ROUTE) },
            onNavigateToAppInfo = { navController.navigate(APP_INFO_ROUTE) },
            onLogout = { viewModel.onLogoutButtonClicked() },
            onCloseDrawer = { scope.launch { drawerState.close() } })
      }) {
        NavHost(navController = navController, startDestination = startDestination) {
          composable(
              LOGIN_ROUTE,
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
                    onNavigateToMain = {
                      navController.navigate(BOOKMARK_ROUTE) {
                        popUpTo(LOGIN_ROUTE) { inclusive = true }
                      }
                      viewModel.dismissError()
                    })
              }

          composable(
              MAIN_ROUTE,
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) {
                LaunchedEffect(mainUiState.userPubkey) {
                  if (mainUiState.userPubkey == null) {
                    navController.navigate(LOGIN_ROUTE) { popUpTo(MAIN_ROUTE) { inclusive = true } }
                  }
                }

                MainScreen(
                    uiState = mainUiState,
                    onLogout = { viewModel.onLogoutButtonClicked() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavigateToLogin = {})
              }

          composable(
              BOOKMARK_ROUTE,
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) {
                val bookmarkViewModel: BookmarkViewModel = hiltViewModel()
                val bookmarkUiState by bookmarkViewModel.uiState.collectAsState()

                LaunchedEffect(mainUiState.userPubkey) {
                  if (mainUiState.userPubkey == null) {
                    navController.navigate(LOGIN_ROUTE) {
                      popUpTo(BOOKMARK_ROUTE) { inclusive = true }
                    }
                  }
                }

                BookmarkScreen(
                    uiState = bookmarkUiState,
                    onRefresh = { bookmarkViewModel.refresh() },
                    onLoad = { bookmarkViewModel.loadBookmarks() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onTabSelected = { tab -> bookmarkViewModel.selectTab(tab) },
                    viewModel = bookmarkViewModel)
              }

          composable(
              LICENSE_ROUTE,
              enterTransition = { EnterTransition.None },
              exitTransition = { ExitTransition.None },
              popEnterTransition = { EnterTransition.None },
              popExitTransition = { ExitTransition.None }) {
                LicenseScreen(onNavigateUp = { navController.navigateUp() })
              }

          composable(
              APP_INFO_ROUTE,
              enterTransition = { EnterTransition.None },
              exitTransition = { ExitTransition.None },
              popEnterTransition = { EnterTransition.None },
              popExitTransition = { ExitTransition.None }) {
                AppInfoScreen(onNavigateUp = { navController.navigateUp() })
              }
        }
      }
}
