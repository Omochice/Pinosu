package io.github.omochice.pinosu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import io.github.omochice.pinosu.auth.AmberSignerClientImpl
import io.github.omochice.pinosu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var appBarConfiguration: AppBarConfiguration
  private lateinit var binding: ActivityMainBinding
  private lateinit var amberLauncher: ActivityResultLauncher<Intent>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)

    val navController = findNavController(R.id.nav_host_fragment_content_main)
    appBarConfiguration = AppBarConfiguration(navController.graph)
    setupActionBarWithNavController(navController, appBarConfiguration)

    amberLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      val res = AmberSignerClientImpl.parseGetPublicKeyResult(result.resultCode, result.data)
      if (res.isSuccess) {
        val pubkey = res.getOrNull()
        Toast.makeText(this, "ログイン成功: ${pubkey?.take(8)}...", Toast.LENGTH_LONG).show()
      } else {
        val err = res.exceptionOrNull()?.message ?: "unknown"
        Toast.makeText(this, "ログイン失敗: $err", Toast.LENGTH_LONG).show()
      }
    }

    binding.fab.setOnClickListener { view ->
      if (AmberSignerClientImpl.isAmberInstalled(this)) {
        val intent = AmberSignerClientImpl.buildGetPublicKeyIntent()
        amberLauncher.launch(intent)
      } else {
        val playStore = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.greenart7c3.nostrsigner"))
        Snackbar.make(view, "Amberがインストールされていません。Play Storeを開きます。", Snackbar.LENGTH_LONG)
            .setAction("インストール") { startActivity(playStore) }
            .setAnchorView(R.id.fab)
            .show()
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    return when (item.itemId) {
      R.id.action_settings -> true
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment_content_main)
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }
}
