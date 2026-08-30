package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SacredGold
import com.example.ui.util.AppLanguage
import com.example.ui.util.Loc
import com.example.ui.viewmodel.ScriptureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHistoryScreen(
    viewModel: ScriptureViewModel,
    onNavigateBack: () -> Unit
) {
    val history by viewModel.readingHistory.collectAsState()
    val readerSettings by viewModel.readerSettings.collectAsState()
    val lang = readerSettings.language

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (lang) {
                            AppLanguage.RU -> "История чтения"
                            AppLanguage.EN -> "Reading History"
                            AppLanguage.TR -> "Okuma Geçmişi"
                        },
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = Loc.get("back", lang),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            // Header Description
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.RU -> "Журнал чтения"
                            AppLanguage.EN -> "Reading Logs"
                            AppLanguage.TR -> "Okuma Kayıtları"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.RU -> "Тихий отчет о ваших размышлениях и прочитанных текстах."
                            AppLanguage.EN -> "A silent record of your contemplative journeys and readings."
                            AppLanguage.TR -> "Düşüncelerinizin ve okumalarınızın sessiz bir kaydı."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dün (Yesterday) items
            val yesterdayItems = history.filter { 
                it.dateText.contains("Dün", ignoreCase = true) || 
                it.dateText.contains("Bugün", ignoreCase = true) ||
                it.dateText.contains("Yesterday", ignoreCase = true) ||
                it.dateText.contains("Today", ignoreCase = true) ||
                it.dateText.contains("Вчера", ignoreCase = true) ||
                it.dateText.contains("Сегодня", ignoreCase = true)
            }
            if (yesterdayItems.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = when (lang) {
                                AppLanguage.RU -> "ВЧЕРА И СЕГОДНЯ"
                                AppLanguage.EN -> "RECENT"
                                AppLanguage.TR -> "DÜN VE BUGÜN"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = SacredGold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            yesterdayItems.forEachIndexed { index, hist ->
                                HistoryItemRow(
                                    hist = hist,
                                    isFirst = index == 0,
                                    lang = lang,
                                    onDelete = { viewModel.deleteHistory(hist.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Bu Hafta (This Week) items
            val otherItems = history.filter { 
                !it.dateText.contains("Dün", ignoreCase = true) && 
                !it.dateText.contains("Bugün", ignoreCase = true) &&
                !it.dateText.contains("Yesterday", ignoreCase = true) &&
                !it.dateText.contains("Today", ignoreCase = true) &&
                !it.dateText.contains("Вчера", ignoreCase = true) &&
                !it.dateText.contains("Сегодня", ignoreCase = true)
            }
            if (otherItems.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = when (lang) {
                                AppLanguage.RU -> "НА ЭТОЙ НЕДЕЛЕ"
                                AppLanguage.EN -> "THIS WEEK"
                                AppLanguage.TR -> "BU HAFTA"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = SacredGold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            otherItems.forEachIndexed { index, hist ->
                                HistoryItemRow(
                                    hist = hist,
                                    isFirst = index == 0,
                                    lang = lang,
                                    onDelete = { viewModel.deleteHistory(hist.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Fallback empty state
            if (history.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (lang) {
                                AppLanguage.RU -> "История чтения пока пуста."
                                AppLanguage.EN -> "No reading history recorded yet."
                                AppLanguage.TR -> "Henüz bir okuma geçmişi bulunmamaktadır."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryItemRow(
    hist: com.example.data.model.ReadingHistory,
    isFirst: Boolean,
    lang: AppLanguage = AppLanguage.TR,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isFirst) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SacredGold)
                        )
                    }
                    Column {
                        Text(
                            text = hist.bookTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (!hist.surahOrChapter.isNullOrEmpty()) hist.surahOrChapter!! else hist.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = hist.dateText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "%${hist.progressPercent}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SacredGold
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_history_item_${hist.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = Loc.get("delete", lang),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Metrics Row
            if (hist.pagesRead > 0 || hist.contemplationMinutes > 0 || hist.isCompleted) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (hist.pagesRead > 0) {
                        val pageLabel = when (lang) {
                            AppLanguage.RU -> "${hist.pagesRead} стр."
                            AppLanguage.EN -> "${hist.pagesRead} Pages"
                            AppLanguage.TR -> "${hist.pagesRead} Sayfa"
                        }
                        SuggestionChip(
                            onClick = {},
                            label = { Text(pageLabel, maxLines = 1, softWrap = false) },
                            icon = { Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    if (hist.contemplationMinutes > 0) {
                        val minLabel = when (lang) {
                            AppLanguage.RU -> "${hist.contemplationMinutes} мин. размышлений"
                            AppLanguage.EN -> "${hist.contemplationMinutes} min Contemplation"
                            AppLanguage.TR -> "${hist.contemplationMinutes} Dk Tefekkür"
                        }
                        SuggestionChip(
                            onClick = {},
                            label = { Text(minLabel, maxLines = 1, softWrap = false) },
                            icon = { Icon(Icons.Filled.HourglassEmpty, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    if (hist.isCompleted) {
                        val doneLabel = when (lang) {
                            AppLanguage.RU -> "Завершено"
                            AppLanguage.EN -> "Completed"
                            AppLanguage.TR -> "Tamamlandı"
                        }
                        SuggestionChip(
                            onClick = {},
                            label = { Text(doneLabel, maxLines = 1, softWrap = false) },
                            icon = { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = SacredGold
                            )
                        )
                    }
                }
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { hist.progressPercent.toFloat() / 100f },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = SacredGold,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )
        }
    }
}
