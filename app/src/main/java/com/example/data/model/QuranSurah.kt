package com.example.data.model

import com.example.ui.util.AppLanguage

data class QuranSurah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameTurkish: String,
    val ayahCount: Int,
    val revelationType: String
) {
    val nameRussian: String
        get() = russianNames[number] ?: nameEnglish

    fun getName(lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.RU -> nameRussian
            AppLanguage.EN -> nameEnglish
            AppLanguage.TR -> nameTurkish
        }
    }

    companion object {
        val russianNames = mapOf(
            1 to "Аль-Фатиха", 2 to "Аль-Бакара", 3 to "Али Имран", 4 to "Ан-Ниса",
            5 to "Аль-Маида", 6 to "Аль-Ан'ам", 7 to "Аль-А'раф", 8 to "Аль-Анфаль",
            9 to "Ат-Тауба", 10 to "Юнус", 11 to "Худ", 12 to "Юсуф",
            13 to "Ар-Ра'д", 14 to "Ибрахим", 15 to "Аль-Хиджр", 16 to "Ан-Нахль",
            17 to "Аль-Исра", 18 to "Аль-Кахф", 19 to "Марьям", 20 to "Та Ха",
            21 to "Аль-Анбийа", 22 to "Аль-Хадж", 23 to "Аль-Му'минун", 24 to "Ан-Нур",
            25 to "Аль-Фуркан", 26 to "Аш-Шу'ара", 27 to "Ан-Намль", 28 to "Аль-Касас",
            29 to "Аль-Анкабут", 30 to "Ар-Рум", 31 to "Лукман", 32 to "Ас-Саджда",
            33 to "Аль-Ахзаб", 34 to "Саба", 35 to "Фатыр", 36 to "Йа Син",
            37 to "Ас-Саффат", 38 to "Сад", 39 to "Аз-Зумар", 40 to "Гафир",
            41 to "Фуссылят", 42 to "Аш-Шура", 43 to "Аз-Зухруф", 44 to "Ад-Духан",
            45 to "Аль-Джасийа", 46 to "Аль-Ахкаф", 47 to "Мухаммад", 48 to "Аль-Фатх",
            49 to "Аль-Худжурат", 50 to "Каф", 51 to "Аз-Зарият", 52 to "Ат-Тур",
            53 to "Ан-Наджм", 54 to "Аль-Камар", 55 to "Ар-Рахман", 56 to "Аль-Ваки'а",
            57 to "Аль-Хадид", 58 to "Аль-Муджадиля", 59 to "Аль-Хашр", 60 to "Аль-Мумтахана",
            61 to "Ас-Сафф", 62 to "Аль-Джуму'а", 63 to "Аль-Мунафикун", 64 to "Ат-Тагабун",
            65 to "Ат-Талак", 66 to "Ат-Тахрим", 67 to "Аль-Мульк", 68 to "Аль-Калям",
            69 to "Аль-Хакка", 70 to "Аль-Ма'аридж", 71 to "Нух", 72 to "Аль-Джинн",
            73 to "Аль-Муззаммиль", 74 to "Аль-Муддассир", 75 to "Аль-Кийама", 76 to "Аль-Инсан",
            77 to "Аль-Мурсалят", 78 to "Ан-Наба", 79 to "Ан-Нази'ат", 80 to "Абаса",
            81 to "Ат-Таквир", 82 to "Аль-Инфитар", 83 to "Аль-Мутаффифин", 84 to "Аль-Иншикак",
            85 to "Аль-Бурудж", 86 to "Ат-Тарик", 87 to "Аль-А'ля", 88 to "Аль-Гашийа",
            89 to "Аль-Фаджр", 90 to "Аль-Баляд", 91 to "Аш-Шамс", 92 to "Аль-Лейль",
            93 to "Ад-Духа", 94 to "Аш-Шарх", 95 to "Ат-Тин", 96 to "Аль-Аляк",
            97 to "Аль-Кадр", 98 to "Аль-Баййина", 99 to "Аз-Зальзаля", 100 to "Аль-Адият",
            101 to "Аль-Кари'а", 102 to "Ат-Такасур", 103 to "Аль-Аср", 104 to "Аль-Хумаза",
            105 to "Аль-Филь", 106 to "Курайш", 107 to "Аль-Ма'ун", 108 to "Аль-Каусар",
            109 to "Аль-Кафирун", 110 to "Ан-Наср", 111 to "Аль-Масад", 112 to "Аль-Ихлас",
            113 to "Аль-Фаляк", 114 to "Ан-Нас"
        )
    }
}

data class QuranVerse(
    val number: Int,
    val textArabic: String,
    val textTurkish: String,
    val audioUrl: String
)

data class QuranSurahContent(
    val number: Int,
    val nameArabic: String,
    val englishName: String,
    val verses: List<QuranVerse>
)

object QuranRepository {
    val surahs = listOf(
        QuranSurah(1, "الفاتحة", "Al-Fatihah", "Fâtiha", 7, "Meccan"),
        QuranSurah(2, "البقرة", "Al-Baqarah", "Bakara", 286, "Medinan"),
        QuranSurah(3, "آل عمران", "Ali 'Imran", "Âl-i İmrân", 200, "Medinan"),
        QuranSurah(4, "النساء", "An-Nisa", "Nisâ", 176, "Medinan"),
        QuranSurah(5, "المائدة", "Al-Ma'idah", "Mâide", 120, "Medinan"),
        QuranSurah(6, "الأنعام", "Al-An'am", "En'âm", 165, "Meccan"),
        QuranSurah(7, "الأعراف", "Al-A'raf", "A'râf", 206, "Meccan"),
        QuranSurah(8, "الأنفال", "Al-Anfal", "Enfâl", 75, "Medinan"),
        QuranSurah(9, "التوبة", "At-Tawbah", "Tevbe", 129, "Medinan"),
        QuranSurah(10, "يونس", "Yunus", "Yûnus", 109, "Meccan"),
        QuranSurah(11, "هود", "Hud", "Hûd", 123, "Meccan"),
        QuranSurah(12, "يوسف", "Yusuf", "Yûsuf", 111, "Meccan"),
        QuranSurah(13, "الرعد", "Ar-Ra'd", "Ra'd", 43, "Medinan"),
        QuranSurah(14, "إبراهيم", "Ibrahim", "İbrâhîm", 52, "Meccan"),
        QuranSurah(15, "الحجر", "Al-Hijr", "Hicr", 99, "Meccan"),
        QuranSurah(16, "النحل", "An-Nahl", "Nahl", 128, "Meccan"),
        QuranSurah(17, "الإسراء", "Al-Isra", "İsrâ", 111, "Meccan"),
        QuranSurah(18, "الكهف", "Al-Kahf", "Kehf", 110, "Meccan"),
        QuranSurah(19, "مريم", "Maryam", "Meryem", 98, "Meccan"),
        QuranSurah(20, "طه", "Taha", "Tâhâ", 135, "Meccan"),
        QuranSurah(21, "الأنبياء", "Al-Anbiya", "Enbiyâ", 112, "Meccan"),
        QuranSurah(22, "الحج", "Al-Jajj", "Hac", 78, "Medinan"),
        QuranSurah(23, "المؤمنون", "Al-Mu'minun", "Mü'minûn", 118, "Meccan"),
        QuranSurah(24, "النور", "An-Nur", "Nûr", 64, "Medinan"),
        QuranSurah(25, "الفرقان", "Al-Furqan", "Furkân", 77, "Meccan"),
        QuranSurah(26, "الشعراء", "An-Shu'ara", "Şuarâ", 227, "Meccan"),
        QuranSurah(27, "النمل", "An-Naml", "Neml", 93, "Meccan"),
        QuranSurah(28, "القصص", "Al-Qasas", "Kasas", 88, "Meccan"),
        QuranSurah(29, "العنكبوت", "Al-'Ankabut", "Ankebût", 69, "Meccan"),
        QuranSurah(30, "الروم", "Ar-Rum", "Rûm", 60, "Meccan"),
        QuranSurah(31, "لقمان", "Luqman", "Lokmân", 34, "Meccan"),
        QuranSurah(32, "السجدة", "As-Sajdah", "Secde", 30, "Meccan"),
        QuranSurah(33, "الأحزاب", "Al-Ahzab", "Ahzâb", 73, "Medinan"),
        QuranSurah(34, "سبأ", "Saba", "Sebe'", 54, "Meccan"),
        QuranSurah(35, "فاطر", "Fatir", "Fâtır", 45, "Meccan"),
        QuranSurah(36, "يس", "Ya-Sin", "Yâsîn", 83, "Meccan"),
        QuranSurah(37, "الصافات", "As-Saffat", "Sâffât", 182, "Meccan"),
        QuranSurah(38, "ص", "Sad", "Sâd", 88, "Meccan"),
        QuranSurah(39, "الزمر", "Az-Zumar", "Zümer", 75, "Meccan"),
        QuranSurah(40, "غافر", "Ghafir", "Mü'min (Gâfir)", 85, "Meccan"),
        QuranSurah(41, "فصلت", "Fussilat", "Fussilet", 54, "Meccan"),
        QuranSurah(42, "الشورى", "Ash-Shura", "Şûrâ", 53, "Meccan"),
        QuranSurah(43, "الزخرف", "Az-Zukhruf", "Zuhruf", 89, "Meccan"),
        QuranSurah(44, "الدخان", "Ad-Dukhan", "Duhân", 59, "Meccan"),
        QuranSurah(45, "الجاثية", "Al-Jathiyah", "Câsiye", 37, "Meccan"),
        QuranSurah(46, "الأحقاف", "Al-Ahqaf", "Ahkâf", 35, "Meccan"),
        QuranSurah(47, "محمد", "Muhammad", "Muhammed", 38, "Medinan"),
        QuranSurah(48, "الفتح", "Al-Fath", "Fetih", 29, "Medinan"),
        QuranSurah(49, "الحجرات", "Al-Hujurat", "Hucurât", 18, "Medinan"),
        QuranSurah(50, "ق", "Qaf", "Kâf", 45, "Meccan"),
        QuranSurah(51, "الذاريات", "Adh-Dhariyat", "Zâriyât", 60, "Meccan"),
        QuranSurah(52, "الطور", "At-Tur", "Tûr", 49, "Meccan"),
        QuranSurah(53, "النجم", "An-Necm", "Necm", 62, "Meccan"),
        QuranSurah(54, "المر", "Al-Qamar", "Kamer", 55, "Meccan"),
        QuranSurah(55, "الرحمن", "Ar-Rahman", "Rahmân", 78, "Medinan"),
        QuranSurah(56, "الواقعة", "Al-Waqi'ah", "Vâkıa", 96, "Meccan"),
        QuranSurah(57, "الحديد", "Al-Hadid", "Hadîd", 29, "Medinan"),
        QuranSurah(58, "المجادلة", "Al-Mujadilah", "Mücâdele", 22, "Medinan"),
        QuranSurah(59, "الحشر", "Al-Hashr", "Haşr", 24, "Medinan"),
        QuranSurah(60, "الممتحنة", "Al-Mumtahinah", "Mümtehine", 13, "Medinan"),
        QuranSurah(61, "الصف", "As-Saff", "Saff", 14, "Medinan"),
        QuranSurah(62, "الجمعة", "Al-Jumu'ah", "Cuma", 11, "Medinan"),
        QuranSurah(63, "المنافقون", "Al-Munafiqun", "Münâfıkûn", 11, "Medinan"),
        QuranSurah(64, "التغابن", "At-Taghabun", "Tegâbun", 18, "Medinan"),
        QuranSurah(65, "الطلاق", "At-Talaq", "Talâk", 12, "Medinan"),
        QuranSurah(66, "التحريم", "At-Tahrim", "Tahrîm", 12, "Medinan"),
        QuranSurah(67, "الملك", "Al-Mulk", "Mülk", 30, "Meccan"),
        QuranSurah(68, "اللقلم", "Al-Qalam", "Kalem", 52, "Meccan"),
        QuranSurah(69, "الحاقة", "Al-Haqqah", "Hâkka", 52, "Meccan"),
        QuranSurah(70, "المعارج", "Al-Ma'arij", "Meâric", 44, "Meccan"),
        QuranSurah(71, "نوح", "Nuh", "Nûh", 28, "Meccan"),
        QuranSurah(72, "الجن", "Al-Jinn", "Cin", 28, "Meccan"),
        QuranSurah(73, "المزمل", "Al-Muzzammil", "Müzzemmil", 20, "Meccan"),
        QuranSurah(74, "المدثر", "Al-Muddaththir", "Müddessir", 56, "Meccan"),
        QuranSurah(75, "القيامة", "Al-Qiyamah", "Kıyâmet", 40, "Meccan"),
        QuranSurah(76, "الانسان", "Al-Insan", "İnsân", 31, "Medinan"),
        QuranSurah(77, "المرسلات", "Al-Mursalat", "Mürselât", 50, "Meccan"),
        QuranSurah(78, "النبأ", "An-Naba'", "Nebe'", 40, "Meccan"),
        QuranSurah(79, "النازعات", "An-Nazi'at", "Nâziât", 46, "Meccan"),
        QuranSurah(80, "عبس", "Abasa", "Abese", 42, "Meccan"),
        QuranSurah(81, "التكوير", "At-Takwir", "Tekvîr", 29, "Meccan"),
        QuranSurah(82, "الانفطار", "Al-Infitar", "İnfitâr", 19, "Meccan"),
        QuranSurah(83, "المطففين", "Al-Mutaffifin", "Mutaffifîn", 36, "Meccan"),
        QuranSurah(84, "الانشقاق", "Al-Inshiqaq", "İnşikâk", 25, "Meccan"),
        QuranSurah(85, "البروج", "Al-Buruj", "Bürûc", 22, "Meccan"),
        QuranSurah(86, "الطارق", "At-Tariq", "Târık", 17, "Meccan"),
        QuranSurah(87, "الأعلى", "Al-A'la", "A'lâ", 19, "Meccan"),
        QuranSurah(88, "الغاشية", "Al-Ghashiyah", "Gâşiye", 26, "Meccan"),
        QuranSurah(89, "الفجر", "Al-Fajr", "Fecr", 30, "Meccan"),
        QuranSurah(90, "البلد", "Al-Balad", "Beled", 20, "Meccan"),
        QuranSurah(91, "الشمس", "Ash-Shams", "Şems", 15, "Meccan"),
        QuranSurah(92, "الليل", "Al-Layl", "Leyl", 21, "Meccan"),
        QuranSurah(93, "الضحى", "Ad-Duha", "Duha", 11, "Meccan"),
        QuranSurah(94, "الشرح", "Ash-Sharh", "İnşirâh", 8, "Meccan"),
        QuranSurah(95, "التين", "At-Tin", "Tîn", 8, "Meccan"),
        QuranSurah(96, "العلق", "Al-'Alaq", "Alak", 19, "Meccan"),
        QuranSurah(97, "القدر", "Al-Qadr", "Kadir", 5, "Meccan"),
        QuranSurah(98, "البينة", "Al-Bayyinah", "Beyyine", 8, "Medinan"),
        QuranSurah(99, "الزلزلة", "Az-Zalzalah", "Zilzâl", 8, "Medinan"),
        QuranSurah(100, "العاديات", "Al-'Adiyat", "Âdiyât", 11, "Meccan"),
        QuranSurah(101, "القارعة", "Al-Qari'ah", "Kâria", 11, "Meccan"),
        QuranSurah(102, "التكاثر", "At-Takathur", "Tekâsür", 8, "Meccan"),
        QuranSurah(103, "العصر", "Al-'Asr", "Asr", 3, "Meccan"),
        QuranSurah(104, "الهمزة", "Al-Humazah", "Hümeze", 9, "Meccan"),
        QuranSurah(105, "الفيل", "Al-Fil", "Fîl", 5, "Meccan"),
        QuranSurah(106, "قريش", "Quraysh", "Kureyş", 4, "Meccan"),
        QuranSurah(107, "الماعون", "Al-Ma'un", "Mâûn", 7, "Meccan"),
        QuranSurah(108, "الكوثر", "Al-Kawthar", "Kevser", 3, "Meccan"),
        QuranSurah(109, "الكافرون", "Al-Kafirun", "Kâfirûn", 6, "Meccan"),
        QuranSurah(110, "النصر", "An-Nasr", "Nasr", 3, "Medinan"),
        QuranSurah(111, "المسد", "Al-Masad", "Tebbet (Mesed)", 5, "Meccan"),
        QuranSurah(112, "الإخلاص", "Al-Ikhlas", "İhlâs", 4, "Meccan"),
        QuranSurah(113, "الفلق", "Al-Falaq", "Felak", 5, "Meccan"),
        QuranSurah(114, "الناس", "An-Nas", "Nâs", 6, "Meccan")
    )
}
