package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GeneratorWebView

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoGeneratorScreen() {
    val context = LocalContext.current

    // Web-based AI Video Engines that support real MP4 video generation & photo uploads
    var selectedEngineKey by remember { mutableStateOf("wan21_fast") }

    val currentUrl = when (selectedEngineKey) {
        "wan21_fast" -> "https://huggingface.co/spaces/multimodalart/Wan2.1-Fast"
        "kling_ai" -> "https://klingai.com"
        "perchance_video" -> "https://perchance.org/ai-video-generator"
        "wan21_studio" -> "https://huggingface.co/spaces/Wan-AI/Wan2.1"
        "easemate_ai" -> "https://www.easemate.ai/image-to-video-ai.html"
        else -> "https://perchance.org/ai-video-generator"
    }

    var webViewInstance by remember { mutableStateOf<android.webkit.WebView?>(null) }
    var loadingProgress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Engine Selector Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pilih Engine Studio Video",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Mendukung Upload Foto Galeri & Download Video MP4",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            webViewInstance?.reload()
                            Toast.makeText(context, "Memuat ulang studio video...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Studio",
                            tint = Color(0xFFFF416C)
                        )
                    }
                }

                // Chips for Engine Selection
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedEngineKey == "wan21_fast",
                        onClick = { selectedEngineKey = "wan21_fast" },
                        label = { Text("Wan 2.1 Fast", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF416C),
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedEngineKey == "kling_ai",
                        onClick = { selectedEngineKey = "kling_ai" },
                        label = { Text("Kling AI (Official)", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE11D48),
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedEngineKey == "perchance_video",
                        onClick = { selectedEngineKey = "perchance_video" },
                        label = { Text("Perchance Video (Uncensored)", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF8E2DE2),
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedEngineKey == "wan21_studio",
                        onClick = { selectedEngineKey = "wan21_studio" },
                        label = { Text("Wan 2.1 Official", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedEngineKey == "easemate_ai",
                        onClick = { selectedEngineKey = "easemate_ai" },
                        label = { Text("EaseMate Video AI", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF3B82F6),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Progress Bar
        if (isLoading && loadingProgress < 100) {
            LinearProgressIndicator(
                progress = { loadingProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Color(0xFFFF416C),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Interactive Web Studio Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            GeneratorWebView(
                url = currentUrl,
                webViewRef = { webViewInstance = it },
                onProgressChanged = { progress ->
                    loadingProgress = progress
                    isLoading = progress < 100
                },
                onPageStarted = {
                    isLoading = true
                },
                onPageFinished = {
                    isLoading = false
                },
                onError = {
                    isLoading = false
                }
            )
        }
    }
}
