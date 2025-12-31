package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * LoginUseCaseの単体テスト
 *
 * Task 6.1: LoginUseCaseの実装
 * - Amberインストール確認のテスト
 * - エラーハンドリングのテスト
 *
 * Requirements: 1.1, 1.3, 1.4, 1.5, 4.5
 */
class LoginUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var loginUseCase: LoginUseCase

  @Before
  fun setup() {
    authRepository = mockk(relaxed = true)
    loginUseCase = AmberLoginUseCase(authRepository)
  }

  // ========== checkAmberInstalled() Tests ==========

  /**
   * Amber未インストール時のテスト
   *
   * Task 6.1: Amberインストール確認 Requirement 1.2: Amber未インストール検出
   */
  @Test
  fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {
    // Given: AuthRepositoryがfalseを返す
    every { authRepository.checkAmberInstalled() } returns false

    // When: checkAmberInstalled()を呼び出す
    val result = loginUseCase.checkAmberInstalled()

    // Then: falseが返される
    assertFalse("Should return false when Amber is not installed", result)
    verify { authRepository.checkAmberInstalled() }
  }

  /**
   * Amberインストール済みの時のテスト
   *
   * Task 6.1: Amberインストール確認 Requirement 1.2: Amber未インストール検出
   */
  @Test
  fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {
    // Given: AuthRepositoryがtrueを返す
    every { authRepository.checkAmberInstalled() } returns true

    // When: checkAmberInstalled()を呼び出す
    val result = loginUseCase.checkAmberInstalled()

    // Then: trueが返される
    assertTrue("Should return true when Amber is installed", result)
    verify { authRepository.checkAmberInstalled() }
  }
}
