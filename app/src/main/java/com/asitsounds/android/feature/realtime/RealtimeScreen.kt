package com.asitsounds.android.feature.realtime

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealtimeScreen(
    onBack: () -> Unit,
    viewModel: RealtimeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted; if (granted) viewModel.start() }

    DisposableEffect(Unit) { onDispose { viewModel.stop() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("실시간 전사") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.stop(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(if (state.recording) "🎙 녹음 중…" else "🎤 마이크 대기",
                style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text("버퍼: ${state.seconds}초 (${state.windowSamples} 샘플 @ 16kHz)",
                style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(32.dp))

            if (state.recording) {
                Button(onClick = { viewModel.stop() }) {
                    Icon(Icons.Filled.Stop, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("중지")
                }
            } else {
                Button(onClick = {
                    if (hasPermission) viewModel.start()
                    else permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Icon(Icons.Filled.Mic, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("녹음 시작")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("5초 롤링 버퍼로 마이크를 캡처합니다. 실제 추론은 설정의 '실제 음성 인식' 활성화 시 적용됩니다.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
