package io.github.omochice.pinosu.ui.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R

private const val DRAWER_WIDTH_FRACTION = 0.75f

/**
 * Application drawer menu
 *
 * Displays navigation drawer with menu items for:
 * - License information
 * - App information
 * - Settings
 * - Logout
 *
 * @param onNavigateToLicense Callback when License menu is clicked
 * @param onNavigateToAppInfo Callback when App Info menu is clicked
 * @param onNavigateToSettings Callback when Settings menu is clicked
 * @param onLogout Callback when Logout menu is clicked
 * @param onCloseDrawer Callback to close the drawer after navigation
 * @param modifier Modifier for this composable
 */
@Composable
fun AppDrawer(
    onNavigateToLicense: () -> Unit,
    onNavigateToAppInfo: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
  DismissibleDrawerSheet(modifier = modifier.fillMaxWidth(DRAWER_WIDTH_FRACTION).fillMaxHeight()) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
      Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

      Spacer(modifier = Modifier.height(8.dp))

      HorizontalDivider()

      DrawerMenuItem(
          icon = Icons.AutoMirrored.Filled.List,
          text = stringResource(R.string.menu_licenses),
          onClick = {
            onCloseDrawer()
            onNavigateToLicense()
          })

      DrawerMenuItem(
          icon = Icons.Default.Info,
          text = stringResource(R.string.menu_app_info),
          onClick = {
            onCloseDrawer()
            onNavigateToAppInfo()
          })

      DrawerMenuItem(
          icon = Icons.Default.Settings,
          text = stringResource(R.string.menu_settings),
          onClick = {
            onCloseDrawer()
            onNavigateToSettings()
          })

      HorizontalDivider()

      DrawerMenuItem(
          icon = Icons.AutoMirrored.Filled.ExitToApp,
          text = stringResource(R.string.menu_logout),
          onClick = {
            onCloseDrawer()
            onLogout()
          })
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun AppDrawerPreview() {
  AppDrawer(
      onNavigateToLicense = {},
      onNavigateToAppInfo = {},
      onNavigateToSettings = {},
      onLogout = {},
      onCloseDrawer = {})
}
