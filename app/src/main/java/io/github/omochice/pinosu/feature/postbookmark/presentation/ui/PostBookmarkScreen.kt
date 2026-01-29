package io.github.omochice.pinosu.feature.postbookmark.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.feature.postbookmark.presentation.viewmodel.PostBookmarkUiState
import io.github.omochice.pinosu.ui.component.ErrorDialog

/**
 * Post bookmark screen
 *
 * @param uiState Current UI state
 * @param onUrlChange Callback when URL changes
 * @param onTitleChange Callback when title changes
 * @param onCategoriesChange Callback when categories change
 * @param onCommentChange Callback when comment changes
 * @param onPostClick Callback when post button is clicked
 * @param onNavigateBack Callback to navigate back
 * @param onDismissError Callback to dismiss error dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostBookmarkScreen(
    uiState: PostBookmarkUiState,
    onUrlChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCategoriesChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onPostClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onDismissError: () -> Unit
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("ブックマークを追加") },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
              }
            })
      }) { paddingValues ->
        if (uiState.isSubmitting) {
          Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            CircularProgressIndicator(
                modifier =
                    Modifier.align(Alignment.Center).semantics { contentDescription = "読み込み中" })
          }
        } else {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .padding(horizontal = 16.dp)
                      .verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "URL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = "https://",
                          style = MaterialTheme.typography.bodyLarge,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.width(4.dp))
                      OutlinedTextField(
                          value = uiState.url,
                          onValueChange = onUrlChange,
                          modifier = Modifier.weight(1f),
                          singleLine = true,
                          placeholder = { Text("example.com/path") })
                    }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("タイトル") },
                    singleLine = true,
                    placeholder = { Text("ブックマークのタイトル") })

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.categories,
                    onValueChange = onCategoriesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("カテゴリ") },
                    singleLine = true,
                    placeholder = { Text("tech, programming, kotlin") },
                    supportingText = { Text("カンマ区切りで複数入力できます") })

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.comment,
                    onValueChange = onCommentChange,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    label = { Text("コメント") },
                    placeholder = { Text("ブックマークについてのメモ") },
                    maxLines = 5)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onPostClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.url.isNotBlank()) {
                      Text("投稿する")
                    }

                Spacer(modifier = Modifier.height(16.dp))
              }
        }

        if (uiState.errorMessage != null) {
          ErrorDialog(message = uiState.errorMessage, onDismiss = onDismissError)
        }
      }
}

@Preview(showBackground = true)
@Composable
private fun PostBookmarkScreenPreview() {
  PostBookmarkScreen(
      uiState = PostBookmarkUiState(),
      onUrlChange = {},
      onTitleChange = {},
      onCategoriesChange = {},
      onCommentChange = {},
      onPostClick = {},
      onNavigateBack = {},
      onDismissError = {})
}

@Preview(showBackground = true)
@Composable
private fun PostBookmarkScreenFilledPreview() {
  PostBookmarkScreen(
      uiState =
          PostBookmarkUiState(
              url = "example.com/article",
              title = "Sample Article",
              categories = "tech, kotlin",
              comment = "This is a great article about Kotlin"),
      onUrlChange = {},
      onTitleChange = {},
      onCategoriesChange = {},
      onCommentChange = {},
      onPostClick = {},
      onNavigateBack = {},
      onDismissError = {})
}

@Preview(showBackground = true)
@Composable
private fun PostBookmarkScreenLoadingPreview() {
  PostBookmarkScreen(
      uiState = PostBookmarkUiState(isSubmitting = true),
      onUrlChange = {},
      onTitleChange = {},
      onCategoriesChange = {},
      onCommentChange = {},
      onPostClick = {},
      onNavigateBack = {},
      onDismissError = {})
}
