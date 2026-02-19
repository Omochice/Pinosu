package io.github.omochice.pinosu.feature.auth.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginModeTest {

  @Test
  fun `Nip55Signer isReadOnly should be false`() {
    assertFalse(LoginMode.Nip55Signer.isReadOnly)
  }

  @Test
  fun `ReadOnly isReadOnly should be true`() {
    assertTrue(LoginMode.ReadOnly.isReadOnly)
  }

  @Test
  fun `Nip55Signer and ReadOnly should be distinct instances`() {
    val signer: LoginMode = LoginMode.Nip55Signer
    val readOnly: LoginMode = LoginMode.ReadOnly

    assertTrue(signer is LoginMode.Nip55Signer)
    assertTrue(readOnly is LoginMode.ReadOnly)
  }
}
