package io.github.omochice.pinosu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.omochice.pinosu.data.amber.AmberSignerClient
import io.github.omochice.pinosu.presentation.navigation.BOOKMARK_ROUTE
import io.github.omochice.pinosu.presentation.navigation.LOGIN_ROUTE
import io.github.omochice.pinosu.presentation.navigation.MAIN_ROUTE
import io.github.omochice.pinosu.presentation.ui.BookmarkScreen
import io.github.omochice.pinosu.presentation.ui.LoginScreen
import io.github.omochice.pinosu.presentation.ui.MainScreen
import io.github.omochice.pinosu.presentation.viewmodel.BookmarkViewModel
import io.github.omochice.pinosu.presentation.viewmodel.LoginViewModel
import io.github.omochice.pinosu.ui.theme.PinosuTheme
import javax.inject.Inject

/**
 * Main Activity for Pinosu application
 *
 * Entry point of the app that sets up Hilt dependency injection and Compose UI. Handles Amber
 * signer integration for Nostr authentication.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val loginViewModel: LoginViewModel by viewModels()

  @Inject lateinit var amberSignerClient: AmberSignerClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    loginViewModel.checkLoginState()

    setContent {
      PinosuTheme { PinosuApp(viewModel = loginViewModel, amberSignerClient = amberSignerClient) }
    }
  }
}

/**
 * Root composable for Pinosu application
 *
 * Sets up navigation and handles authentication state transitions between login, main, and bookmark
 * screens.
 *
 * @param viewModel ViewModel managing login/logout state
 * @param amberSignerClient Client for Amber NIP-55 communication
 */
@Composable
fun PinosuApp(viewModel: LoginViewModel, amberSignerClient: AmberSignerClient) {
  val navController = rememberNavController()

  val mainUiState by viewModel.mainUiState.collectAsState()
  val loginUiState by viewModel.uiState.collectAsState()

  val amberLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.processAmberResponse(result.resultCode, result.data)
          }

  val startDestination = if (mainUiState.userPubkey != null) BOOKMARK_ROUTE else LOGIN_ROUTE

  NavHost(navController = navController, startDestination = startDestination) {
    composable(LOGIN_ROUTE) {
      LoginScreen(
          uiState = loginUiState,
          onLoginButtonClick = {
            viewModel.onLoginButtonClicked()
            if (amberSignerClient.checkAmberInstalled()) {
              val intent = amberSignerClient.createPublicKeyIntent()
              amberLauncher.launch(intent)
            }
          },
          onDismissDialog = { viewModel.dismissError() },
          onInstallAmber = {
            // TODO: Implement Play Store link
          },
          onRetry = {
            viewModel.onRetryLogin()
            if (amberSignerClient.checkAmberInstalled()) {
              val intent = amberSignerClient.createPublicKeyIntent()
              amberLauncher.launch(intent)
            }
          },
          onNavigateToMain = {
            navController.navigate(BOOKMARK_ROUTE) { popUpTo(LOGIN_ROUTE) { inclusive = true } }
            viewModel.dismissError()
          })
    }

    composable(MAIN_ROUTE) {
      LaunchedEffect(mainUiState.userPubkey) {
        if (mainUiState.userPubkey == null) {
          navController.navigate(LOGIN_ROUTE) { popUpTo(MAIN_ROUTE) { inclusive = true } }
        }
      }

      MainScreen(
          uiState = mainUiState,
          onLogout = { viewModel.onLogoutButtonClicked() },
          onNavigateToLogin = {})
    }

    composable(BOOKMARK_ROUTE) {
      val bookmarkViewModel: BookmarkViewModel = hiltViewModel()
      val bookmarkUiState by bookmarkViewModel.uiState.collectAsState()

      LaunchedEffect(mainUiState.userPubkey) {
        if (mainUiState.userPubkey == null) {
          navController.navigate(LOGIN_ROUTE) { popUpTo(BOOKMARK_ROUTE) { inclusive = true } }
        }
      }

      BookmarkScreen(
          uiState = bookmarkUiState,
          onRefresh = { bookmarkViewModel.refresh() },
          onLoad = { bookmarkViewModel.loadBookmarks() },
          viewModel = bookmarkViewModel)
    }
  }
}
