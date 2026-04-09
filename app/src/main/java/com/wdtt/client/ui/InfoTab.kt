package com.wdtt.client.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.wdtt.client.R
import com.wdtt.client.WDTTColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

private val BROWSER_PACKAGES = listOf(
    "com.android.chrome",
    "com.google.android.googlequicksearchbox",
    "org.mozilla.firefox",
    "com.yandex.browser",
    "ru.yandex.searchplugin",
    "com.yandex.browser.lite",
    "com.opera.browser",
    "com.opera.mini.native",
    "com.microsoft.emmx",
    "com.brave.browser",
    "com.duckduckgo.mobile.android",
    "com.sec.android.app.sbrowser",
    "com.vivaldi.browser",
    "com.kiwibrowser.browser",
)

private fun openUrlInBrowser(context: Context, url: String) {
    try {
        val pm = context.packageManager
        val uri = Uri.parse(url)

        for (pkg in BROWSER_PACKAGES) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage(pkg)
                if (intent.resolveActivity(pm) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (_: Exception) {}
        }
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(pm) != null) {
            context.startActivity(intent)
        }
    } catch (_: Exception) {}
}

@Composable
fun InfoTab() {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val currentVersion = "v1.0.6"
    var updateStatus by remember { mutableStateOf("Проверить обновление") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Дополнительная информация",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        // ═══ Версия и Обновление ═══
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Установлена версия $currentVersion",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (updateStatus == "Проверка...") return@Button
                        updateStatus = "Проверка..."
                        scope.launch {
                            try {
                                val result = withContext(Dispatchers.IO) {
                                    val url = URL("https://api.github.com/repos/amurcanov/proxy-turn-vk-android/releases/latest")
                                    val connection = url.openConnection() as HttpURLConnection
                                    connection.requestMethod = "GET"
                                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                                    connection.connectTimeout = 5000
                                    connection.readTimeout = 5000

                                    if (connection.responseCode == 200) {
                                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                                        val matcher = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").matcher(response)
                                        if (matcher.find()) matcher.group(1) else null
                                    } else null
                                }

                                if (result != null) {
                                    val latestClean = result.removePrefix("v").trim()
                                    val currentClean = currentVersion.removePrefix("v").trim()
                                    updateStatus = if (latestClean == currentClean) {
                                        "Последняя версия"
                                    } else {
                                        "Вышла версия $result"
                                    }
                                } else {
                                    updateStatus = "Ошибка проверки"
                                }
                            } catch (e: Exception) {
                                updateStatus = "Ошибка сети"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = updateStatus,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // ═══ GitHub ═══
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GitHubSection(
                    title = "Актуальные релизы",
                    buttonText = "WDTT Релизы",
                    url = "https://github.com/amurcanov/proxy-turn-vk-android/releases"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                GitHubSection(
                    title = "Страница разработчика",
                    buttonText = "GitHub Amurcanov",
                    url = "https://github.com/amurcanov"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                GitHubSection(
                    title = "Если возникли проблемы",
                    buttonText = "Поднять вопрос",
                    url = "https://github.com/amurcanov/proxy-turn-vk-android/issues/new"
                )
            }
        }
    }
}

@Composable
private fun GitHubSection(
    title: String,
    buttonText: String,
    url: String
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Button(
            onClick = { openUrlInBrowser(context, url) },
            modifier = Modifier.fillMaxWidth(0.9f).height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) WDTTColors.githubDark else WDTTColors.github)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_github),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = Color.White
                )
            }
        }
    }
}


@Composable
private fun InfoCard(
    title: String,
    buttonText: String,
    iconPainter: Painter,
    url: String,
    buttonColor: Color,
    textColor: Color = Color.White
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Button(
                onClick = { openUrlInBrowser(context, url) },
                modifier = Modifier.fillMaxWidth(0.9f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = if (buttonText.isEmpty()) Modifier.height(28.dp).width(128.dp) else Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                    if (buttonText.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
