package io.github.omochice.pinosu.ui.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    onNavigateToLicense: () -> Unit,
    onNavigateToAppInfo: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
  DismissibleDrawerSheet(modifier = modifier.fillMaxSize()) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        navigationIcon = {
          IconButton(onClick = onCloseDrawer) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.cd_close_menu))
          }
        })

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
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
