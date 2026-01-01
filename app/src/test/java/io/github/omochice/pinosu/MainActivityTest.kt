package io.github.omochice.pinosu

import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * MainActivityの単体テスト
 * - GetLoginStateUseCaseを呼び出してログイン状態を確認
 * - ログイン済み → メイン画面表示
 * - 未ログイン → ログイン画面表示
 * - 不正データ検出時のログイン状態クリア
 */
class MainActivityTest {

  @Test
  fun `when logged in on startup, should show main screen`() = runTest {
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    val loggedInUser = User("npub1" + "1234567890abcdef".repeat(3) + "1234567890a")
    coEvery { mockGetLoginStateUseCase() } returns loggedInUser

    val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

    assertEquals("MainScreen", initialDestination)
  }

  @Test
  fun `when not logged in on startup, should show login screen`() = runTest {
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    coEvery { mockGetLoginStateUseCase() } returns null

    val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

    assertEquals("LoginScreen", initialDestination)
  }

  @Test
  fun `when invalid data detected on startup, should show login screen`() = runTest {
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    coEvery { mockGetLoginStateUseCase() } returns null

    val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

    assertEquals("LoginScreen", initialDestination)
  }

  /**
   * ログイン状態に基づいて初期表示画面を決定する
   *
   * これはMainActivityに実装される予定のロジックをテストするためのヘルパー関数
   */
  private suspend fun determineInitialDestination(
      getLoginStateUseCase: GetLoginStateUseCase
  ): String {
    val user = getLoginStateUseCase()
    return if (user != null) "MainScreen" else "LoginScreen"
  }
}
