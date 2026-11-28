package io.github.omochice.pinosu.core.result

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import org.junit.Test

class AndThenTest {

  @Test
  fun `success is continued with the result of the next step`() {
    assertEquals(Result.success(4), Result.success(2).andThen { Result.success(it * 2) })
  }

  @Test
  fun `failure of the next step is returned`() {
    val error = IllegalStateException("next")

    val result = Result.success(2).andThen { Result.failure<Int>(error) }

    assertSame(error, result.exceptionOrNull())
  }

  @Test
  fun `failure skips the next step and keeps its exception`() {
    val error = IllegalStateException("first")
    var called = false

    val result =
        Result.failure<Int>(error).andThen {
          called = true
          Result.success(it)
        }

    assertSame(error, result.exceptionOrNull())
    assertEquals(false, called)
  }

  @Test
  fun `exception thrown by the next step is not caught`() {
    assertFailsWith<IllegalStateException> {
      Result.success(1).andThen<Int, Int> { throw IllegalStateException("thrown") }
    }
  }
}
