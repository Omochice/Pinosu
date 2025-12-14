package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.User
import javax.inject.Inject

/**
 * GetLoginStateUseCaseの実装クラス
 *
 * Task 6.3: GetLoginStateUseCaseの実装
 * - AuthRepositoryに委譲してログイン状態を取得
 * - 読み取り専用操作
 *
 * Requirements: 2.2, 2.3
 *
 * @property authRepository 認証リポジトリ
 */
class GetLoginStateUseCaseImpl @Inject constructor(private val authRepository: AuthRepository) :
    GetLoginStateUseCase {

  /**
   * ログイン状態を取得する
   *
   * AuthRepositoryに委譲してローカルストレージからユーザー情報を取得する。 この操作は読み取り専用であり、状態を変更しない。
   *
   * Task 6.3: invoke()実装 Requirement 2.2, 2.3: ログイン状態確認とメイン画面表示
   *
   * @return ログイン済みの場合はUser、未ログインの場合はnull
   */
  override suspend fun invoke(): User? {
    return authRepository.getLoginState()
  }
}
