package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * GetLoginStateUseCaseのテストクラス
 *
 * Task 6.3: GetLoginStateUseCaseの実装 Requirements: 2.2, 2.3
 *
 * テストシナリオ:
 * 1. 正常系: ログイン済みユーザーの取得
 * 2. 正常系: 未ログイン状態の取得
 * 3. 検証: 読み取り専用操作であること
 */
class GetLoginStateUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase

  @Before
  fun setup() {
    authRepository = mockk()
    getLoginStateUseCase = AmberGetLoginStateUseCase(authRepository)
  }

  @Test
  fun `invoke returns logged in user when user is logged in`() = runTest {
    // Given: ログイン済み状態
    val testPubkey = "a".repeat(64)
    val testUser = User(testPubkey)
    coEvery { authRepository.getLoginState() } returns testUser

    // When: ログイン状態を取得
    val result = getLoginStateUseCase()

    // Then: ユーザーが返される
    assertEquals(testUser, result)
    coVerify(exactly = 1) { authRepository.getLoginState() }
  }

  @Test
  fun `invoke returns null when user is not logged in`() = runTest {
    // Given: 未ログイン状態
    coEvery { authRepository.getLoginState() } returns null

    // When: ログイン状態を取得
    val result = getLoginStateUseCase()

    // Then: nullが返される
    assertNull(result)
    coVerify(exactly = 1) { authRepository.getLoginState() }
  }

  @Test
  fun `invoke is read-only operation`() = runTest {
    // Given: ログイン済み状態
    val testPubkey = "b".repeat(64)
    val testUser = User(testPubkey)
    coEvery { authRepository.getLoginState() } returns testUser

    // When: 複数回呼び出し
    getLoginStateUseCase()
    getLoginStateUseCase()

    // Then: AuthRepositoryへの呼び出しのみで、変更操作は行われない
    coVerify(exactly = 2) { authRepository.getLoginState() }
    coVerify(exactly = 0) { authRepository.saveLoginState(any()) }
    coVerify(exactly = 0) { authRepository.logout() }
  }
}
