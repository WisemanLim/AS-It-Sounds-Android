package com.asitsounds.android.feature.import_

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.net.Uri

// BottomSheet offering SAF documents, photo-picker, and (debug) a Mock-ASR demo (F-2).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSheet(
    onDismiss: () -> Unit,
    onUriPicked: (Uri, ImportSource) -> Unit,
    onDemo: (() -> Unit)? = null
) {
    val docLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onUriPicked(it, ImportSource.DOCUMENT) } }

    val mediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { onUriPicked(it, ImportSource.MEDIA) } }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("가져오기", style = MaterialTheme.typography.headlineSmall)
            Text("소스", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                onClick = { docLauncher.launch(arrayOf("audio/*", "video/*")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Files 에서 가져오기 (오디오/비디오)") }
            OutlinedButton(
                onClick = {
                    mediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("사진 보관함에서 가져오기 (동영상)") }

            if (onDemo != null) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onDemo, modifier = Modifier.fillMaxWidth()) {
                    Text("✨ 데모: 전사 시작 (Mock ASR)")
                }
                Text(
                    "Files/사진: 실제 오디오/비디오를 사본으로 가져와 전사. 데모: 합성 세그먼트 즉시 시연.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
