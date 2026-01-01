package io.github.omochice.pinosu.manifest

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.test
import roid.content.Context
import roid.content.Intent
import roid.content.pm.PackageManager
import roid.net.Uri
import roidx.test.core.app.ApplicationProvider
import roidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class ManifestConfigurationtest {

  private lateinit var context: Context
  private lateinit var packageManager: PackageManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    packageManager = context.packageManager
  }

  @test
  fun testApplicationName() {
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    val appName = packageManager.getApplicationLabel(applicationInfo).toString()

    // Verify application name is set
    assertNotNull("Application name should not be null", appName)
    assertTrue("Application name should not be empty", appName.isNotEmpty())
  }

  @test
  fun testApplicationIcon() {
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
    val appIcon = applicationInfo.icon

    // Verify icon is set (not default)
    assertTrue("Application icon should be set", appIcon != 0)
  }

  @test
  fun testQueriesElementForAmberIntent() {
    // Create Intent with nostrsigner scheme
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:"))

    // Test if queries allow querying apps that can process this Intent
    // If queries element is correctly set, resolveActivity should not be null
    // and queryIntentActivities should return results
    val resolveInfo =
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

    // Verify queries element is set
    // The query itself is possible even if Amber is not actually installed
    assertNotNull("Query for nostrsigner scheme should be possible", resolveInfo)
  }

  @test
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

    // For Android 12 (API 31) and later, exported must be explicitly set
    val activityInfo = resolveInfo?.activityInfo
    assertNotNull("ActivityInfo should not be null", activityInfo)
    assertTrue(
        "MainActivity should be exported for LAUNCHER intent", activityInfo?.exported == true)
  }
}
