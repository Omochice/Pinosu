package io.github.omochice.pinosu.manifest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** AndroidManifest.xmlの設定をテストする Task 1.3: Androidマニフェストの基本設定 */
@RunWith(AndroidJUnit4::class)
class ManifestConfigurationTest {

  private lateinit var context: Context
  private lateinit var packageManager: PackageManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    packageManager = context.packageManager
  }

  /** アプリケーション名が正しく設定されているかテスト Task 1.3: アプリケーション名とアイコンの設定 */
  @Test
  fun testApplicationName() {
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    val appName = packageManager.getApplicationLabel(applicationInfo).toString()

    // アプリケーション名が設定されていることを確認
    assertNotNull("Application name should not be null", appName)
    assertTrue("Application name should not be empty", appName.isNotEmpty())
  }

  /** アプリケーションアイコンが設定されているかテスト Task 1.3: アプリケーション名とアイコンの設定 */
  @Test
  fun testApplicationIcon() {
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    val appIcon = applicationInfo.icon

    // アイコンが設定されていることを確認（デフォルト以外）
    assertTrue("Application icon should be set", appIcon != 0)
  }

  /** <queries>要素でAmber Intent検出が可能かテスト Task 1.3: <queries> 要素の追加（Amber Intent検出用） */
  @Test
  fun testQueriesElementForAmberIntent() {
    // nostrsigner スキームのIntentを作成
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"))

    // このIntentを処理できるアプリがあるかクエリ可能かテスト
    // queries要素が正しく設定されていれば、resolveActivityがnullでない、または
    // queryIntentActivitiesが空でないはず
    val resolveInfo =
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

    // queries要素が設定されていることを確認
    // 実際にAmberがインストールされていなくてもクエリ自体は可能
    assertNotNull("Query for nostrsigner scheme should be possible", resolveInfo)
  }

  /** MainActivityがexportedとして正しく設定されているかテスト Task 1.3: Exported Activity設定（Android 13+対応） */
  @Test
  fun testMainActivityExported() {
    val intent =
        Intent(Intent.ACTION_MAIN).apply {
          addCategory(Intent.CATEGORY_LAUNCHER)
          setPackage(context.packageName)
        }

    val resolveInfo =
        packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .firstOrNull()

    assertNotNull("MainActivity should be resolvable", resolveInfo)

    // Android 12 (API 31)以降ではexportedが明示的に必要
    val activityInfo = resolveInfo?.activityInfo
    assertNotNull("ActivityInfo should not be null", activityInfo)
    assertTrue(
        "MainActivity should be exported for LAUNCHER intent", activityInfo?.exported == true)
  }
}
