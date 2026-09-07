package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.ui.theme.SacredGold
import com.example.ui.viewmodel.AppThemeSetting
import com.example.ui.viewmodel.FontFamilySetting
import com.example.ui.viewmodel.LineHeightSetting
import com.example.ui.viewmodel.ScriptureViewModel
import com.example.ui.util.AppLanguage
import com.example.ui.util.Loc
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ScriptureViewModel,
    book: Book,
    onNavigateBack: () -> Unit
) {
    if (book.id == "quran") {
        QuranReaderView(viewModel = viewModel, onNavigateBack = onNavigateBack)
        return
    }

    if (book.id == "torah" || book.id == "sermon" || book.id == "talmud" || book.id == "bukhari" || book.id == "gita") {
        BibleReaderView(viewModel = viewModel, book = book, onNavigateBack = onNavigateBack)
        return
    }

    val activeBookContentState by viewModel.activeBookContent.collectAsState()
    val book = activeBookContentState ?: book
    val isBookLoading by viewModel.isBookLoading.collectAsState()
    val bookError by viewModel.bookError.collectAsState()

    LaunchedEffect(book.id) {
        viewModel.loadBookContent(book)
    }

    val readerSettings by viewModel.readerSettings.collectAsState()
    val lang = readerSettings.language
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showSettingsPanel by remember { mutableStateOf(false) }
    var showLanguageCard by remember { mutableStateOf(false) }
    var languageMode by remember { mutableStateOf("turkish") } // "turkish", "original", "bilingual"
    var isBookmarked by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteQuoteText by remember { mutableStateOf("") }
    var noteTextQuery by remember { mutableStateOf("") }
    var aiQueryText by remember { mutableStateOf("") }

    // Scroll progress line calculations
    val scrollProgress = remember {
        derivedStateOf {
            if (listState.layoutInfo.totalItemsCount == 0) 0f
            else {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = listState.layoutInfo.totalItemsCount
                (lastVisible.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            }
        }
    }

    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = {
                Text(
                    text = when (lang) {
                        AppLanguage.RU -> "Добавить заметку или выделение"
                        AppLanguage.EN -> "Add Note or Highlight"
                        AppLanguage.TR -> "Not veya İşaretleme Ekle"
                    },
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.RU -> "Выбранный стих / отрывок:"
                            AppLanguage.EN -> "Selected Verse/Passage:"
                            AppLanguage.TR -> "Seçilen Ayet/Pasaj:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = SacredGold
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = noteQuoteText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = noteTextQuery,
                        onValueChange = { noteTextQuery = it },
                        label = {
                            Text(
                                when (lang) {
                                    AppLanguage.RU -> "Ваша заметка (необязательно)"
                                    AppLanguage.EN -> "Your Reflection Note (Optional)"
                                    AppLanguage.TR -> "Tefekkür Notunuz (İsteğe Bağlı)"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (lang) {
                                    AppLanguage.RU -> "Напишите ваши мысли об этом отрывке..."
                                    AppLanguage.EN -> "Write your thoughts about this passage..."
                                    AppLanguage.TR -> "Bu pasaj hakkındaki düşüncelerinizi yazın..."
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("note_input_field"),
                        singleLine = false,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            viewModel.addNoteOrHighlight(
                                bookTitle = book.title,
                                quoteText = noteQuoteText,
                                noteText = null,
                                isHighlightOnly = true
                            )
                            showAddNoteDialog = false
                            noteTextQuery = ""
                        }
                    ) {
                        Text(
                            when (lang) {
                                AppLanguage.RU -> "Только выделить"
                                AppLanguage.EN -> "Highlight Only"
                                AppLanguage.TR -> "Yalnızca İşaretle"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.addNoteOrHighlight(
                                bookTitle = book.title,
                                quoteText = noteQuoteText,
                                noteText = noteTextQuery,
                                isHighlightOnly = false
                            )
                            showAddNoteDialog = false
                            noteTextQuery = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            when (lang) {
                                AppLanguage.RU -> "Сохранить заметку"
                                AppLanguage.EN -> "Save Note"
                                AppLanguage.TR -> "Notu Kaydet"
                            }
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text(
                        when (lang) {
                            AppLanguage.RU -> "Отмена"
                            AppLanguage.EN -> "Cancel"
                            AppLanguage.TR -> "İptal"
                        }
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Set Active Book inside ViewModel for progress tracking
    LaunchedEffect(book) {
        viewModel.setActiveBook(book)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(
                            text = book.title,
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
                    actions = {
                        IconButton(onClick = { isBookmarked = !isBookmarked }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = when (lang) {
                                    AppLanguage.RU -> "Закладка"
                                    AppLanguage.EN -> "Bookmark"
                                    AppLanguage.TR -> "Yer İmi"
                                },
                                tint = if (isBookmarked) SacredGold else MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showSettingsPanel = !showSettingsPanel }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = Loc.get("appearance_settings", lang),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                // Reading progress indicator line
                LinearProgressIndicator(
                    progress = { scrollProgress.value },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            // Contextual FAB for notes taking
            ExtendedFloatingActionButton(
                onClick = {
                    noteQuoteText = book.paragraphs.firstOrNull() ?: ""
                    showAddNoteDialog = true
                },
                icon = {
                    Icon(
                        Icons.Filled.EditNote,
                        when (lang) {
                            AppLanguage.RU -> "Добавить заметку"
                            AppLanguage.EN -> "Add Note"
                            AppLanguage.TR -> "Not Ekle"
                        }
                    )
                },
                text = {
                    Text(
                        when (lang) {
                            AppLanguage.RU -> "Быстрая заметка"
                            AppLanguage.EN -> "Quick Note"
                            AppLanguage.TR -> "Hızlı Not Al"
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("reader_quick_note_fab")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                contentPadding = PaddingValues(top = 32.dp, bottom = 120.dp)
            ) {
                // Metadata Header
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = book.subContentTitle.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = SacredGold,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = book.contentTitle,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        if (book.introText.isNotEmpty()) {
                            Text(
                                text = book.introText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontSize = (readerSettings.fontSizeSp - 2).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Polished Control Panel Card (Downloads, Language Switcher, Audio Player)
                item {
                    val downloadedBooks by viewModel.downloadedBooks.collectAsState()
                    val downloadProgress by viewModel.downloadProgress.collectAsState()
                    val isDownloaded = downloadedBooks.contains(book.id)
                    val progress = downloadProgress[book.id]

                    val currentPlayingUrl by viewModel.currentPlayingUrl.collectAsState()
                    val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()
                    val isAudioLoading by viewModel.isAudioLoading.collectAsState()

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            // Header Toggle Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showLanguageCard = !showLanguageCard },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Language,
                                        contentDescription = null,
                                        tint = SacredGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.RU -> "ЯЗЫК И ПАРАМЕТРЫ ЧТЕНИЯ"
                                            AppLanguage.EN -> "READING LANGUAGE & OPTIONS"
                                            AppLanguage.TR -> "OKUMA DİLİ VE SEÇENEKLER"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { showLanguageCard = !showLanguageCard },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showLanguageCard) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Gizle/Göster",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (showLanguageCard) {
                                Spacer(modifier = Modifier.height(12.dp))
                                // 1. LANGUAGE MODE ROW (Only if original paragraphs exist)
                                if (book.originalParagraphs.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val primaryLangLabel = when (lang) {
                                                AppLanguage.RU -> "Русский"
                                                AppLanguage.EN -> "English"
                                                AppLanguage.TR -> "Türkçe"
                                            }
                                            val bilingualLabel = when (lang) {
                                                AppLanguage.RU -> "Двуязычный"
                                                AppLanguage.EN -> "Bilingual"
                                                AppLanguage.TR -> "İki Dilli"
                                            }
                                            val modes = listOf(
                                                "turkish" to primaryLangLabel,
                                                "original" to book.originalLanguageName,
                                                "bilingual" to bilingualLabel
                                            )
                                            modes.forEach { (mode, label) ->
                                                val isSelected = languageMode == mode
                                                Card(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { languageMode = mode },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(36.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                            fontSize = 12.sp,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(horizontal = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }

                            // 2. AUDIO PLAYER ROW
                            if (book.audioUrl.isNotEmpty()) {
                                val isCurrentBookPlaying = currentPlayingUrl == book.audioUrl
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isCurrentBookPlaying && isAudioPlaying) {
                                                        SacredGold.copy(alpha = 0.2f)
                                                    } else {
                                                        MaterialTheme.colorScheme.secondaryContainer
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrentBookPlaying && isAudioPlaying) Icons.Filled.Headphones else Icons.Filled.PlayLesson,
                                                contentDescription = null,
                                                tint = if (isCurrentBookPlaying && isAudioPlaying) SacredGold else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = when (lang) {
                                                    AppLanguage.RU -> "Аудиочтение"
                                                    AppLanguage.EN -> "Audiobook Narrator"
                                                    AppLanguage.TR -> "Sesli Kitap Okuyucu"
                                                },
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isCurrentBookPlaying) {
                                                    if (isAudioPlaying) {
                                                        when (lang) {
                                                            AppLanguage.RU -> "Звучит выразительное аудиочтение..."
                                                            AppLanguage.EN -> "Beautifully narrated audiobook is playing..."
                                                            AppLanguage.TR -> "Muhteşem tonlu seslendirme çalınıyor..."
                                                        }
                                                    } else {
                                                        when (lang) {
                                                            AppLanguage.RU -> "Чтение приостановлено."
                                                            AppLanguage.EN -> "Narration paused."
                                                            AppLanguage.TR -> "Seslendirme duraklatıldı."
                                                        }
                                                    }
                                                } else {
                                                    when (lang) {
                                                        AppLanguage.RU -> "Слушайте глубокий, умиротворяющий голос."
                                                        AppLanguage.EN -> "Listen with a deep, resonant, and peaceful voice."
                                                        AppLanguage.TR -> "Derin, tok ve huzur veren erkek sesiyle dinleyin."
                                                    }
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (isCurrentBookPlaying && isAudioLoading) {
                                            CircularProgressIndicator(
                                                color = SacredGold,
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            IconButton(
                                                onClick = {
                                                    if (isCurrentBookPlaying) {
                                                        viewModel.togglePlayPause()
                                                    } else {
                                                        viewModel.playAudio(book.audioUrl, -1)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                            ) {
                                                Icon(
                                                    imageVector = if (isCurrentBookPlaying && isAudioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                    contentDescription = when (lang) {
                                                        AppLanguage.RU -> "Слушать"
                                                        AppLanguage.EN -> "Listen"
                                                        AppLanguage.TR -> "Dinle"
                                                    },
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            if (isCurrentBookPlaying) {
                                                IconButton(
                                                    onClick = { viewModel.stopAudio() },
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Stop,
                                                        contentDescription = when (lang) {
                                                            AppLanguage.RU -> "Остановить"
                                                            AppLanguage.EN -> "Stop"
                                                            AppLanguage.TR -> "Durdur"
                                                        },
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            }

                            // 3. OFFLINE DOWNLOAD MODULE ROW
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isDownloaded) Icons.Filled.CloudDone else Icons.Filled.CloudDownload,
                                        contentDescription = null,
                                        tint = if (isDownloaded) SacredGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (isDownloaded) {
                                                when (lang) {
                                                    AppLanguage.RU -> "Сохраненная глава"
                                                    AppLanguage.EN -> "Saved Chapter"
                                                    AppLanguage.TR -> "Kayıtlı Bölüm"
                                                }
                                            } else {
                                                when (lang) {
                                                    AppLanguage.RU -> "Скачать главу"
                                                    AppLanguage.EN -> "Download Chapter"
                                                    AppLanguage.TR -> "Bölümü İndir"
                                                }
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (progress != null) {
                                            Text(
                                                text = when (lang) {
                                                    AppLanguage.RU -> "Загрузка: %${(progress * 100).toInt()}"
                                                    AppLanguage.EN -> "Downloading: %${(progress * 100).toInt()}"
                                                    AppLanguage.TR -> "İndiriliyor: %${(progress * 100).toInt()}"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Text(
                                                text = if (isDownloaded) {
                                                    when (lang) {
                                                        AppLanguage.RU -> "Тексты и аудио сохранены на устройстве."
                                                        AppLanguage.EN -> "Saved to your device for reading anytime."
                                                        AppLanguage.TR -> "Metinler ve seslendirmeler cihazınızda kayıtlı."
                                                    }
                                                } else {
                                                    when (lang) {
                                                        AppLanguage.RU -> "Скачайте для чтения и прослушивания в любое время."
                                                        AppLanguage.EN -> "Download to read and listen anytime."
                                                        AppLanguage.TR -> "Metinleri ve seslendirmeyi dilediğiniz zaman okuyup dinleyin."
                                                    }
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (progress != null) {
                                    CircularProgressIndicator(
                                        progress = { progress },
                                        color = SacredGold,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else if (!isDownloaded) {
                                    Button(
                                        onClick = { viewModel.downloadBook(book.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            when (lang) {
                                                AppLanguage.RU -> "Скачать"
                                                AppLanguage.EN -> "Download"
                                                AppLanguage.TR -> "İndir"
                                            },
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                            if (progress != null) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = SacredGold,
                                    trackColor = Color.Transparent
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                            val isAiLoaded = book.contentTitle != (com.example.data.model.BookRepository.books.firstOrNull { it.id == book.id }?.contentTitle ?: "")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isAiLoaded) Icons.Filled.AutoAwesome else Icons.Filled.VerifiedUser,
                                        contentDescription = null,
                                        tint = SacredGold,
                                        modifier = Modifier.size(20.dp)
                                      )
                                      Column {
                                          Text(
                                              text = if (isAiLoaded) {
                                                  when (lang) {
                                                      AppLanguage.RU -> "Чтение Писания активно"
                                                      AppLanguage.EN -> "Scripture Reading Active"
                                                      AppLanguage.TR -> "Metin Okuma Hazır"
                                                  }
                                              } else {
                                                  when (lang) {
                                                      AppLanguage.RU -> "Стандартное издание"
                                                      AppLanguage.EN -> "Standard Edition"
                                                      AppLanguage.TR -> "Standart Nüsha"
                                                  }
                                              },
                                              style = MaterialTheme.typography.titleSmall,
                                              fontWeight = FontWeight.Bold,
                                              color = MaterialTheme.colorScheme.onSurface
                                          )
                                          Text(
                                              text = if (isAiLoaded) {
                                                  "${book.title} • ${book.contentTitle} (${book.originalLanguageName})"
                                              } else {
                                                  when (book.id) {
                                                      "quran" -> when (lang) {
                                                          AppLanguage.RU -> "Священный Коран (Сура Аль-Фатх • Русский и арабский)"
                                                          AppLanguage.EN -> "Holy Quran (Surah Al-Fath • English & Arabic)"
                                                          AppLanguage.TR -> "Kur'an-ı Kerim (Fetih Suresi • Türkçe & Arapça)"
                                                      }
                                                      "torah" -> when (lang) {
                                                          AppLanguage.RU -> "Тора (Бытие 1-3 • Русский и иврит)"
                                                          AppLanguage.EN -> "Torah (Genesis 1-3 • English & Hebrew)"
                                                          AppLanguage.TR -> "Tevrat (Yaratılış 1-3 • Türkçe & İbranice)"
                                                      }
                                                      "sermon" -> when (lang) {
                                                          AppLanguage.RU -> "Евангелие (Русский и греческий)"
                                                          AppLanguage.EN -> "Gospel (English & Greek)"
                                                          AppLanguage.TR -> "İncil (Türkçe & Grekçe)"
                                                      }
                                                      else -> when (lang) {
                                                          AppLanguage.RU -> "Оригинальный текст и перевод"
                                                          AppLanguage.EN -> "Original Text & Translation"
                                                          AppLanguage.TR -> "Orijinal Metin ve Meal"
                                                      }
                                                  }
                                              },
                                              style = MaterialTheme.typography.bodySmall,
                                              color = MaterialTheme.colorScheme.onSurfaceVariant
                                          )
                                      }
                                  }
                                  Box(
                                      modifier = Modifier
                                          .clip(RoundedCornerShape(4.dp))
                                          .background(
                                              if (isAiLoaded) {
                                                  SacredGold.copy(alpha = 0.2f)
                                              } else {
                                                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                              }
                                          )
                                          .padding(horizontal = 8.dp, vertical = 4.dp)
                                  ) {
                                      Text(
                                          text = if (isAiLoaded) {
                                              when (lang) {
                                                  AppLanguage.RU -> "ОНЛАЙН/АРХИВ"
                                                  AppLanguage.EN -> "LIVE/ARCHIVE"
                                                  AppLanguage.TR -> "CANLI/ARŞİV"
                                              }
                                          } else {
                                              when (lang) {
                                                  AppLanguage.RU -> "ВСТРОЕННЫЙ"
                                                  AppLanguage.EN -> "EMBEDDED"
                                                  AppLanguage.TR -> "GÖMÜLÜ"
                                              }
                                          },
                                          style = MaterialTheme.typography.labelSmall,
                                          color = if (isAiLoaded) SacredGold else MaterialTheme.colorScheme.primary,
                                          fontWeight = FontWeight.Bold
                                      )
                                  }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = SacredGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.RU -> "Загрузка из архива/онлайн (бесплатно)"
                                            AppLanguage.EN -> "Live Library & Archive Fetch (Free)"
                                            AppLanguage.TR -> "Canlı Kütüphane & Arşivden Getir (Bedava)"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = when (lang) {
                                        AppLanguage.RU -> "Загружайте любые разделы священных текстов, включая 24 книги Торы (Sefaria), Библию (Bible API) и Священный Коран, совершенно бесплатно, без ключей API."
                                        AppLanguage.EN -> "Load any section of the sacred scriptures including the 24 volumes of Torah (Sefaria), Bible (Bible API), and the Holy Quran completely free, without API keys, instantly in English & Original language."
                                        AppLanguage.TR -> "24 cilt Tevrat (Sefaria), İncil (Bible API) ve Kur'an-ı Kerim dahil kütüphanedeki kutsal metinlerin dilediğiniz bölümünü tamamen ücretsiz, anahtarsız ve sınırsız olarak anında Türkçe & Orijinal dilinde yükleyin."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = aiQueryText,
                                    onValueChange = { aiQueryText = it },
                                    label = {
                                        Text(
                                            when (lang) {
                                                AppLanguage.RU -> "Название главы или темы"
                                                AppLanguage.EN -> "Chapter or Subject Title"
                                                AppLanguage.TR -> "Bölüm veya Konu Başlığı"
                                            }
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            when (book.id) {
                                                "torah" -> when (lang) {
                                                    AppLanguage.RU -> "напр., Бытие 1, Исход 20, Псалтирь 23..."
                                                    AppLanguage.EN -> "e.g., Genesis 1, Exodus 20, Psalms 23..."
                                                    AppLanguage.TR -> "Örn: Yaratılış 1, Çıkış 20, Mezmurlar 23..."
                                                }
                                                "sermon" -> when (lang) {
                                                    AppLanguage.RU -> "напр., От Матфея 5, От Матфея 6, От Матфея 7..."
                                                    AppLanguage.EN -> "e.g., Matthew 5, Matthew 6, Matthew 7..."
                                                    AppLanguage.TR -> "Örn: Matta 5, Matta 6, Matta 7..."
                                                }
                                                else -> when (lang) {
                                                    AppLanguage.RU -> "напр., Глава 1..."
                                                    AppLanguage.EN -> "e.g., Chapter 1..."
                                                    AppLanguage.TR -> "Örn: Bölüm 1..."
                                                }
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        if (aiQueryText.isNotEmpty()) {
                                            IconButton(onClick = { aiQueryText = "" }) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = when (lang) {
                                                        AppLanguage.RU -> "Очистить"
                                                        AppLanguage.EN -> "Clear"
                                                        AppLanguage.TR -> "Temizle"
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                 // Complete books list for quick access
                                 val allBooksOfCategory = when (book.id) {
                                     "torah" -> {
                                         when (lang) {
                                             AppLanguage.RU -> listOf(
                                                 "Бытие", "Исход", "Левит", "Числа", "Второзаконие", "Иисус Навин", "Судьи", "Руфь", "1 Царств", "2 Царств",
                                                 "3 Царств", "4 Царств", "1 Паралипоменон", "2 Паралипоменон", "Ездра", "Неемия", "Есфирь", "Иов", "Псалтирь",
                                                 "Притчи", "Екклесиаст", "Песнь Песней", "Исаия", "Иеремия", "Плач Иеремии", "Иезекииль", "Даниил", "Осия", "Иоиль",
                                                 "Амос", "Авдий", "Иона", "Михей", "Наум", "Аввакум", "Софония", "Аггей", "Захария", "Малахия"
                                             )
                                             AppLanguage.EN -> listOf(
                                                 "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
                                                 "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job", "Psalms",
                                                 "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel",
                                                 "Amos", "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi"
                                             )
                                             AppLanguage.TR -> listOf(
                                                 "Yaratılış", "Çıkış", "Levililer", "Sayılar", "Yasanın Tekrarı",
                                                 "Yeşu", "Hakimler", "Rut", "1. Samuel", "2. Samuel",
                                                 "1. Krallar", "2. Krallar", "1. Tarihler", "2. Tarihler",
                                                 "Ezra", "Nehemya", "Ester", "Eyüp", "Mezmurlar",
                                                 "Özdeyişler", "Vaiz", "Ezgi", "Yeşaya", "Yeremya",
                                                 "Ağıtlar", "Hezekiel", "Daniel", "Hoşea", "Yoel",
                                                 "Amos", "Obadya", "Yunus", "Mika", "Nahum",
                                                 "Habakkuk", "Sefanya", "Hagay", "Zekeriya", "Malaki"
                                             )
                                         }
                                     }
                                     "sermon" -> {
                                         when (lang) {
                                             AppLanguage.RU -> listOf(
                                                 "От Матфея", "От Марка", "От Луки", "От Иоанна", "Деяния", "Римлянам", "1 Коринфянам", "2 Коринфянам", "Галатам",
                                                 "Ефесянам", "Филиппийцам", "Колоссянам", "1 Фессалоникийцам", "2 Фессалоникийцам", "1 Тимофею", "2 Тимофею", "Титу",
                                                 "Филимону", "Евреям", "Иакова", "1 Петра", "2 Петра", "1 Иоанна", "2 Иоанна", "3 Иоанна", "Иуды", "Откровение"
                                             )
                                             AppLanguage.EN -> listOf(
                                                 "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians",
                                                 "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus",
                                                 "Philemon", "Hebrews", "James", "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"
                                             )
                                             AppLanguage.TR -> listOf(
                                                 "Matta", "Markos", "Luka", "Yuhanna", "Elçilerin İşleri",
                                                 "Romalılar", "1. Korintliler", "2. Korintliler", "Galatyalılar",
                                                 "Efesliler", "Filipililer", "Koloseliler", "1. Selanikliler",
                                                 "2. Selanikliler", "1. Timoteyus", "2. Timoteyus", "Titus",
                                                 "Filimon", "İbraniler", "Yakup", "1. Petrus", "2. Petrus",
                                                 "1. Yuhanna", "2. Yuhanna", "3. Yuhanna", "Yahuda", "Vahiy"
                                             )
                                         }
                                     }
                                     "talmud" -> {
                                         listOf(
                                             "Berakhot", "Shabbat", "Eruvin", "Pesachim", "Yoma", "Sukkah", "Beitzah", "Rosh Hashanah", "Taanit", "Megillah", "Moed Katan", "Chagigah"
                                         )
                                     }
                                     "bukhari" -> {
                                         listOf(
                                             "Hadith 1", "Hadith 15", "Hadith 42", "Hadith 100", "Hadith 200", "Hadith 500"
                                         )
                                     }
                                     else -> emptyList()
                                 }

                                 if (allBooksOfCategory.isNotEmpty()) {
                                     Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                         Text(
                                             text = when (lang) {
                                                 AppLanguage.RU -> "Быстрый выбор книги (${allBooksOfCategory.size} книг):"
                                                 AppLanguage.EN -> "Quick Book Selection (${allBooksOfCategory.size} Books):"
                                                 AppLanguage.TR -> "Hızlı Kitap Seçimi (${allBooksOfCategory.size} Kitap):"
                                             },
                                             style = MaterialTheme.typography.labelSmall,
                                             color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                         )
                                         LazyRow(
                                             horizontalArrangement = Arrangement.spacedBy(6.dp),
                                             modifier = Modifier.fillMaxWidth()
                                         ) {
                                             items(allBooksOfCategory) { itemBook ->
                                                 SuggestionChip(
                                                     onClick = { aiQueryText = "$itemBook 1" },
                                                     label = { Text(itemBook, style = MaterialTheme.typography.labelSmall) }
                                                 )
                                             }
                                         }
                                     }
                                 }

                                // Quick suggestions chips
                                val suggestions = when (book.id) {
                                    "torah" -> when (lang) {
                                        AppLanguage.RU -> listOf("Бытие 1", "Псалтирь 23", "Исход 20")
                                        AppLanguage.EN -> listOf("Genesis 1", "Psalms 23", "Exodus 20")
                                        AppLanguage.TR -> listOf("Yaratılış 1", "Mezmurlar 23", "Çıkış 20")
                                    }
                                    "sermon" -> when (lang) {
                                        AppLanguage.RU -> listOf("От Матфея 5", "От Матфея 6", "От Матфея 7")
                                        AppLanguage.EN -> listOf("Matthew 5", "Matthew 6", "Matthew 7")
                                        AppLanguage.TR -> listOf("Matta 5", "Matta 6", "Matta 7")
                                    }
                                    "talmud" -> listOf("Berakhot 2a", "Shabbat 2a", "Megillah 2a")
                                    "bukhari" -> listOf("Hadith 1", "Hadith 15", "Hadith 42")
                                    "gita" -> when (lang) {
                                        AppLanguage.RU -> listOf("Санкхья-йога", "Карма-йога", "Бхакти-йога")
                                        AppLanguage.EN -> listOf("Sankhya Yoga", "Karma Yoga", "Bhakti Yoga")
                                        AppLanguage.TR -> listOf("Bilgelik ve Ruh", "Karma Yoga", "Bhakti Yoga")
                                    }
                                    else -> when (lang) {
                                        AppLanguage.RU -> listOf("Глава 1", "Глава 2")
                                        AppLanguage.EN -> listOf("Chapter 1", "Chapter 2")
                                        AppLanguage.TR -> listOf("Bölüm 1", "Bölüm 2")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    suggestions.forEach { suggestion ->
                                        SuggestionChip(
                                            onClick = { aiQueryText = suggestion },
                                            label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (aiQueryText.isNotBlank()) {
                                            viewModel.loadBookContentWithAI(book.id, aiQueryText)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SacredGold,
                                        contentColor = Color.White
                                    ),
                                    enabled = aiQueryText.isNotBlank() && !isBookLoading,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (isBookLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            when (lang) {
                                                AppLanguage.RU -> "Загрузка главы..."
                                                AppLanguage.EN -> "Fetching Chapter..."
                                                AppLanguage.TR -> "Bölüm Getiriliyor..."
                                            }
                                        )
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            when (lang) {
                                                AppLanguage.RU -> "Загрузить главу из архива (бесплатно)"
                                                AppLanguage.EN -> "Load Chapter Live/Archive (Free)"
                                                AppLanguage.TR -> "Bölümü Canlı/Arşivden Yükle (Ücretsiz)"
                                            }
                                        )
                                    }
                                }

                                if (bookError != null) {
                                    Text(
                                        text = "${when (lang) { AppLanguage.RU -> "Ошибка"; AppLanguage.EN -> "Error"; AppLanguage.TR -> "Hata" }}: $bookError",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

                // Core Scripture Paragraphs
                items(book.paragraphs.size) { index ->
                    val paragraphText = book.paragraphs[index]
                    val originalText = book.originalParagraphs.getOrNull(index) ?: ""
                    val isFirst = index == 0

                    val customTextSize = readerSettings.fontSizeSp.sp
                    val customLineHeight = (readerSettings.fontSizeSp * readerSettings.lineHeight.value).sp
                    val customFontFamily = if (readerSettings.fontFamily == FontFamilySetting.SERIF) FontFamily.Serif else FontFamily.SansSerif

                    val isRtl = book.originalLanguageName.contains("İbranice") ||
                                book.originalLanguageName.contains("Arapça") ||
                                book.originalLanguageName.contains("Hebrew") ||
                                book.originalLanguageName.contains("Arabic")
                    val origTextAlign = if (isRtl) TextAlign.Right else TextAlign.Left

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                noteQuoteText = if (languageMode == "original" && originalText.isNotEmpty()) originalText else paragraphText
                                showAddNoteDialog = true
                            }
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (languageMode) {
                                "original" -> {
                                    if (originalText.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "${index + 1}  ",
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                                            )
                                            Text(
                                                text = originalText,
                                                fontFamily = FontFamily.Serif,
                                                fontSize = (readerSettings.fontSizeSp + 1).sp,
                                                lineHeight = (readerSettings.fontSizeSp * 1.5).sp,
                                                color = SacredGold,
                                                textAlign = origTextAlign,
                                                fontStyle = FontStyle.Italic,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                                "bilingual" -> {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "${index + 1}  ",
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Turkish Text
                                            Text(
                                                text = paragraphText,
                                                fontFamily = customFontFamily,
                                                fontSize = customTextSize,
                                                lineHeight = customLineHeight,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            // Original Text Box
                                            if (originalText.isNotEmpty()) {
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                                    ),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = originalText,
                                                        fontFamily = FontFamily.Serif,
                                                        fontSize = (readerSettings.fontSizeSp - 1).sp,
                                                        lineHeight = (readerSettings.fontSizeSp * 1.4).sp,
                                                        color = SacredGold,
                                                        textAlign = origTextAlign,
                                                        fontStyle = FontStyle.Italic,
                                                        modifier = Modifier.padding(8.dp).fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    // standard turkish mode
                                    if (readerSettings.showOriginalScript && originalText.isNotEmpty()) {
                                        Text(
                                            text = if (originalText.contains(": ")) originalText.substringAfter(": ") else originalText,
                                            fontFamily = FontFamily.Serif,
                                            fontSize = (readerSettings.fontSizeSp).sp,
                                            lineHeight = (readerSettings.fontSizeSp * 1.5).sp,
                                            color = SacredGold,
                                            textAlign = origTextAlign,
                                            fontStyle = FontStyle.Italic,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                        )
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    if (isFirst && paragraphText.isNotEmpty() && !readerSettings.showOriginalScript) {
                                        val firstChar = paragraphText.take(1)
                                        val restOfText = paragraphText.drop(1)

                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 12.dp, top = 4.dp)
                                                    .background(Color.Transparent),
                                                contentAlignment = Alignment.TopCenter
                                            ) {
                                                Text(
                                                    text = firstChar,
                                                    fontFamily = FontFamily.Serif,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = (readerSettings.fontSizeSp * 2.8).sp,
                                                    lineHeight = (readerSettings.fontSizeSp * 2.4).sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.align(Alignment.TopCenter)
                                                )
                                            }
                                            Text(
                                                text = restOfText,
                                                fontFamily = customFontFamily,
                                                fontSize = customTextSize,
                                                lineHeight = customLineHeight,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    } else {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "${index + 1}  ",
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                                            )
                                            Text(
                                                text = paragraphText,
                                                fontFamily = customFontFamily,
                                                fontSize = customTextSize,
                                                lineHeight = customLineHeight,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footnotes section
                if (book.footnotes.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.RU -> "Критические примечания"
                                    AppLanguage.EN -> "Critical Notes"
                                    AppLanguage.TR -> "Kritik Notlar"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = SacredGold,
                                letterSpacing = 1.sp
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                book.footnotes.forEach { (ref, note) ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = ref,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(36.dp)
                                        )
                                        Text(
                                            text = note,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                lineHeight = 20.sp,
                                                fontStyle = FontStyle.Italic
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Appearance Customization Slider Sheet
            AnimatedVisibility(
                visible = showSettingsPanel,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Loc.get("appearance_settings", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { showSettingsPanel = false }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = when (lang) {
                                        AppLanguage.RU -> "Закрыть"
                                        AppLanguage.EN -> "Close"
                                        AppLanguage.TR -> "Kapat"
                                    }
                                )
                            }
                        }

                        // Theme Options
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = Loc.get("theme", lang)?.uppercase() ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val themes = listOf(
                                    Triple(when (lang) { AppLanguage.RU -> "Светлая"; AppLanguage.EN -> "Light"; AppLanguage.TR -> "Aydınlık" }, AppThemeSetting.LIGHT, Color.White),
                                    Triple(when (lang) { AppLanguage.RU -> "Темная"; AppLanguage.EN -> "Dark"; AppLanguage.TR -> "Karanlık" }, AppThemeSetting.DARK, Color(0xFF1E2120)),
                                    Triple(when (lang) { AppLanguage.RU -> "Сепия"; AppLanguage.EN -> "Sepia"; AppLanguage.TR -> "Antik" }, AppThemeSetting.SEPIA, Color(0xFFF4ECD8))
                                )
                                themes.forEach { (label, setting, bg) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bg)
                                            .border(
                                                width = if (readerSettings.theme == setting) 2.dp else 1.dp,
                                                color = if (readerSettings.theme == setting) SacredGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.updateThemeSetting(setting) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (setting == AppThemeSetting.DARK) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        // Text Size Slider
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.RU -> "РАЗМЕР ШРИФТА"
                                        AppLanguage.EN -> "FONT SIZE"
                                        AppLanguage.TR -> "METİN BOYUTU"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${readerSettings.fontSizeSp.toInt()}px",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                              ) {
                                  Icon(
                                      imageVector = Icons.Filled.FormatSize,
                                      contentDescription = null,
                                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                      modifier = Modifier.size(16.dp)
                                  )
                                  Slider(
                                      value = readerSettings.fontSizeSp,
                                      onValueChange = { viewModel.updateFontSize(it) },
                                      valueRange = 14f..32f,
                                      colors = SliderDefaults.colors(
                                          thumbColor = MaterialTheme.colorScheme.primary,
                                          activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                      ),
                                      modifier = Modifier
                                          .weight(1f)
                                          .testTag("font_size_slider")
                                  )
                                  Icon(
                                      imageVector = Icons.Filled.FormatSize,
                                      contentDescription = null,
                                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                      modifier = Modifier.size(24.dp)
                                  )
                              }
                          }

                          // Font Family Option
                          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                              Text(
                                  text = when (lang) {
                                      AppLanguage.RU -> "ШРИФТ"
                                      AppLanguage.EN -> "FONT FAMILY"
                                      AppLanguage.TR -> "YAZI TİPİ"
                                  },
                                  style = MaterialTheme.typography.labelMedium,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant
                              )
                              Row(
                                  modifier = Modifier.fillMaxWidth(),
                                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                              ) {
                                  Card(
                                      modifier = Modifier
                                          .weight(1f)
                                          .clickable { viewModel.updateFontFamily(FontFamilySetting.SERIF) },
                                      colors = CardDefaults.cardColors(
                                          containerColor = if (readerSettings.fontFamily == FontFamilySetting.SERIF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                      ),
                                      shape = RoundedCornerShape(8.dp)
                                  ) {
                                      Box(
                                          modifier = Modifier.fillMaxWidth().height(48.dp),
                                          contentAlignment = Alignment.Center
                                      ) {
                                          Text(
                                              text = when (lang) {
                                                  AppLanguage.RU -> "С засечками"
                                                  AppLanguage.EN -> "Serif (Classic)"
                                                  AppLanguage.TR -> "Serif (Klasik)"
                                              },
                                              fontFamily = FontFamily.Serif,
                                              fontWeight = FontWeight.Bold,
                                              color = if (readerSettings.fontFamily == FontFamilySetting.SERIF) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                          )
                                      }
                                  }

                                  Card(
                                      modifier = Modifier
                                          .weight(1f)
                                          .clickable { viewModel.updateFontFamily(FontFamilySetting.SANS_SERIF) },
                                      colors = CardDefaults.cardColors(
                                          containerColor = if (readerSettings.fontFamily == FontFamilySetting.SANS_SERIF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                      ),
                                      shape = RoundedCornerShape(8.dp)
                                  ) {
                                      Box(
                                          modifier = Modifier.fillMaxWidth().height(48.dp),
                                          contentAlignment = Alignment.Center
                                      ) {
                                          Text(
                                              text = when (lang) {
                                                  AppLanguage.RU -> "Без засечек"
                                                  AppLanguage.EN -> "Sans-serif (Modern)"
                                                  AppLanguage.TR -> "Sans-serif (Modern)"
                                              },
                                              fontFamily = FontFamily.SansSerif,
                                              fontWeight = FontWeight.Bold,
                                              color = if (readerSettings.fontFamily == FontFamilySetting.SANS_SERIF) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                          )
                                      }
                                  }
                              }
                          }

                          // Line Spacing Option
                          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                              Text(
                                  text = when (lang) {
                                      AppLanguage.RU -> "МЕЖСТРОЧНЫЙ ИНТЕРВАЛ"
                                      AppLanguage.EN -> "LINE SPACING"
                                      AppLanguage.TR -> "SATIR ARALIĞI"
                                  },
                                  style = MaterialTheme.typography.labelMedium,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant
                              )
                              Row(
                                  modifier = Modifier.fillMaxWidth(),
                                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                              ) {
                                  val lineHeights = listOf(
                                      (when (lang) { AppLanguage.RU -> "Узкий"; AppLanguage.EN -> "Tight"; AppLanguage.TR -> "Dar" }) to LineHeightSetting.TIGHT,
                                      (when (lang) { AppLanguage.RU -> "Средний"; AppLanguage.EN -> "Medium"; AppLanguage.TR -> "Orta" }) to LineHeightSetting.NORMAL,
                                      (when (lang) { AppLanguage.RU -> "Широкий"; AppLanguage.EN -> "Wide"; AppLanguage.TR -> "Geniş" }) to LineHeightSetting.WIDE
                                  )
                                  lineHeights.forEach { (label, setting) ->
                                      Card(
                                          modifier = Modifier
                                              .weight(1f)
                                              .clickable { viewModel.updateLineHeight(setting) },
                                          colors = CardDefaults.cardColors(
                                              containerColor = if (readerSettings.lineHeight == setting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                          ),
                                          shape = RoundedCornerShape(8.dp)
                                      ) {
                                          Box(
                                              modifier = Modifier.fillMaxWidth().height(44.dp),
                                              contentAlignment = Alignment.Center
                                          ) {
                                              Text(
                                                  text = label,
                                                  style = MaterialTheme.typography.labelLarge,
                                                  fontWeight = FontWeight.Bold,
                                                  color = if (readerSettings.lineHeight == setting) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                              )
                                          }
                                      }
                                  }
                              }
                          }
                      }
                  }
              }
          }
      }
  }
