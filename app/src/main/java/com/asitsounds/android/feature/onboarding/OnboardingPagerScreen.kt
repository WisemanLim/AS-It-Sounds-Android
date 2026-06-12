package com.asitsounds.android.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asitsounds.android.core.ui.theme.HangulBlue
import kotlinx.coroutines.launch

@Composable
fun OnboardingPagerScreen(onFinish: () -> Unit) {
    val pages = OnboardingPages.pages
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Scaffold { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Top "건너뛰기" (skip)
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onFinish) { Text("건너뛰기", color = MaterialTheme.colorScheme.outline) }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                OnboardingPageContent(pages[page])
            }

            // Dots indicator
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { i ->
                    val active = i == pagerState.currentPage
                    Box(
                        Modifier.padding(horizontal = 4.dp).size(if (active) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (active) HangulBlue else MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }

            Button(
                onClick = {
                    if (isLast) onFinish()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).height(54.dp)
            ) { Text(if (isLast) "시작하기" else "다음", style = MaterialTheme.typography.titleMedium) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(page.emoji, style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Text(page.title, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(page.subtitle, style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)

        // Quote bubble
        if (page.quote != null) {
            Spacer(Modifier.height(20.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (page.quoteEmoji != null) {
                        Text(page.quoteEmoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(12.dp))
                    }
                    Column {
                        Text(page.quote, style = MaterialTheme.typography.bodyLarge)
                        if (page.quoteCaption != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(page.quoteCaption, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }

        // 3-line segment previews
        if (page.previews.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    page.previews.forEachIndexed { i, p ->
                        if (i > 0) HorizontalDivider()
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(p.en, style = MaterialTheme.typography.bodyLarge)
                            Text(p.hangul, color = HangulBlue, fontWeight = FontWeight.Medium)
                            Text(p.ko, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }

        // Language chips
        if (page.chips.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                page.chips.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }
        }

        // Feature rows
        if (page.features.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                page.features.forEach { f ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(f.emoji, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(14.dp))
                        Text(f.text, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        // Model-download note card
        if (page.noteTitle != null) {
            Spacer(Modifier.height(20.dp))
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⬇️", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(page.noteTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        if (page.noteBody != null)
                            Text(page.noteBody, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
