package io.github.omochice.pinosu.data.amber

import android.content.Context
import android.content.pm.PackageManager

/**
 * Amber NIP-55 Intent通信のカプセル化
 *
 * AmberアプリとのNIP-55プロトコルに基づく通信を処理する。 Task 4.1: AmberSignerClientの基本実装 Requirements: 1.2, 5.1
 */
class AmberSignerClient(private val context: Context) {

  /**
   * Amberアプリがインストールされているかチェックする
   *
   * PackageManagerを使用してAmberパッケージの存在を確認する。
   *
   * Task 4.1: checkAmberInstalled()実装 Requirement 1.2: Amber未インストール検出
   *
   * @return Amberがインストールされている場合true、そうでない場合false
   */
  fun checkAmberInstalled(): Boolean {
    return try {
      context.packageManager.getPackageInfo(AMBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES)
      true
    } catch (e: PackageManager.NameNotFoundException) {
      // Amberがインストールされていない
      false
    } catch (e: Exception) {
      // その他の例外（セキュリティ例外など）もfalseとして扱う
      false
    }
  }

  /**
   * NIP-55公開鍵取得リクエスト用のIntentを作成する
   *
   * NIP-55プロトコルに準拠したIntentを構築する。 nostrsigner:スキームを使用し、Amberアプリにget_public_keyリクエストを送信する。
   *
   * Task 4.2: createPublicKeyIntent()実装 Requirements: 1.3, 4.1, 4.2
   *
   * @return 構築されたIntent
   */
  fun createPublicKeyIntent(): android.content.Intent {
    val intent =
        android.content.Intent(
            android.content.Intent.ACTION_VIEW, android.net.Uri.parse("$NOSTRSIGNER_SCHEME:"))
    intent.`package` = AMBER_PACKAGE_NAME
    intent.putExtra("type", TYPE_GET_PUBLIC_KEY)
    intent.addFlags(
        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or
            android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
    return intent
  }

  companion object {
    /** Amberアプリのパッケージ名 */
    const val AMBER_PACKAGE_NAME = "com.greenart7c3.nostrsigner"

    /** NIP-55 Intentスキーム */
    const val NOSTRSIGNER_SCHEME = "nostrsigner"

    /** get_public_keyリクエストタイプ */
    const val TYPE_GET_PUBLIC_KEY = "get_public_key"
  }
}

/**
 * Amberからのレスポンス
 *
 * Task 4.1: AmberResponseデータクラスの定義
 *
 * @property pubkey ユーザーの公開鍵（64文字の16進数文字列）
 * @property packageName Amberアプリのパッケージ名
 */
data class AmberResponse(val pubkey: String, val packageName: String)

/**
 * Amber通信のエラー
 *
 * AmberアプリとのNIP-55 Intent通信で発生しうるエラーを表現する。 Task 4.1: AmberErrorの定義 Requirements: 1.5, 4.5, 5.1,
 * 5.3, 5.4
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
