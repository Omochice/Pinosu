package io.github.omochice.pinosu.presentation.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.omochice.pinosu.presentation.viewmodel.PostBookmarkViewModel

/**
 * Post bookmark screen
 *
 * Screen for creating and posting new bookmarks to Nostr relays via NIP-55 signing.
 *
 * @param viewModel PostBookmarkViewModel
 * @param onNavigateBack Callback when navigation back is requested
 */
@Composable
fun PostBookmarkScreen(
    viewModel: PostBookmarkViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()

  val nip55Launcher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.processSignedEvent(result.resultCode, result.data)
          }

  LaunchedEffect(uiState.readyToSign) {
    if (uiState.readyToSign) {
      val intent = viewModel.createSignEventIntent()
      if (intent != null) {
        nip55Launcher.launch(intent)
      }
    }
  }

  LaunchedEffect(uiState.postSuccess) {
    if (uiState.postSuccess) {
      onNavigateBack()
    }
  }

  Scaffold { padding ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())) {
          Text(text = "新しいブックマークを投稿", style = MaterialTheme.typography.headlineMedium)

          Spacer(modifier = Modifier.height(24.dp))

          OutlinedTextField(
              value = uiState.url,
              onValueChange = viewModel::updateUrl,
              label = { Text("URL *") },
              placeholder = { Text("https://example.com") },
              modifier = Modifier.fillMaxWidth(),
              enabled = !uiState.isSubmitting,
              singleLine = true)

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
              value = uiState.title,
              onValueChange = viewModel::updateTitle,
              label = { Text("タイトル") },
              placeholder = { Text("ブックマークのタイトル") },
              modifier = Modifier.fillMaxWidth(),
              enabled = !uiState.isSubmitting,
              singleLine = true)

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
              value = uiState.categories,
              onValueChange = viewModel::updateCategories,
              label = { Text("カテゴリ") },
              placeholder = { Text("tech, news, etc.") },
              supportingText = { Text("カンマ区切りで複数指定可能") },
              modifier = Modifier.fillMaxWidth(),
              enabled = !uiState.isSubmitting,
              singleLine = true)

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
              value = uiState.comment,
              onValueChange = viewModel::updateComment,
              label = { Text("コメント") },
              placeholder = { Text("このブックマークについてのメモ") },
              modifier = Modifier.fillMaxWidth().height(120.dp),
              enabled = !uiState.isSubmitting,
              maxLines = 5)

          Spacer(modifier = Modifier.height(24.dp))

          Button(
              onClick = { viewModel.prepareAndSignEvent() },
              modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
              enabled = !uiState.isSubmitting && uiState.url.isNotBlank()) {
                if (uiState.isSubmitting) {
                  CircularProgressIndicator(
                      modifier = Modifier.padding(end = 8.dp),
                      color = MaterialTheme.colorScheme.onPrimary)
                }
                Text(if (uiState.isSubmitting) "送信中..." else "投稿")
              }

          Spacer(modifier = Modifier.height(16.dp))

          TextButton(
              onClick = onNavigateBack,
              modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
              enabled = !uiState.isSubmitting) {
                Text("キャンセル")
              }
        }

    val errorMessage = uiState.errorMessage
    if (errorMessage != null) {
      AlertDialog(
          onDismissRequest = viewModel::dismissError,
          title = { Text("エラー") },
          text = { Text(errorMessage) },
          confirmButton = { Button(onClick = viewModel::dismissError) { Text("OK") } })
    }
  }
}
