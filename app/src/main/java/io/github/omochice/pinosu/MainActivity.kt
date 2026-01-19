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
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.presentation.navigation.AppInfo
import io.github.omochice.pinosu.presentation.navigation.Bookmark
import io.github.omochice.pinosu.presentation.navigation.License
import io.github.omochice.pinosu.presentation.navigation.Login
import io.github.omochice.pinosu.presentation.navigation.Main
import io.github.omochice.pinosu.presentation.navigation.PostBookmark
import io.github.omochice.pinosu.presentation.navigation.Route
import io.github.omochice.pinosu.presentation.navigation.defaultEnterTransition
import io.github.omochice.pinosu.presentation.navigation.defaultExitTransition
import io.github.omochice.pinosu.presentation.navigation.defaultPopEnterTransition
import io.github.omochice.pinosu.presentation.navigation.defaultPopExitTransition
import io.github.omochice.pinosu.presentation.ui.AppInfoScreen
import io.github.omochice.pinosu.presentation.ui.BookmarkScreen
import io.github.omochice.pinosu.presentation.ui.LicenseScreen
import io.github.omochice.pinosu.presentation.ui.LoginScreen
import io.github.omochice.pinosu.presentation.ui.MainScreen
import io.github.omochice.pinosu.presentation.ui.PostBookmarkScreen
import io.github.omochice.pinosu.presentation.ui.drawer.AppDrawer
import io.github.omochice.pinosu.presentation.viewmodel.BookmarkViewModel
import io.github.omochice.pinosu.presentation.viewmodel.LoginViewModel
import io.github.omochice.pinosu.presentation.viewmodel.PostBookmarkViewModel
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

  private var pendingSharedContent: SharedContent? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    loginViewModel.checkLoginState()
    pendingSharedContent = extractSharedContent(intent)

    setContent {
      PinosuTheme {
        PinosuApp(
            viewModel = loginViewModel,
            nip55SignerClient = nip55SignerClient,
            sharedContent = pendingSharedContent,
            onSharedContentConsumed = { pendingSharedContent = null },
        )
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    pendingSharedContent = extractSharedContent(intent)
    recreate()
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
 * @param sharedContent Content shared from other apps, or null if not from share intent
 * @param onSharedContentConsumed Callback when shared content has been consumed
 */
@Composable
fun PinosuApp(
    viewModel: LoginViewModel,
    nip55SignerClient: Nip55SignerClient,
    sharedContent: SharedContent? = null,
    onSharedContentConsumed: () -> Unit = {},
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

  val isLoggedIn = mainUiState.userPubkey != null
  var pendingContentAfterLogin by
      androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(sharedContent) }

  val startDestination: Route =
      when {
        isLoggedIn && sharedContent != null ->
            PostBookmark(sharedUrl = sharedContent.url, sharedComment = sharedContent.comment)
        isLoggedIn -> Bookmark
        else -> Login
      }

  ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        AppDrawer(
            onNavigateToLicense = { navController.navigate(License) },
            onNavigateToAppInfo = { navController.navigate(AppInfo) },
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
                      val destination =
                          if (pendingContentAfterLogin != null) {
                            PostBookmark(
                                sharedUrl = pendingContentAfterLogin?.url,
                                sharedComment = pendingContentAfterLogin?.comment,
                            )
                          } else {
                            Bookmark
                          }
                      navController.navigate(destination) { popUpTo<Login> { inclusive = true } }
                      pendingContentAfterLogin = null
                      onSharedContentConsumed()
                      viewModel.dismissError()
                    },
                )
              }

          composable<Main>(
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) {
                LaunchedEffect(mainUiState.userPubkey) {
                  if (mainUiState.userPubkey == null) {
                    navController.navigate(Login) { popUpTo<Main> { inclusive = true } }
                  }
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
                  if (mainUiState.userPubkey == null) {
                    navController.navigate(Login) { popUpTo<Bookmark> { inclusive = true } }
                  }
                }

                BookmarkScreen(
                    uiState = bookmarkUiState,
                    onRefresh = { bookmarkViewModel.refresh() },
                    onLoad = { bookmarkViewModel.loadBookmarks() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onTabSelected = { tab -> bookmarkViewModel.selectTab(tab) },
                    onAddBookmark = { navController.navigate(PostBookmark()) },
                    viewModel = bookmarkViewModel,
                )
              }

          composable<PostBookmark>(
              enterTransition = { defaultEnterTransition },
              exitTransition = { defaultExitTransition },
              popEnterTransition = { defaultPopEnterTransition },
              popExitTransition = { defaultPopExitTransition }) { backStackEntry ->
                val route: PostBookmark = backStackEntry.toRoute()
                val postBookmarkViewModel: PostBookmarkViewModel = hiltViewModel()
                val postBookmarkUiState by
                    postBookmarkViewModel.uiState.collectAsStateWithLifecycle()

                val signEventLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()) { result ->
                          postBookmarkViewModel.processSignedEvent(result.resultCode, result.data)
                        }

                LaunchedEffect(route.sharedUrl, route.sharedComment) {
                  route.sharedUrl?.let { postBookmarkViewModel.updateUrl(it) }
                  route.sharedComment?.let { postBookmarkViewModel.updateComment(it) }
                }

                LaunchedEffect(mainUiState.userPubkey) {
                  if (mainUiState.userPubkey == null) {
                    navController.navigate(Login) { popUpTo<PostBookmark> { inclusive = true } }
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
        }
      }
}

/**
 * Represents content shared from other apps
 *
 * @property url The shared URL (http/https), or null if shared text is not a URL
 * @property comment The shared text when it's not a URL, or null if it's a URL
 */
data class SharedContent(val url: String? = null, val comment: String? = null)

/**
 * Extract shared content from ACTION_SEND intent
 *
 * If the shared text is a URL (http/https), it's set as url. Otherwise, it's set as comment.
 *
 * @param intent The intent to process
 * @return SharedContent if valid share intent, null otherwise
 */
internal fun extractSharedContent(intent: android.content.Intent): SharedContent? {
  if (intent.action != android.content.Intent.ACTION_SEND) return null
  if (intent.type != "text/plain") return null

  val sharedText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT) ?: return null

  val isUrl =
      sharedText.startsWith("http://", ignoreCase = true) ||
          sharedText.startsWith("https://", ignoreCase = true)

  return if (isUrl) {
    SharedContent(url = sharedText, comment = null)
  } else {
    SharedContent(url = null, comment = sharedText)
  }
}
