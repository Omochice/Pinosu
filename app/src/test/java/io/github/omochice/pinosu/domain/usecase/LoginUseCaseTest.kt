package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*import org.junit.Before
import org.junit.test

/*** LoginUseCaseUnit tests** Task 6.1: LoginUseCaseImplementation of* - AmberInstallverifytests* - errortests** Requirements: 1.1, 1.3, 1.4, 1.5, 4.5*/class LoginUseCasetest {

 private lateinit var authRepository: AuthRepository
 private lateinit var loginUseCase: LoginUseCase

 @Before
 fun setup() {
 authRepository = mockk(relaxed = true)
 loginUseCase = AmberLoginUseCase(authRepository)
 }

// ========== checkAmberInstalled() tests ==========
/*** AmberInstalltests** Task 6.1: AmberInstallverify Requirement 1.2: AmberInstalldetection*/ @test
 fun testCheckAmberInstalled_WhenNotInstalled_ReturnsFalse() {
// Given: AuthRepositoryfalse every { authRepository.checkAmberInstalled() } returns false

// When: Call checkAmberInstalled() val result = loginUseCase.checkAmberInstalled()

// Then: false is returned assertFalse("Should return false when Amber is not installed", result)
 verify { authRepository.checkAmberInstalled() }
 }

/*** AmberInstalloftests** Task 6.1: AmberInstallverify Requirement 1.2: AmberInstalldetection*/ @test
 fun testCheckAmberInstalled_WhenInstalled_ReturnsTrue() {
// Given: AuthRepositorytrue every { authRepository.checkAmberInstalled() } returns true

// When: Call checkAmberInstalled() val result = loginUseCase.checkAmberInstalled()

// Then: true is returned assertTrue("Should return true when Amber is installed", result)
 verify { authRepository.checkAmberInstalled() }
 }
}
