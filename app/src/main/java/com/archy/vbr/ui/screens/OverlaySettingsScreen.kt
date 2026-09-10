package com.archy.vbr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.archy.vbr.data.PreferencesRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaySettingsScreen(
    preferencesRepository: PreferencesRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val hapticEnabled by preferencesRepository.hapticEnabled.collectAsState(initial = true)
    val hideFromScreenshots by preferencesRepository.hideFromScreenshots.collectAsState(initial = true)
    val positionLeft by preferencesRepository.overlayPositionLeft.collectAsState(initial = false)
    val overlayHeight by preferencesRepository.overlayHeight.collectAsState(initial = 500)
    val overlayWidth by preferencesRepository.overlayWidth.collectAsState(initial = 48)
    val overlayAlpha by preferencesRepository.overlayAlpha.collectAsState(initial = 0.6f)
    val overlayOffsetY by preferencesRepository.overlayOffsetY.collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overlay Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Side Position
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bar Screen Edge", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (positionLeft) "Left Edge" else "Right Edge (Adjacent to Volume Keys)")
                        Switch(
                            checked = positionLeft,
                            onCheckedChange = { isLeft ->
                                coroutineScope.launch {
                                    preferencesRepository.setOverlayPositionLeft(isLeft)
                                }
                            }
                        )
                    }
                }
            }

            // Hide from Screenshots
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hide from Screenshots", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Exclude the edge bar from system screenshots and screen recordings",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = hideFromScreenshots,
                            onCheckedChange = { hide ->
                                coroutineScope.launch {
                                    preferencesRepository.setHideFromScreenshots(hide)
                                }
                            }
                        )
                    }
                }
            }

            // Haptic Feedback
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Haptic Feedback Tick", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Vibrate slightly when adjusting volume",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    preferencesRepository.setHapticEnabled(enabled)
                                }
                            }
                        )
                    }
                }
            }

            // Height Slider
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bar Height: ${overlayHeight}px", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = overlayHeight.toFloat(),
                        onValueChange = { height ->
                            coroutineScope.launch {
                                preferencesRepository.setOverlayHeight(height.toInt())
                            }
                        },
                        valueRange = 200f..1000f
                    )
                }
            }

            // Width Slider
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bar Thickness: ${overlayWidth}px", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = overlayWidth.toFloat(),
                        onValueChange = { width ->
                            coroutineScope.launch {
                                preferencesRepository.setOverlayWidth(width.toInt())
                            }
                        },
                        valueRange = 24f..120f
                    )
                }
            }

            // Opacity Slider
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bar Opacity: ${(overlayAlpha * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = overlayAlpha,
                        onValueChange = { alpha ->
                            coroutineScope.launch {
                                preferencesRepository.setOverlayAlpha(alpha)
                            }
                        },
                        valueRange = 0.1f..1.0f
                    )
                }
            }

            // Vertical Offset Slider
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vertical Adjustment: ${overlayOffsetY}px", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = overlayOffsetY.toFloat(),
                        onValueChange = { offset ->
                            coroutineScope.launch {
                                preferencesRepository.setOverlayOffsetY(offset.toInt())
                            }
                        },
                        valueRange = -500f..500f
                    )
                }
            }
        }
    }
}
