package com.asitsounds.android.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asitsounds.android.core.ui.SegmentAccessibility
import com.asitsounds.android.core.ui.theme.HangulBlue
import com.asitsounds.android.core.data.db.SegmentEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectDetailScreen(
    projectId: Long,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val segments by viewModel.segments.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val activeIndex by viewModel.activeIndex.collectAsState()
    val title by viewModel.title.collectAsState()

    LaunchedEffect(projectId) { viewModel.load(projectId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title.ifEmpty { "프로젝트" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SegmentFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(segments) { index, seg ->
                    SegmentRow(seg, filter, highlighted = index == activeIndex) {
                        viewModel.updatePosition(seg.startMs)
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentRow(
    seg: SegmentEntity,
    filter: SegmentFilter,
    highlighted: Boolean,
    onTap: () -> Unit
) {
    Card(
        onClick = onTap,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = SegmentAccessibility.describe(seg) },
        colors = if (highlighted)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(formatTimecode(seg.startMs), style = MaterialTheme.typography.labelSmall)
            if (filter.showEn && seg.textEn.isNotBlank())
                Text(seg.textEn, style = MaterialTheme.typography.bodyLarge)
            if (filter.showHangul && seg.textHangul.isNotBlank())
                Text(seg.textHangul, color = HangulBlue, fontWeight = FontWeight.Medium)
            if (filter.showKo && seg.textKo.isNotBlank())
                Text(seg.textKo, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatTimecode(ms: Long): String {
    val s = ms / 1000
    return "%02d:%02d".format(s / 60, s % 60)
}
