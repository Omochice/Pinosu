package io.github.omochice.pinosu.ui.drawer

import androidx.annotation.StringRes
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.navigation.AppInfo
import io.github.omochice.pinosu.core.navigation.License
import io.github.omochice.pinosu.core.navigation.Route
import io.github.omochice.pinosu.core.navigation.Settings

private const val DRAWER_WIDTH_FRACTION = 0.75f

private data class DrawerDestination(
    val icon: ImageVector,
    @param:StringRes val label: Int,
    val route: Route,
)

private val appDestinations =
    listOf(
        DrawerDestination(Icons.AutoMirrored.Filled.List, R.string.menu_licenses, License),
        DrawerDestination(Icons.Default.Info, R.string.menu_app_info, AppInfo),
        DrawerDestination(Icons.Default.Settings, R.string.menu_settings, Settings),
    )

/**
 * Application drawer menu
 *
 * Displays navigation drawer with menu items for:
 * - License information
 * - App information
 * - Settings
 * - Logout
 *
 * @param onNavigate Callback with the destination route when a navigation menu item is clicked
 * @param onLogout Callback when Logout menu is clicked
 * @param onCloseDrawer Callback to close the drawer after navigation
 * @param modifier Modifier for this composable
 */
@Composable
fun AppDrawer(
    onNavigate: (Route) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
  val navigate: (Route) -> Unit = { route ->
    onCloseDrawer()
    onNavigate(route)
  }

  ModalDrawerSheet(modifier = modifier.fillMaxWidth(DRAWER_WIDTH_FRACTION).fillMaxHeight()) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
      Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

      Spacer(modifier = Modifier.height(8.dp))

      HorizontalDivider()

      DrawerDestinationItems(destinations = appDestinations, onNavigate = navigate)

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

@Composable
private fun DrawerDestinationItems(
    destinations: List<DrawerDestination>,
    onNavigate: (Route) -> Unit,
) {
  destinations.forEach { destination ->
    DrawerMenuItem(
        icon = destination.icon,
        text = stringResource(destination.label),
        onClick = { onNavigate(destination.route) })
  }
}

@Preview(showBackground = true)
@Composable
private fun AppDrawerPreview() {
  AppDrawer(onNavigate = {}, onLogout = {}, onCloseDrawer = {})
}
