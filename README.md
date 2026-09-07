# Scriptorium

> Library of humanity

**A personal digital library for sacred and classical texts — Torah, Bible, Quran, Sahih al-Bukhari, Talmud, Bhagavad Gita, and translation, all in one place.**

[![Build Status](https://github.com/muhsintags/unstable-scriptorium/actions/workflows/build.yml/badge.svg)](https://github.com/muhsintags/unstable-scriptorium/actions) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE) [![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin)](https://kotlinlang.org) [![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?logo=android)](https://developer.android.com)

[Download](#download) ▸ [Features](#features) ▸ [Languages](#languages) ▸ [Tech Stack](#tech-stack) ▸ [Build](#build--run) ▸ [Privacy](#privacy) ▸ [Contributing](#contributing)

---

## About
#about

**Scriptorium** is a native Android app that brings sacred and classical texts from multiple traditions together in a clean, offline-friendly library. It is built for focused reading, comparative study, personal notes, and multilingual access without the clutter.

## Features
#features

- ▸ **Multi-tradition library** — Torah, Bible/Gospel, Quran, Sahih al-Bukhari, Talmud, and Bhagavad Gita
- ▸ **Comparative Reading Mode** — compare two or three sources with parallel cards, split columns, chapter selection, filtering, and source swapping
- ▸ **Three-language interface** — Turkish, English, and Russian across navigation, reader controls, dialogs, notifications, and catalogue metadata
- ▸ **Language-aware scripture data** — Turkish, English, and Russian book/surah names, Russian Quran edition support, and language-aware offline cache keys
- ▸ **Offline-first reading** — download complete books or individual chapters; content is cached locally with Room and file storage
- ▸ **Reading tools** — adjustable font size, serif/sans-serif typography, line height, light/dark/sepia themes, original text, bilingual mode, and audio playback where available
- ▸ **Personal study** — notes, highlights, bookmarks, reading history, progress tracking, and contemplation timer
- ▸ **Daily verse notifications** — scheduled wisdom notifications with the selected application language
- ▸ **Live content integration** — Quran, Bible, Torah, Talmud, Bukhari, and Gita content can be loaded from their configured sources and retained offline
- ▸ **Automatic updates** — optional GitHub-based update checks with localized update dialogs
- ▸ **Modern Android UI** — Jetpack Compose, Material 3, animated navigation, responsive reader layouts, and a custom Scriptorium launcher logo

## Languages

The application supports:

| Code | Interface | Content behavior |
| --- | --- | --- |
| `TR` | Türkçe | Turkish interface and Turkish translations where available |
| `EN` | English | English interface and English translations where available |
| `RU` | Русский | Russian interface, Russian catalogue names, and Russian Quran translation |

Language selection is persisted across launches. Existing values such as `TR`, `EN`, `RU`, `tr-TR`, and `ru-RU` are accepted. On a first launch, Turkish and Russian device locales select their matching language; other locales default to English. Downloaded scripture content is cached separately per language so changing language does not reuse the wrong translation.

## Download
#download

Every push to `main` triggers an automatic build. You can grab an APK two ways:

| Option | What you get | Where |
| --- | --- | --- |
| **Latest Release** | Stable, signed release APK | [Releases page](https://github.com/muhsintags/unstable-scriptorium/releases/latest) |
| **Dev Build** | Freshest debug build (may be unstable) | [Actions tab](https://github.com/muhsintags/unstable-scriptorium/actions) → latest run → Artifacts |

Also available on [APKPure](https://apkpure.com/p/com.muhsintags.scriptorium) when a release is published.

## Tech Stack
#tech-stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM |
| Local storage | Room |
| Networking | Retrofit + OkHttp |
| Async | Kotlin Coroutines |
| CI/CD | GitHub Actions |
| Dev environment | GitHub Codespaces |

The application ID is `com.muhsintags.scriptorium`, with `minSdk 24`, `targetSdk 36`, and version `1.0` / version code `1` in the current configuration.

> This project is built entirely in the cloud — no local Android Studio setup required. Every build, test, and release runs through GitHub Actions and Codespaces.

## Build & Run
#build--run

**Locally / in Codespaces:**

```
./gradlew assembleDebug      # debug build
./gradlew assembleRelease    # signed release build (requires keystore secrets)
```

The release build expects `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`. Do not commit keystores or encoded signing keys. Use `.env.example` for local configuration and GitHub Actions secrets for CI.

**Via GitHub Actions:** Push to `main` or start the workflow manually from the [Actions tab](https://github.com/muhsintags/unstable-scriptorium/actions). The workflow generates a debug keystore, configures Gradle, builds both variants, and publishes `app-debug` and `app-release` artifacts.

## Project Structure
#project-structure

```
unstable-scriptorium/
├── app/                  # Main application module
├── gradle/               # Gradle wrapper & version catalog
├── app/src/main/java/    # Compose UI, ViewModel, repositories, API and data models
├── app/src/main/res/     # Android resources and launcher logo
├── app/src/test/         # Unit, Robolectric and screenshot tests
├── .github/workflows/    # CI/CD build pipeline
└── index.html            # Localized privacy policy page
```

## Current Status
#current-status

- Native Android application is active on the `main` branch.
- Turkish, English, and Russian application language flows are implemented.
- Debug and release APKs are built automatically in GitHub Actions.
- Release signing material remains outside the repository.

## Roadmap
#roadmap

- [x] Publish to APKPure
- [x] Firebase dependency cleanup
- [x] Comparative reading mode
- [x] Turkish, English, and Russian localization
- [x] Language-aware offline caching
- [x] Custom Scriptorium application logo
- [ ] Google Play release (pending developer account)
- [ ] Turkish commentary layer for texts
- [ ] Expand the catalogue with additional classical and sacred texts

## Privacy

Scriptorium is designed to keep personal study data on the device. Notes, highlights, reading history, settings, and downloaded content are stored locally. The app does not include analytics, advertising, or tracking services. Network access is used for configured scripture sources, translation/content services, update checks, and optional location-based prayer times.

The full privacy policy is available in [index.html](index.html) and includes English, Turkish, Russian, Arabic, Hindi, Hebrew, and German.

## Contributing
#contributing

This is currently a solo personal project, but bug reports, suggestions, and feedback are always welcome — open an [Issue](https://github.com/muhsintags/unstable-scriptorium/issues) any time.

Feel free to fork the repo and build your own version too. Some ideas to get you started:

- ▸ Build a **philosophy library** — Stoic, Confucian, or other classical texts instead of religious ones
- ▸ **Translate the app into your own language** before official support arrives — a native speaker's translation is often more accurate than a machine one (e.g. Japanese, before it's officially supported)
- ▸ Add a **new sacred or classical text** not currently included
- ▸ Extend the **comparative reading mode** with more viewing options or additional traditions

## Built With AI Assistance
#built-with-ai-assistance

This project was developed with the help of AI tools:

- **Claude** (Anthropic)
- **ChatGPT** (OpenAI)
- **Google AI Studio**
- **Gemini**

## License
#license

Licensed under the [MIT License](LICENSE).

---

Made with a lot of coffee and GitHub Actions minutes.
