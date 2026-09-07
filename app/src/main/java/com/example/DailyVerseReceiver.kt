package com.example

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.data.model.BookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class DailyVerseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            scheduleAlarm(context)
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.getSharedPreferences("scriptorium_auth", Context.MODE_PRIVATE)
                val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
                if (!notificationsEnabled) {
                    pendingResult.finish()
                    return@launch
                }

                // Get allowed books
                val savedBooks = prefs.getStringSet("selected_verse_books", null)
                val books = BookRepository.books
                val filteredBooks = if (savedBooks != null) {
                    books.filter { savedBooks.contains(it.id) }
                } else {
                    books
                }
                val targetBooks = if (filteredBooks.isEmpty()) books else filteredBooks

                // Choose a random book from the selected ones
                val randomBook = targetBooks.randomOrNull() ?: books.first()

                val settingsPrefs = context.getSharedPreferences("scriptorium_settings", Context.MODE_PRIVATE)
                val langStr = settingsPrefs.getString("language", "TR") ?: "TR"
                val lang = try {
                    com.example.ui.util.AppLanguage.valueOf(langStr)
                } catch (e: Exception) {
                    com.example.ui.util.AppLanguage.TR
                }

                // Fetch random verse from live API, fallback to offline if error
                val (ref, text) = fetchVerseFromApiWithFallback(randomBook.id, randomBook, lang)

                val savedReligionStr = prefs.getString("user_religion", "islam")
                val userReligion = com.example.ui.util.UserReligion.fromId(savedReligionStr)
                val savedSectStr = prefs.getString("user_sect", "sunni")
                val userSect = com.example.ui.util.UserSect.fromId(savedSectStr, userReligion)

                // Detect location and fetch live API prayer times
                val locationInfo = com.example.data.api.PrayerTimeApiService.detectLocation(context)
                val livePrayerSchedules = com.example.data.api.PrayerTimeApiService.fetchPrayerTimesFromApi(
                    latitude = locationInfo.latitude,
                    longitude = locationInfo.longitude,
                    religion = userReligion,
                    sect = userSect
                )

                val randomPrayer = livePrayerSchedules.randomOrNull()

                val finalTitle = if (randomPrayer != null) {
                    "📍 ${locationInfo.cityName} [${userSect.getTitle(lang)}] ${randomPrayer.getName(lang)} (${randomPrayer.timeStr})"
                } else ref

                val finalText = if (randomPrayer != null && ref != null && text != null) {
                    "${randomPrayer.getMessage(lang)}\n\n($ref): $text"
                } else text ?: ""

                if (finalTitle != null && finalText.isNotEmpty()) {
                    showNotification(context, finalTitle, finalText, lang)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                scheduleAlarm(context)
                pendingResult.finish()
            }
        }
    }

    private suspend fun fetchVerseFromApiWithFallback(bookId: String, fallbackBook: com.example.data.model.Book, lang: com.example.ui.util.AppLanguage): Pair<String, String> {
        val client = OkHttpClient()
        return try {
            when (bookId) {
                "quran" -> {
                    val randomAyah = (1..6236).random()
                    val quranEdition = when (lang) {
                        com.example.ui.util.AppLanguage.RU -> "ru.kuliev"
                        com.example.ui.util.AppLanguage.EN -> "en.yusufali"
                        com.example.ui.util.AppLanguage.TR -> "tr.yazir"
                    }
                    val url = "https://api.alquran.cloud/v1/ayah/$randomAyah/$quranEdition"
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("Quran API failure")
                        val body = response.body?.string() ?: ""
                        val json = JSONObject(body)
                        val dataObj = json.getJSONObject("data")
                        val text = dataObj.getString("text").trim()
                        val surahObj = dataObj.getJSONObject("surah")
                        val surahNum = surahObj.getInt("number")
                        val surahName = surahObj.getString("englishName")
                        val numberInSurah = dataObj.getInt("numberInSurah")
                        val surahLocalizedName = com.example.data.model.QuranRepository.surahs.find { it.number == surahNum }?.getName(lang) ?: surahName
                        val refHeader = when (lang) {
                            com.example.ui.util.AppLanguage.RU -> "СВЯЩЕННЫЙ КОРАН (Сура $surahLocalizedName, Аят $numberInSurah)"
                            com.example.ui.util.AppLanguage.EN -> "HOLY QURAN (Surah $surahLocalizedName, Verse $numberInSurah)"
                            com.example.ui.util.AppLanguage.TR -> "KUR'AN-I KERİM ($surahLocalizedName Suresi, Ayet $numberInSurah)"
                        }
                        Pair(refHeader, text)
                    }
                }
                "torah" -> {
                    // Random Torah book and chapter details
                    val torahBooks = when (lang) {
                        com.example.ui.util.AppLanguage.RU -> listOf(
                            Triple("genesis", "Бытие", 50),
                            Triple("exodus", "Исход", 40),
                            Triple("leviticus", "Левит", 27),
                            Triple("numbers", "Числа", 36),
                            Triple("deuteronomy", "Второзаконие", 34)
                        )
                        com.example.ui.util.AppLanguage.EN -> listOf(
                            Triple("genesis", "Genesis", 50),
                            Triple("exodus", "Exodus", 40),
                            Triple("leviticus", "Leviticus", 27),
                            Triple("numbers", "Numbers", 36),
                            Triple("deuteronomy", "Deuteronomy", 34)
                        )
                        com.example.ui.util.AppLanguage.TR -> listOf(
                            Triple("genesis", "Yaratılış", 50),
                            Triple("exodus", "Mısır'dan Çıkış", 40),
                            Triple("leviticus", "Levililer", 27),
                            Triple("numbers", "Sayılar", 36),
                            Triple("deuteronomy", "Yasanın Tekrarı", 34)
                        )
                    }
                    val selectedTorah = torahBooks.random()
                    val chapter = (1..selectedTorah.third).random()
                    val url = "https://bible-api.com/${selectedTorah.first}%20$chapter"
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("Torah Bible API failure")
                        val body = response.body?.string() ?: ""
                        val json = JSONObject(body)
                        val versesJA = json.getJSONArray("verses")
                        if (versesJA.length() == 0) throw Exception("No verses returned")
                        val randomIdx = (0 until versesJA.length()).random()
                        val verseObj = versesJA.getJSONObject(randomIdx)
                        val englishText = verseObj.getString("text").trim()
                        val verseNum = verseObj.getInt("verse")
                        val verseText = when (lang) {
                            com.example.ui.util.AppLanguage.RU -> translateTextGtx(englishText, targetLang = "ru", sourceLang = "en")
                            com.example.ui.util.AppLanguage.EN -> englishText
                            com.example.ui.util.AppLanguage.TR -> translateTextGtx(englishText, targetLang = "tr", sourceLang = "en")
                        }
                        val refHeader = when (lang) {
                            com.example.ui.util.AppLanguage.RU -> "ТОРА (${selectedTorah.second}, Глава $chapter:$verseNum)"
                            com.example.ui.util.AppLanguage.EN -> "TORAH (${selectedTorah.second}, Chapter $chapter:$verseNum)"
                            com.example.ui.util.AppLanguage.TR -> "TEVRAT (${selectedTorah.second}, Bölüm $chapter:$verseNum)"
                        }
                        Pair(refHeader, verseText)
                    }
                }
                "sermon" -> {
                    // Random sermon chapter from Matthew (5, 6, 7)
                    val chapter = (5..7).random()
                    val url = "https://bible-api.com/matthew%20$chapter"
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("Sermon Bible API failure")
                        val body = response.body?.string() ?: ""
                        val json = JSONObject(body)
                        val versesJA = json.getJSONArray("verses")
                        if (versesJA.length() == 0) throw Exception("No verses returned")
                        val randomIdx = (0 until versesJA.length()).random()
                        val verseObj = versesJA.getJSONObject(randomIdx)
                        val englishText = verseObj.getString("text").trim()
                        val verseNum = verseObj.getInt("verse")
                        val verseText = when (lang) {
                            com.example.ui.util.AppLanguage.RU -> translateTextGtx(englishText, targetLang = "ru", sourceLang = "en")
                            com.example.ui.util.AppLanguage.EN -> englishText
                            com.example.ui.util.AppLanguage.TR -> translateTextGtx(englishText, targetLang = "tr", sourceLang = "en")
                        }
                        val refHeader = when (lang) {
                            com.example.ui.util.AppLanguage.RU -> "ЕВА scriptorium (От Матфея $chapter:$verseNum)".replace("scriptorium", "НГЕЛИЕ")
                            com.example.ui.util.AppLanguage.EN -> "GOSPEL (Matthew $chapter:$verseNum)"
                            com.example.ui.util.AppLanguage.TR -> "İNCİL (Matta $chapter:$verseNum)"
                        }
                        Pair(refHeader, verseText)
                    }
                }
                else -> fetchOfflineVerse(fallbackBook, lang)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fetchOfflineVerse(fallbackBook, lang)
        }
    }

    private fun fetchOfflineVerse(book: com.example.data.model.Book, lang: com.example.ui.util.AppLanguage): Pair<String, String> {
        val paragraphs = book.paragraphs
        if (paragraphs.isEmpty()) {
            return Pair(
                when (lang) {
                    com.example.ui.util.AppLanguage.RU -> "СВЯЩЕННОЕ ПИСАНИЕ"
                    com.example.ui.util.AppLanguage.EN -> "HOLY SCRIPTURES"
                    com.example.ui.util.AppLanguage.TR -> "KUTSAL KİTAP"
                },
                when (lang) {
                    com.example.ui.util.AppLanguage.RU -> "Текст не найден."
                    com.example.ui.util.AppLanguage.EN -> "Verse content not found."
                    com.example.ui.util.AppLanguage.TR -> "Ayet içeriği bulunamadı."
                }
            )
        }
        val randomIndex = (paragraphs.indices).random()
        val textTr = paragraphs[randomIndex]
        val text = when (lang) {
            com.example.ui.util.AppLanguage.RU -> translateTextGtx(textTr, targetLang = "ru", sourceLang = "tr")
            com.example.ui.util.AppLanguage.EN -> translateTextGtx(textTr, targetLang = "en", sourceLang = "tr")
            com.example.ui.util.AppLanguage.TR -> textTr
        }
        val bookTitle = com.example.ui.util.Loc.get(book.id, lang)
        val ref = when (lang) {
            com.example.ui.util.AppLanguage.RU -> when (book.id) {
                "quran" -> "СВЯЩЕННЫЙ КОРАН (Сура Аль-Фатх, Аят ${randomIndex + 1})"
                "torah" -> "ТОРА (Бытие, Глава 1:${randomIndex + 1})"
                "sermon" -> "ЕВА scriptorium (От Матфея 5:${randomIndex + 1})".replace("scriptorium", "НГЕЛИЕ")
                else -> "${bookTitle.uppercase()} (${randomIndex + 1})"
            }
            com.example.ui.util.AppLanguage.EN -> when (book.id) {
                "quran" -> "HOLY QURAN (Surah Al-Fath, Verse ${randomIndex + 1})"
                "torah" -> "TORAH (Genesis, Chapter 1:${randomIndex + 1})"
                "sermon" -> "GOSPEL (Matthew 5:${randomIndex + 1})"
                else -> "${bookTitle.uppercase()} (${randomIndex + 1})"
            }
            com.example.ui.util.AppLanguage.TR -> when (book.id) {
                "quran" -> "KUR'AN-I KERİM (Fetih Suresi, Ayet ${randomIndex + 1})"
                "torah" -> "TEVRAT (Yaratılış, Bölüm 1:${randomIndex + 1})"
                "sermon" -> "İNCİL (Matta 5:${randomIndex + 1})"
                else -> "${bookTitle.uppercase()} (${randomIndex + 1})"
            }
        }
        return Pair(ref, text)
    }

    private fun translateTextGtx(text: String, targetLang: String = "tr", sourceLang: String = "auto"): String {
        val okHttpClient = OkHttpClient()
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=$encodedText"
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val jsonArray = org.json.JSONArray(bodyStr)
                    val sentencesArray = jsonArray.optJSONArray(0)
                    if (sentencesArray != null) {
                        val sb = StringBuilder()
                        for (i in 0 until sentencesArray.length()) {
                            val sentence = sentencesArray.optJSONArray(i)
                            if (sentence != null) {
                                sb.append(sentence.optString(0))
                            }
                        }
                        return sb.toString().trim()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return text
    }

    private fun showNotification(context: Context, title: String, message: String, lang: com.example.ui.util.AppLanguage = com.example.ui.util.AppLanguage.TR) {
        val channelId = "hourly_verse_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                when (lang) {
                    com.example.ui.util.AppLanguage.RU -> "Аяты дня и времена молитв"
                    com.example.ui.util.AppLanguage.EN -> "Daily Verses & Prayer Times"
                    com.example.ui.util.AppLanguage.TR -> "Günün Ayetleri"
                },
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = when (lang) {
                    com.example.ui.util.AppLanguage.RU -> "Времена молитв и аяты из священных писаний."
                    com.example.ui.util.AppLanguage.EN -> "Prayer times and daily verses from sacred scriptures."
                    com.example.ui.util.AppLanguage.TR -> "Seçilen kutsal kitaplardan saatlik ayet bildirimleri."
                }
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(4242, notification)
    }

    companion object {
        fun scheduleAlarm(context: Context, force: Boolean = false) {
            val prefs = context.getSharedPreferences("scriptorium_auth", Context.MODE_PRIVATE)
            val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailyVerseReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (!notificationsEnabled) {
                alarmManager.cancel(pendingIntent)
                return
            }

            // Get interval in minutes, default is 24 hours (1440 minutes)
            val intervalMinutes = prefs.getInt("notification_interval_minutes", 1440)
            val intervalMillis = intervalMinutes * 60 * 1000L

            val lastTriggerTime = prefs.getLong("last_trigger_time", 0L)
            val currentTime = System.currentTimeMillis()

            // Calculate when the next alarm should trigger
            var triggerAtMillis = lastTriggerTime + intervalMillis
            if (force) {
                triggerAtMillis = currentTime + 3000L // Trigger in 3 seconds to show immediate confirmation
                prefs.edit().putLong("last_trigger_time", currentTime).apply()
            } else if (triggerAtMillis <= currentTime) {
                triggerAtMillis = currentTime + intervalMillis
                prefs.edit().putLong("last_trigger_time", currentTime).apply()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }
}
