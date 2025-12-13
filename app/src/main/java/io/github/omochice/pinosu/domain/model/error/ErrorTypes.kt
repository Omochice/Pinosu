package io.github.omochice.pinosu.domain.model.error

/**
 * ログイン処理のエラー
 *
 * ログイン処理全体で発生しうるエラーを表現するsealed class。 LoginUseCaseとAuthRepositoryで使用される。
 *
 * Task 2.2: エラー型の定義 Requirements: 1.5, 4.5, 5.1, 5.2, 5.3, 5.4
 */
sealed class LoginError {
  /** Amberアプリがインストールされていない */
  data object AmberNotInstalled : LoginError()

  /** ユーザーがAmberでログインを拒否した */
  data object UserRejected : LoginError()

  /** Amberからの応答がタイムアウトした */
  data object Timeout : LoginError()

  /**
   * ネットワークエラーが発生した
   *
   * @property message エラーメッセージ
   */
  data class NetworkError(val message: String) : LoginError()

  /**
   * 不明なエラーが発生した
   *
   * @property throwable 発生した例外
   */
  data class UnknownError(val throwable: Throwable) : LoginError()
}

/**
 * ログアウト処理のエラー
 *
 * ログアウト処理で発生しうるエラーを表現するsealed class。 LogoutUseCaseで使用される。
 *
 * Task 2.2: エラー型の定義 Requirements: 1.5, 4.5, 5.1, 5.2, 5.3, 5.4
 */
sealed class LogoutError {
  /**
   * ストレージ操作でエラーが発生した
   *
   * @property message エラーメッセージ
   */
  data class StorageError(val message: String) : LogoutError()
}

/**
 * ローカルストレージ操作のエラー
 *
 * EncryptedSharedPreferencesなどのローカルストレージ操作で発生しうるエラーを表現する。 LocalAuthDataSourceとAuthRepositoryで使用される。
 *
 * Task 2.2: エラー型の定義 Requirements: 1.5, 4.5, 5.1, 5.2, 5.3, 5.4
 */
sealed class StorageError : Exception() {
  /**
   * ストレージへの書き込みに失敗した
   *
   * @property message エラーメッセージ
   */
  data class WriteError(override val message: String) : StorageError()

  /**
   * ストレージからの読み込みに失敗した
   *
   * @property message エラーメッセージ
   */
  data class ReadError(override val message: String) : StorageError()
}

/**
 * Amber通信のエラー
 *
 * AmberアプリとのNIP-55 Intent通信で発生しうるエラーを表現する。 AmberSignerClientで使用される。
 *
 * Task 2.2: エラー型の定義 Requirements: 1.5, 4.5, 5.1, 5.2, 5.3, 5.4
 */
sealed class AmberError {
  /** Amberアプリがインストールされていない */
  data object NotInstalled : AmberError()

  /** ユーザーがAmberで操作を拒否した */
  data object UserRejected : AmberError()

  /** Amberからの応答がタイムアウトした */
  data object Timeout : AmberError()

  /**
   * Amberからの応答が不正な形式だった
   *
   * @property message エラーメッセージ
   */
  data class InvalidResponse(val message: String) : AmberError()

  /**
   * Intent解決に失敗した
   *
   * @property message エラーメッセージ
   */
  data class IntentResolutionError(val message: String) : AmberError()
}
