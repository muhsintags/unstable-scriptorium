package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SacredGold
import com.example.ui.util.AppLanguage
import com.example.ui.util.bounceClick
import com.example.ui.viewmodel.ScriptureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ScriptureViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val readerSettings by viewModel.readerSettings.collectAsState()
    val lang = readerSettings.language

    var nameInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    fun performLogin(name: String) {
        val defaultName = when (lang) {
            AppLanguage.RU -> "Искатель мудрости"
            AppLanguage.EN -> "Wisdom Pilgrim"
            AppLanguage.TR -> "Bilgelik Yolcusu"
        }
        val finalName = name.trim().ifEmpty { defaultName }
        viewModel.signInWithDemo("misafir@Scriptorium.org", finalName)
        val toastText = when (lang) {
            AppLanguage.RU -> "Добро пожаловать, $finalName!"
            AppLanguage.EN -> "Welcome, $finalName!"
            AppLanguage.TR -> "Hoş geldiniz, $finalName!"
        }
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Language Toggle Switcher Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = true,
                    onClick = {
                        val newLang = when (lang) {
                            AppLanguage.TR -> AppLanguage.EN
                            AppLanguage.EN -> AppLanguage.RU
                            AppLanguage.RU -> AppLanguage.TR
                        }
                        viewModel.updateLanguage(newLang)
                    },
                    label = {
                        Text(
                            text = when (lang) {
                                AppLanguage.RU -> "🌐 Русский"
                                AppLanguage.EN -> "🌐 English"
                                AppLanguage.TR -> "🌐 Türkçe"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SacredGold,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // 1. App Header & Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, SacredGold.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = "Scriptorium Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Text(
                    text = when (lang) {
                        AppLanguage.RU -> "Священные Писания"
                        AppLanguage.EN -> "Sacred Scriptures"
                        AppLanguage.TR -> "Kutsal Metinler"
                    },
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when (lang) {
                        AppLanguage.RU -> "Добро пожаловать в библиотеку древней мудрости человечества."
                        AppLanguage.EN -> "Welcome to humanity's ancient library of wisdom."
                        AppLanguage.TR -> "İnsanlığın kadim bilgelik kütüphanesine hoş geldiniz."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 2. Offline Name Card Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = when (lang) {
                                AppLanguage.RU -> "Создать профиль"
                                AppLanguage.EN -> "Create Profile"
                                AppLanguage.TR -> "Profil Oluşturun"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (lang) {
                                AppLanguage.RU -> "Введите ваше имя, чтобы персонализировать чтение."
                                AppLanguage.EN -> "Enter your name to personalize your reading experience."
                                AppLanguage.TR -> "Okuma deneyiminizi kişiselleştirmek için isminizi belirleyin."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            if (showError && it.isNotBlank()) showError = false
                        },
                        label = {
                            Text(
                                when (lang) {
                                    AppLanguage.RU -> "Имя и фамилия"
                                    AppLanguage.EN -> "Full Name"
                                    AppLanguage.TR -> "Ad Soyad"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (lang) {
                                    AppLanguage.RU -> "Например: Иван Иванов"
                                    AppLanguage.EN -> "e.g., John Doe"
                                    AppLanguage.TR -> "Örn: Ahmet Yılmaz"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        isError = showError,
                        supportingText = if (showError) {
                            {
                                Text(
                                    when (lang) {
                                        AppLanguage.RU -> "Пожалуйста, введите ваше имя."
                                        AppLanguage.EN -> "Please enter your name."
                                        AppLanguage.TR -> "Lütfen adınızı ve soyadınızı girin."
                                    }
                                )
                            }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guest_name_input"),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (nameInput.isBlank()) {
                                    showError = true
                                } else {
                                    performLogin(nameInput)
                                }
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (nameInput.isBlank()) {
                                showError = true
                            } else {
                                performLogin(nameInput)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_name_login_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.RU -> "Войти и начать"
                                    AppLanguage.EN -> "Sign In & Begin"
                                    AppLanguage.TR -> "Giriş Yap ve Başla"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Anonymous quick entry option
                    TextButton(
                        onClick = {
                            val guestName = when (lang) {
                                AppLanguage.RU -> "Искатель мудрости"
                                AppLanguage.EN -> "Wisdom Pilgrim"
                                AppLanguage.TR -> "Bilgelik Yolcusu"
                            }
                            performLogin(guestName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_guest_login_button"),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = when (lang) {
                                AppLanguage.RU -> "Продолжить как гость"
                                AppLanguage.EN -> "Continue as Guest"
                                AppLanguage.TR -> "Misafir Olarak Devam Et"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Footer info badge
            Surface(
                color = SacredGold.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SacredGold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoStories,
                        contentDescription = null,
                        tint = SacredGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.RU -> "Библиотека Священных Писаний"
                            AppLanguage.EN -> "Sacred Scriptures Library"
                            AppLanguage.TR -> "Kutsal Metinler Kitaplığı"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
