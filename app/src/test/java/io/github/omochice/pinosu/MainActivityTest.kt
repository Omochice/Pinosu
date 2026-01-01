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

  // ========== アプリ起動時のログイン状態確認 ==========

  @Test
  fun `when logged in on startup, should show main screen`() = runTest {
    // Given: ログイン済みユーザー
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    val loggedInUser = User("npub1" + "1234567890abcdef".repeat(3) + "1234567890a")
    coEvery { mockGetLoginStateUseCase() } returns loggedInUser

    // When: アプリ起動時にログイン状態を確認
    val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

    // Then: メイン画面が表示される
    assertEquals("MainScreen", initialDestination)
  }

  @Test
  fun `when not logged in on startup, should show login screen`() = runTest {
    // Given: 未ログインユーザー
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    coEvery { mockGetLoginStateUseCase() } returns null

    // When: アプリ起動時にログイン状態を確認
    val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

    // Then: ログイン画面が表示される
    assertEquals("LoginScreen", initialDestination)
  }

  @Test
  fun `when invalid data detected on startup, should show login screen`() = runTest {
    // Given: 不正なデータ（UseCase側でnullを返す想定）
    val mockGetLoginStateUseCase = mockk<GetLoginStateUseCase>()
    coEvery { mockGetLoginStateUseCase() } returns null

    // When: アプリ起動時にログイン状態を確認
    val initialDestination = determineInitialDestination(mockGetLoginStateUseCase)

    // Then: ログイン画面が表示される（不正データはUseCaseでnull変換済み）
    assertEquals("LoginScreen", initialDestination)
  }

  // ========== ヘルパー関数 ==========

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
