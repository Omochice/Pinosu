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
 * - Amberインストール確認のテスト
 * - エラーハンドリングのテスト
 */
class LoginUseCaseTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var loginUseCase: LoginUseCase

  @Before
  fun setup() {
    authRepository = mockk(relaxed = true)
    loginUseCase = AmberLoginUseCase(authRepository)
  }

  /** Amber未インストール時のテスト */
  @Test
  fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {

    every { authRepository.checkAmberInstalled() } returns false

    val result = loginUseCase.checkAmberInstalled()

    assertFalse("Should return false when Amber is not installed", result)
    verify { authRepository.checkAmberInstalled() }
  }

  /** Amberインストール済みの時のテスト */
  @Test
  fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {

    every { authRepository.checkAmberInstalled() } returns true

    val result = loginUseCase.checkAmberInstalled()

    assertTrue("Should return true when Amber is installed", result)
    verify { authRepository.checkAmberInstalled() }
  }
}
