package io.github.omochice.pinosu

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Pinosuアプリケーションクラス
 *
 * Task 7.3: 依存性注入の設定
 * - Hilt DIコンテナの初期化
 *
 * @HiltAndroidAppアノテーションによりHiltのコード生成がトリガーされ、 アプリケーション全体でDIが利用可能になる。
 */
@HiltAndroidApp class PinosuApplication : Application()
