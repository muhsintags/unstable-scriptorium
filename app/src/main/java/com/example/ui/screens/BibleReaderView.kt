package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBook
import com.example.data.model.BibleRepository
import com.example.data.model.Book
import com.example.ui.theme.SacredGold
import com.example.ui.viewmodel.FontFamilySetting
import com.example.ui.viewmodel.ScriptureViewModel
import com.example.ui.util.AppLanguage
import com.example.ui.util.Loc
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderView(
    viewModel: ScriptureViewModel,
    book: Book,
    onNavigateBack: () -> Unit
) {
    val isTorah = book.id == "torah"
    
    // Select correct state depending on which book we are in
    val currentSelectedBook by when (book.id) {
        "torah" -> viewModel.currentSelectedTorahBook
        "sermon" -> viewModel.currentSelectedSermonBook
        "talmud" -> viewModel.currentSelectedTalmudBook
        "bukhari" -> viewModel.currentSelectedBukhariBook
        "gita" -> viewModel.currentSelectedGitaBook
        else -> viewModel.currentSelectedTorahBook
    }.collectAsState()

    val currentSelectedChapter by when (book.id) {
        "torah" -> viewModel.currentSelectedTorahChapter
        "sermon" -> viewModel.currentSelectedSermonChapter
        "talmud" -> viewModel.currentSelectedTalmudChapter
        "bukhari" -> viewModel.currentSelectedBukhariChapter
        "gita" -> viewModel.currentSelectedGitaChapter
        else -> viewModel.currentSelectedTorahChapter
    }.collectAsState()
    
    val activeBookContentState by viewModel.activeBookContent.collectAsState()
    val activeBook = activeBookContentState ?: book
    val isBookLoading by viewModel.isBookLoading.collectAsState()
    val bookError by viewModel.bookError.collectAsState()
    
    val readerSettings by viewModel.readerSettings.collectAsState()
    val isEnglish = readerSettings.language == AppLanguage.EN
    val lang = readerSettings.language

    val currentPlayingUrl by viewModel.currentPlayingUrl.collectAsState()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()
    val isAudioLoading by viewModel.isAudioLoading.collectAsState()
    val activePlayingVerseIndex by viewModel.activePlayingVerseIndex.collectAsState()
    val downloadedChapters by viewModel.downloadedChapters.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    val chapterKey = currentSelectedBook?.let { "${book.id}_${it.id}_$currentSelectedChapter" } ?: ""
    val isChapterDownloaded = downloadedChapters.contains(chapterKey)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showLanguageCard by remember { mutableStateOf(false) }

    LaunchedEffect(currentSelectedChapter, activeBook) {
        val chapterVal = currentSelectedChapter
        if (book.id == "bukhari" && chapterVal != null && activeBook != null) {
            kotlinx.coroutines.delay(100)
            val paragraphsSize = activeBook.paragraphs.size
            if (chapterVal in 1..paragraphsSize) {
                listState.scrollToItem(chapterVal)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.stopAudio()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAudio()
        }
    }

    // Automatic progress and timing tracker
    val startTime = remember(chapterKey) { System.currentTimeMillis() }
    var elapsedSeconds by remember(chapterKey) { mutableStateOf(0) }
    LaunchedEffect(chapterKey) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsedSeconds++
        }
    }
    val autoMinutes = (elapsedSeconds / 60).coerceAtLeast(0)

    val maxScrolledParagraphIndex = remember(chapterKey) { mutableStateOf(0) }
    val visibleIndices = remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.map { it.index }
        }
    }
    
    val paragraphsCount = activeBook?.paragraphs?.size ?: 1
    LaunchedEffect(visibleIndices.value, chapterKey) {
        val lastVisible = visibleIndices.value.lastOrNull() ?: 0
        // Item 0 is the download/header block.
        // Paragraphs start at item 1.
        val currentParagraphIdx = (lastVisible - 1).coerceIn(0, paragraphsCount - 1)
        if (currentParagraphIdx > maxScrolledParagraphIndex.value) {
            maxScrolledParagraphIndex.value = currentParagraphIdx
        }
    }

    val totalPagesOfChapter = (paragraphsCount / 4.0).coerceAtLeast(1.0).toInt()
    val autoPagesRead = ((maxScrolledParagraphIndex.value + 1).toFloat() / paragraphsCount.toFloat() * totalPagesOfChapter)
        .toInt().coerceIn(1, totalPagesOfChapter)

    // Check if user has scrolled to the bottom (lastVisible index is >= paragraphsCount + 1)
    val isBottomReached = remember(chapterKey) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= paragraphsCount + 1
        }
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var languageMode by remember { mutableStateOf("turkish") } // "turkish", "original", "bilingual"
    var isBookmarked by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteQuoteText by remember { mutableStateOf("") }
    var noteTextQuery by remember { mutableStateOf("") }
    
    val booksList = when (book.id) {
        "torah" -> BibleRepository.torahBooks
        "sermon" -> BibleRepository.bibleBooks
        "talmud" -> BibleRepository.talmudBooks
        "bukhari" -> BibleRepository.bukhariBooks
        "gita" -> BibleRepository.gitaBooks
        else -> BibleRepository.bibleBooks
    }
    val filteredBooks = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            booksList
        } else {
            booksList.filter {
                it.nameTurkish.contains(searchQuery, ignoreCase = true) ||
                it.nameEnglish.contains(searchQuery, ignoreCase = true) ||
                it.bookNumber.toString() == searchQuery
            }
        }
    }
    
    // Dialog for adding notes
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = {
                Text(
                    text = when (lang) {
                        AppLanguage.RU -> "Добавить размышление или заметку"
                        AppLanguage.EN -> "Add Reflection or Note"
                        AppLanguage.TR -> "Tefekkür veya Not Ekle"
                    },
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.RU -> "Выбранный отрывок:"
                            AppLanguage.EN -> "Selected Passage:"
                            AppLanguage.TR -> "Seçilen Pasaj:"
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
                                    AppLanguage.RU -> "Запишите ваши мысли об этом отрывке..."
                                    AppLanguage.EN -> "Write your thoughts about this passage..."
                                    AppLanguage.TR -> "Bu pasaj hakkındaki düşüncelerinizi yazın..."
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("bible_note_input_field"),
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
                                bookTitle = activeBook.title + " - " + activeBook.contentTitle,
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
                                bookTitle = activeBook.title + " - " + activeBook.contentTitle,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentSelectedBook != null && currentSelectedChapter != null) {
                                val name = currentSelectedBook?.getName(lang)
                                if (book.id == "talmud") {
                                    val pageNum = 2 + ((currentSelectedChapter ?: 1) - 1) / 2
                                    val side = if ((currentSelectedChapter ?: 1) % 2 == 1) "a" else "b"
                                    "$name $pageNum$side"
                                } else {
                                    val chLabel = when (lang) {
                                        AppLanguage.RU -> "Глава $currentSelectedChapter"
                                        AppLanguage.EN -> "$currentSelectedChapter"
                                        AppLanguage.TR -> "$currentSelectedChapter. Bölüm"
                                    }
                                    "$name $chLabel"
                                }
                            } else if (currentSelectedBook != null) {
                                currentSelectedBook!!.getName(lang)
                            } else {
                                Loc.get(book.id, lang)
                            },
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentSelectedBook != null) {
                            Text(
                                text = "${currentSelectedBook?.getName(lang)} • ${currentSelectedBook?.sourceLanguage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentSelectedChapter != null) {
                            when (book.id) {
                                "torah" -> viewModel.selectTorahChapter(null)
                                "sermon" -> viewModel.selectSermonChapter(null)
                                "talmud" -> viewModel.selectTalmudChapter(null)
                                "bukhari" -> viewModel.selectBukhariChapter(null)
                                "gita" -> viewModel.selectGitaChapter(null)
                            }
                        } else if (currentSelectedBook != null) {
                            when (book.id) {
                                "torah" -> viewModel.selectTorahBook(null)
                                "sermon" -> viewModel.selectSermonBook(null)
                                "talmud" -> viewModel.selectTalmudBook(null)
                                "bukhari" -> viewModel.selectBukhariBook(null)
                                "gita" -> viewModel.selectGitaBook(null)
                            }
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = Loc.get("back", lang),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (currentSelectedBook != null && currentSelectedChapter != null) {
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
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (currentSelectedBook != null && currentSelectedChapter != null && !isBookLoading) {
                ExtendedFloatingActionButton(
                    onClick = {
                        noteQuoteText = activeBook.paragraphs.firstOrNull() ?: ""
                        showAddNoteDialog = true
                    },
                    icon = { Icon(Icons.Filled.EditNote, when (lang) { AppLanguage.RU -> "Добавить заметку"; AppLanguage.EN -> "Add Note"; AppLanguage.TR -> "Not Ekle" }) },
                    text = { Text(when (lang) { AppLanguage.RU -> "Быстрая заметка"; AppLanguage.EN -> "Quick Note"; AppLanguage.TR -> "Hızlı Not Al" }) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentSelectedBook == null) {
                // BOOK INDEX VIEW (FİHRİST DÜZENİ)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = when (book.id) {
                            "torah" -> when (lang) {
                                AppLanguage.RU -> "Библиотека Торы"
                                AppLanguage.EN -> "Torah Library"
                                AppLanguage.TR -> "Tevrat Kütüphanesi"
                            }
                            "sermon" -> when (lang) {
                                AppLanguage.RU -> "Библиотека Евангелия"
                                AppLanguage.EN -> "Gospel Library"
                                AppLanguage.TR -> "İncil Kütüphanesi"
                            }
                            "talmud" -> when (lang) {
                                AppLanguage.RU -> "Библиотека Талмуда"
                                AppLanguage.EN -> "Talmud Library"
                                AppLanguage.TR -> "Talmud Kütüphanesi"
                            }
                            "bukhari" -> when (lang) {
                                AppLanguage.RU -> "Библиотека Сахих аль-Бухари"
                                AppLanguage.EN -> "Sahih al-Bukhari Library"
                                AppLanguage.TR -> "Sahih-i Buharî Kütüphanesi"
                            }
                            "gita" -> when (lang) {
                                AppLanguage.RU -> "Библиотека Бхагавад-гиты"
                                AppLanguage.EN -> "Bhagavad Gita Library"
                                AppLanguage.TR -> "Bhagavad Gita Kütüphanesi"
                            }
                            else -> when (lang) {
                                AppLanguage.RU -> "Библиотека"
                                AppLanguage.EN -> "Library"
                                AppLanguage.TR -> "Kütüphane"
                            }
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SacredGold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = when (book.id) {
                            "torah" -> when (lang) {
                                AppLanguage.RU -> "Размышляйте над священными текстами на иврите и переводами."
                                AppLanguage.EN -> "Contemplate Hebrew scriptures and modern translations."
                                AppLanguage.TR -> "İbranice kutsal metinleri ve Türkçe çevirileri tefekkür edin."
                            }
                            "sermon" -> when (lang) {
                                AppLanguage.RU -> "Читайте учения Иисуса Христа в греческом оригинале и современных переводах."
                                AppLanguage.EN -> "Read the teachings of Jesus Christ in their Greek originals and modern translations."
                                AppLanguage.TR -> "Grekçe asılları ve Türkçe çevirileri ile İsa Mesih'in öğretilerini okuyun."
                            }
                            "talmud" -> when (lang) {
                                AppLanguage.RU -> "Исследуйте Вавилонский Талмуд с комментариями и параллельным арамейским текстом."
                                AppLanguage.EN -> "Explore the Babylonian Talmud with commentary and parallel Aramaic text."
                                AppLanguage.TR -> "Tefsirler ve paralel Aramice metinler eşliğinde Babil Talmudu'nu okuyun."
                            }
                            "bukhari" -> when (lang) {
                                AppLanguage.RU -> "Читайте Сахих аль-Бухари с переводами и арабскими оригиналами."
                                AppLanguage.EN -> "Read Sahih al-Bukhari with English and Turkish translations."
                                AppLanguage.TR -> "Sahih-i Buharî'yi Türkçe çevirileri ve Arapça asılları ile inceleyin."
                            }
                            "gita" -> when (lang) {
                                AppLanguage.RU -> "Исследуйте Бхагавад-гиту с оригинальными стихами на санскрите и переводами."
                                AppLanguage.EN -> "Explore Bhagavad Gita with Sanskrit original verses and translations."
                                AppLanguage.TR -> "Bhagavad Gita'yı Sanskritçe asılları ve çevirileri ile inceleyin."
                            }
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Download Entire Book Banner Card
                    val downloadedBooks by viewModel.downloadedBooks.collectAsState()
                    val downloadProgress by viewModel.downloadProgress.collectAsState()
                    val isBookDownloaded = downloadedBooks.contains(book.id)
                    val bookProgress = downloadProgress[book.id]

                    if (!isBookDownloaded || bookProgress != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CloudDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.RU -> "Скачать книгу целиком"
                                            AppLanguage.EN -> "Download Entire Book"
                                            AppLanguage.TR -> "Tüm Kitabı Cihaza İndir"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (bookProgress != null) {
                                            when (lang) {
                                                AppLanguage.RU -> "Загрузка: %${(bookProgress * 100).toInt()}"
                                                AppLanguage.EN -> "Downloading: %${(bookProgress * 100).toInt()}"
                                                AppLanguage.TR -> "İndiriliyor: %${(bookProgress * 100).toInt()}"
                                            }
                                        } else {
                                            when (lang) {
                                                AppLanguage.RU -> "Загрузите все разделы для чтения офлайн в любое время."
                                                AppLanguage.EN -> "Download all sections to read offline anytime."
                                                AppLanguage.TR -> "Tüm bölümleri cihazınıza kaydedip internetsiz okuyun."
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (bookProgress != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { bookProgress },
                                            color = SacredGold,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                if (bookProgress == null) {
                                    Button(
                                        onClick = { viewModel.downloadBook(book.id) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
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
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                when (lang) {
                                    AppLanguage.RU -> "Поиск книги по названию или номеру..."
                                    AppLanguage.EN -> "Search book name or number..."
                                    AppLanguage.TR -> "Kitap ismi veya numara ara..."
                                }
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = Loc.get("search", lang)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = when (lang) {
                                            AppLanguage.RU -> "Очистить"
                                            AppLanguage.EN -> "Clear"
                                            AppLanguage.TR -> "Temizle"
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bible_book_search_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Book List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(filteredBooks) { _, bibleBook ->
                            Card(
                                onClick = {
                                    when (book.id) {
                                        "torah" -> viewModel.selectTorahBook(bibleBook)
                                        "sermon" -> viewModel.selectSermonBook(bibleBook)
                                        "talmud" -> viewModel.selectTalmudBook(bibleBook)
                                        "bukhari" -> viewModel.selectBukhariBook(bibleBook)
                                        "gita" -> viewModel.selectGitaBook(bibleBook)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bible_book_card_${bibleBook.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Book Number badge
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = bibleBook.bookNumber.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Book names
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = bibleBook.getName(lang),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.RU -> "${bibleBook.nameEnglish} • ${bibleBook.sourceLanguage}"
                                                AppLanguage.EN -> "${bibleBook.nameTurkish} • ${bibleBook.sourceLanguage}"
                                                AppLanguage.TR -> "${bibleBook.nameEnglish} • ${bibleBook.sourceLanguage}"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Chapters Count on Right
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${bibleBook.chaptersCount}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SacredGold
                                        )
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.RU -> "Глав"
                                                AppLanguage.EN -> "Chapters"
                                                AppLanguage.TR -> "Bölüm"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (currentSelectedChapter == null) {
                // CHAPTER FAST ACCESS GRID (HIZLI ERİŞİM ŞEMASI)
                val selectedBook = currentSelectedBook!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.RU -> "Главы книги ${selectedBook.getName(lang)}"
                            AppLanguage.EN -> "${selectedBook.nameEnglish} Chapters"
                            AppLanguage.TR -> "${selectedBook.nameTurkish} Bölümleri"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SacredGold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.RU -> "Выберите главу, которую хотите прочитать, чтобы увидеть текст и комментарии."
                            AppLanguage.EN -> "Select the chapter you want to read to view text and commentary."
                            AppLanguage.TR -> "Okumak istediğiniz bölümü seçerek metni ve dipnotları görüntüleyin."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Chapter Grid / Flow Row for Fast Access
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (ch in 1..selectedBook.chaptersCount) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                when (book.id) {
                                                    "torah" -> viewModel.selectTorahChapter(ch)
                                                    "sermon" -> viewModel.selectSermonChapter(ch)
                                                    "talmud" -> viewModel.selectTalmudChapter(ch)
                                                    "bukhari" -> viewModel.selectBukhariChapter(ch)
                                                    "gita" -> viewModel.selectGitaChapter(ch)
                                                }
                                            }
                                            .testTag("chapter_button_$ch"),
                                        contentAlignment = Alignment.Center
                                     ) {
                                         val label = if (book.id == "talmud") {
                                             val pageNum = 2 + (ch - 1) / 2
                                             val side = if (ch % 2 == 1) "a" else "b"
                                             "$pageNum$side"
                                         } else {
                                             ch.toString()
                                         }
                                         Text(
                                             text = label,
                                             style = MaterialTheme.typography.titleMedium,
                                             fontWeight = FontWeight.Bold,
                                             color = MaterialTheme.colorScheme.primary
                                         )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ACTIVE CHAPTER VIEW
                if (isBookLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = SacredGold)
                            Text(
                                text = when (lang) {
                                    AppLanguage.RU -> "Загрузка и подготовка главы..."
                                    AppLanguage.EN -> "Loading and translating chapter..."
                                    AppLanguage.TR -> "Bölüm yükleniyor ve çevriliyor..."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else if (bookError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = bookError!!,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    val currentBook = currentSelectedBook
                                    val currentChapter = currentSelectedChapter
                                    if (currentBook != null && currentChapter != null) {
                                        when (book.id) {
                                            "torah" -> viewModel.selectTorahChapter(currentChapter)
                                            "sermon" -> viewModel.selectSermonChapter(currentChapter)
                                            "talmud" -> viewModel.selectTalmudChapter(currentChapter)
                                            "bukhari" -> viewModel.selectBukhariChapter(currentChapter)
                                            "gita" -> viewModel.selectGitaChapter(currentChapter)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SacredGold)
                            ) {
                                Text(
                                    when (lang) {
                                        AppLanguage.RU -> "Повторить"
                                        AppLanguage.EN -> "Retry"
                                        AppLanguage.TR -> "Tekrar Dene"
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // SHOW RENDERED SCRIPTURE
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                    ) {
                        // Header info
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = activeBook.subContentTitle.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SacredGold,
                                    letterSpacing = 3.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = activeBook.contentTitle,
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
                                if (activeBook.introText.isNotEmpty()) {
                                    Text(
                                        text = activeBook.introText,
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

                        // Reading Mode & Audio Row (Collapsible)
                        item {
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
                                                    AppLanguage.RU -> "ЯЗЫК ЧТЕНИЯ И НАСТРОЙКИ"
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
                                                contentDescription = when (lang) {
                                                    AppLanguage.RU -> "Скрыть/Показать"
                                                    AppLanguage.EN -> "Hide/Show"
                                                    AppLanguage.TR -> "Gizle/Göster"
                                                },
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (showLanguageCard) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        if (activeBook.originalParagraphs.isNotEmpty()) {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    val translationLabel = when (lang) {
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
                                                        "turkish" to translationLabel,
                                                        "original" to activeBook.originalLanguageName,
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
                                                                    fontSize = 11.sp,
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

                                        // Offline Status & Download Actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isChapterDownloaded) {
                                                        when (lang) {
                                                            AppLanguage.RU -> "Сохранено на устройстве"
                                                            AppLanguage.EN -> "Saved to Device"
                                                            AppLanguage.TR -> "Cihazda Kayıtlı"
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
                                                color = if (isChapterDownloaded) SacredGold else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isChapterDownloaded) {
                                                    when (lang) {
                                                        AppLanguage.RU -> "Сохранено для чтения в любое время."
                                                        AppLanguage.EN -> "Saved to read anytime."
                                                        AppLanguage.TR -> "Dilediğiniz an okumak için cihazınızda kayıtlı."
                                                    }
                                                } else {
                                                    when (lang) {
                                                        AppLanguage.RU -> "Скачайте эту главу для чтения в любое время."
                                                        AppLanguage.EN -> "Download this chapter to read anytime."
                                                        AppLanguage.TR -> "Bu bölümü dilediğiniz zaman okumak için indirin."
                                                    }
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        val progress = downloadProgress[chapterKey]
                                        if (progress != null && progress < 1f) {
                                            CircularProgressIndicator(
                                                progress = { progress },
                                                color = SacredGold,
                                                modifier = Modifier.size(28.dp),
                                                strokeWidth = 3.dp
                                            )
                                        } else {
                                            if (!isChapterDownloaded) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.downloadBibleChapter(
                                                            book.id,
                                                            currentSelectedBook!!,
                                                            currentSelectedChapter!!
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Download,
                                                        contentDescription = when (lang) {
                                                            AppLanguage.RU -> "Скачать главу"
                                                            AppLanguage.EN -> "Download chapter"
                                                            AppLanguage.TR -> "Bölümü indir"
                                                        },
                                                        tint = SacredGold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                        // Render Verses / Paragraphs
                        items(activeBook.paragraphs.size) { index ->
                            val paragraphText = activeBook.paragraphs[index]
                            val originalText = activeBook.originalParagraphs.getOrNull(index) ?: ""

                            val customTextSize = readerSettings.fontSizeSp.sp
                            val customLineHeight = (readerSettings.fontSizeSp * readerSettings.lineHeight.value).sp
                            val customFontFamily = if (readerSettings.fontFamily == FontFamilySetting.SERIF) FontFamily.Serif else FontFamily.SansSerif

                            val isRtl = activeBook.originalLanguageName.contains("İbranice") ||
                                        activeBook.originalLanguageName.contains("Hebrew") ||
                                        activeBook.originalLanguageName.contains("Arapça") ||
                                        activeBook.originalLanguageName.contains("Arabic") ||
                                        activeBook.originalLanguageName.contains("Aramice")
                            val origTextAlign = if (isRtl) TextAlign.Right else TextAlign.Left

                            val isVersePlaying = activePlayingVerseIndex == index

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isVersePlaying) {
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                                    } else {
                                        Color.Transparent
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        noteQuoteText = if (languageMode == "original" && originalText.isNotEmpty()) originalText else paragraphText
                                        showAddNoteDialog = true
                                    },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(if (isVersePlaying) 12.dp else 4.dp)
                                ) {
                                    // Action bar for the paragraph
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Paragraph Index Badge
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (index + 1).toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Actions: Note Play (Audio Play removed)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    noteQuoteText = if (languageMode == "original" && originalText.isNotEmpty()) originalText else paragraphText
                                                    showAddNoteDialog = true
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.EditNote,
                                                    contentDescription = when (lang) {
                                                        AppLanguage.RU -> "Добавить заметку"
                                                        AppLanguage.EN -> "Add Note"
                                                        AppLanguage.TR -> "Not Ekle"
                                                    },
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Render content texts
                                    when (languageMode) {
                                        "original" -> {
                                            if (originalText.isNotEmpty()) {
                                                Text(
                                                    text = if (originalText.contains(": ")) originalText.substringAfter(": ") else originalText,
                                                    fontSize = customTextSize,
                                                    lineHeight = customLineHeight,
                                                    fontFamily = customFontFamily,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = origTextAlign,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                        "bilingual" -> {
                                            if (originalText.isNotEmpty()) {
                                                Text(
                                                    text = if (originalText.contains(": ")) originalText.substringAfter(": ") else originalText,
                                                    fontSize = (readerSettings.fontSizeSp - 2).sp,
                                                    lineHeight = (customLineHeight.value - 4).sp,
                                                    fontFamily = customFontFamily,
                                                    color = SacredGold,
                                                    textAlign = origTextAlign,
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                                )
                                            }
                                            Text(
                                                text = paragraphText,
                                                fontSize = customTextSize,
                                                lineHeight = customLineHeight,
                                                fontFamily = customFontFamily,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        else -> { // "turkish"
                                            if (readerSettings.showOriginalScript && originalText.isNotEmpty()) {
                                                Text(
                                                    text = if (originalText.contains(": ")) originalText.substringAfter(": ") else originalText,
                                                    fontSize = (readerSettings.fontSizeSp).sp,
                                                    lineHeight = (customLineHeight.value + 4).sp,
                                                    fontFamily = customFontFamily,
                                                    color = SacredGold,
                                                    textAlign = origTextAlign,
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                                )
                                                HorizontalDivider(
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                            }
                                            Text(
                                                text = paragraphText,
                                                fontSize = customTextSize,
                                                lineHeight = customLineHeight,
                                                fontFamily = customFontFamily,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                        modifier = Modifier.padding(top = 16.dp)
                                    )
                                }
                            }
                        }

                        // Footnotes Section
                        if (activeBook.footnotes.isNotEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp, bottom = 48.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = null,
                                            tint = SacredGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.RU -> "ПОЯСНЕНИЯ И ПРИМЕЧАНИЯ"
                                                AppLanguage.EN -> "EXPLANATIONS & FOOTNOTES"
                                                AppLanguage.TR -> "AÇIKLAMALAR & DİPNOTLAR"
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    activeBook.footnotes.forEach { (ref, note) ->
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = ref,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = SacredGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = note,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 22.sp
                                            )
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    }
                                }
                            }
                        }

                        // Okumayı Kaydet / Tamamla Block
                        item {
                            val historyList by viewModel.readingHistory.collectAsState()
                            val bTitle = Loc.get(book.id, lang)
                            val previousPagesRead = historyList
                                .filter { it.bookTitle == bTitle }
                                .sumOf { it.pagesRead }
                            val newTotalPagesRead = previousPagesRead + autoPagesRead
                            val totalPages = when {
                                book.id == "torah" -> 300
                                book.id == "talmud" -> 2711
                                book.id == "bukhari" -> 2000
                                else -> 400 // sermon / gospel / incil
                            }
                            val calculatedProgress = ((newTotalPagesRead.toFloat() / totalPages.toFloat()) * 100f).toInt().coerceIn(1, 100)

                            var isSavedSuccessfully by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp, bottom = 40.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SacredGold.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.RU -> "Итоги сессии чтения"
                                                AppLanguage.EN -> "Reading Session Summary"
                                                AppLanguage.TR -> "Okuma Oturumu Özeti"
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        // Auto-track badge
                                        Surface(
                                            color = SacredGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = when (lang) {
                                                    AppLanguage.RU -> "Авторасчёт"
                                                    AppLanguage.EN -> "Auto-Calculated"
                                                    AppLanguage.TR -> "Otomatik Hesaplandı"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SacredGold,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    if (isSavedSuccessfully) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = when (lang) {
                                                    AppLanguage.RU -> "Успешно"
                                                    AppLanguage.EN -> "Success"
                                                    AppLanguage.TR -> "Başarılı"
                                                },
                                                tint = SacredGold
                                            )
                                            Text(
                                                text = when (lang) {
                                                    AppLanguage.RU -> "Запись о чтении успешно сохранена!"
                                                    AppLanguage.EN -> "Your reading record was successfully added!"
                                                    AppLanguage.TR -> "Okuma kaydınız başarıyla eklendi!"
                                                },
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    } else {
                                        val displaySection = currentSelectedBook?.getName(lang) ?: activeBook.title
                                        val displayChapter = currentSelectedChapter?.let {
                                            when (lang) {
                                                AppLanguage.RU -> " Глава $it"
                                                AppLanguage.EN -> " Chapter $it"
                                                AppLanguage.TR -> " $it. Bölüm"
                                            }
                                        } ?: ""

                                        // Show automated metrics in clean, beautiful badge rows
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Chapter/Section details
                                            Text(
                                                text = when (lang) {
                                                    AppLanguage.RU -> "Раздел: $displaySection$displayChapter"
                                                    AppLanguage.EN -> "Section: $displaySection$displayChapter"
                                                    AppLanguage.TR -> "Bölüm: $displaySection$displayChapter"
                                                },
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                                            // Page metric
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = SacredGold, modifier = Modifier.size(18.dp))
                                                    Text(
                                                        text = when (lang) {
                                                            AppLanguage.RU -> "Прочитано страниц"
                                                            AppLanguage.EN -> "Pages Read"
                                                            AppLanguage.TR -> "Okunan Sayfa"
                                                        },
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    text = when (lang) {
                                                        AppLanguage.RU -> "$autoPagesRead стр."
                                                        AppLanguage.EN -> "$autoPagesRead pages"
                                                        AppLanguage.TR -> "$autoPagesRead sayfa"
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            // Duration metric
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Filled.HourglassEmpty, contentDescription = null, tint = SacredGold, modifier = Modifier.size(18.dp))
                                                    Text(
                                                        text = when (lang) {
                                                            AppLanguage.RU -> "Время чтения"
                                                            AppLanguage.EN -> "Reading Time"
                                                            AppLanguage.TR -> "Okuma Süresi"
                                                        },
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    text = when (lang) {
                                                        AppLanguage.RU -> "$autoMinutes мин."
                                                        AppLanguage.EN -> "$autoMinutes min"
                                                        AppLanguage.TR -> "$autoMinutes dk"
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            // Cumulative progress metric
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = SacredGold, modifier = Modifier.size(18.dp))
                                                    Text(
                                                        text = when (lang) {
                                                            AppLanguage.RU -> "Новый прогресс книги"
                                                            AppLanguage.EN -> "New Book Progress"
                                                            AppLanguage.TR -> "Yeni Kitap İlerlemesi"
                                                        },
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    text = "%$calculatedProgress",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SacredGold
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.updateReadingSessionProgress(
                                                    bookTitle = bTitle,
                                                    subtitle = when (lang) {
                                                        AppLanguage.RU -> "$displaySection (Глава ${currentSelectedChapter ?: 1})"
                                                        AppLanguage.EN -> "$displaySection (Chapter ${currentSelectedChapter ?: 1})"
                                                        AppLanguage.TR -> "$displaySection (${currentSelectedChapter ?: 1}. Bölüm)"
                                                    },
                                                    progress = calculatedProgress,
                                                    surahOrChapter = "$displaySection (${currentSelectedChapter ?: 1})",
                                                    pagesRead = autoPagesRead,
                                                    isCompleted = isBottomReached.value,
                                                    contemplationMinutes = autoMinutes
                                                )
                                                isSavedSuccessfully = true
                                            },
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SacredGold)
                                        ) {
                                            Text(
                                                text = when (lang) {
                                                    AppLanguage.RU -> "Сохранить прогресс чтения"
                                                    AppLanguage.EN -> "Save Reading Progress"
                                                    AppLanguage.TR -> "Okuma İlerlemesini Kaydet"
                                                },
                                                color = Color.White
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
