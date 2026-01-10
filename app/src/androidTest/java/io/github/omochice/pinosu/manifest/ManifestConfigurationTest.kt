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

@RunWith(AndroidJUnit4::class)
class ManifestConfigurationTest {

  private lateinit var context: Context
  private lateinit var packageManager: PackageManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    packageManager = context.packageManager
  }

  @Test
  fun testApplicationName() {
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    val appName = packageManager.getApplicationLabel(applicationInfo).toString()

    assertNotNull("Application name should not be null", appName)
    assertTrue("Application name should not be empty", appName.isNotEmpty())
  }

  @Test
  fun testApplicationIcon() {
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    val appIcon = applicationInfo.icon

    assertTrue("Application icon should be set", appIcon != 0)
  }

  @Test
  fun testQueriesElementForNip55SignerIntent() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"))

    val resolveInfo =
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

    assertNotNull("Query for nostrsigner scheme should be possible", resolveInfo)
  }

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

    val activityInfo = resolveInfo?.activityInfo
    assertNotNull("ActivityInfo should not be null", activityInfo)
    assertTrue(
        "MainActivity should be exported for LAUNCHER intent", activityInfo?.exported == true)
  }
}
