package com.example.ui.screens

import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val targetUrl = "https://perchance.org/papaigeneratorv2"

    var selectedTab by remember { mutableStateOf("image") } // "image" or "video"

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var loadingProgress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // Handle system back button for WebView navigation when in image tab
    BackHandler(enabled = selectedTab == "image" && webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = if (selectedTab == "image") {
                                                listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                                            } else {
                                                listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
                                            }
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == "image") Icons.Default.AutoAwesome else Icons.Default.Movie,
                                    contentDescription = "AI Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (selectedTab == "image") "PapAI Image Studio" else "PapAI Video Studio",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (selectedTab == "image") "AI Image Generator & Image Edit" else "Wan 2.2 / Wan 2.1 Video Engine",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Aplikasi terkunci: Header Perchance telah disembunyikan & elemen dikunci.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security Status",
                                tint = Color(0xFF10B981)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == "image",
                    onClick = { selectedTab = "image" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Gambar AI"
                        )
                    },
                    label = { Text("Gambar AI") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF8E2DE2),
                        selectedTextColor = Color(0xFF8E2DE2),
                        indicatorColor = Color(0xFF8E2DE2).copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == "video",
                    onClick = { selectedTab = "video" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Video AI Wan2.2"
                        )
                    },
                    label = { Text("Video AI (Wan2.2)") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF416C),
                        selectedTextColor = Color(0xFFFF416C),
                        indicatorColor = Color(0xFFFF416C).copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTab == "image") {
                ImageGeneratorScreen()
            } else {
                VideoGeneratorScreen()
            }
        }
    }
}

