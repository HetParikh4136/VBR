package com.archy.vbr.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.archy.vbr.data.PreferencesRepository
import com.archy.vbr.service.FingerprintVolumeService
import com.archy.vbr.service.OverlayVolumeService
import com.archy.vbr.util.VolumeController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    preferencesRepository: PreferencesRepository,
    onOpenOverlaySettings: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isAccessibilityRunning by remember { mutableStateOf(FingerprintVolumeService.isServiceRunning) }
    var isHardwareSupported by remember { mutableStateOf(FingerprintVolumeService.isSupported) }
    var isOverlayRunning by remember { mutableStateOf(OverlayVolumeService.isServiceRunning) }
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // Re-check service statuses on lifecycle resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityRunning = FingerprintVolumeService.isServiceRunning
                isHardwareSupported = FingerprintVolumeService.isSupported
                isOverlayRunning = OverlayVolumeService.isServiceRunning
                hasOverlayPermission = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val volumeController = remember { VolumeController(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VBR Manager") }
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
            // Section 1: Hardware Sensor Status Card
            HardwareStatusCard(
                context = context,
                isRunning = isAccessibilityRunning,
                isSupported = isHardwareSupported
            )

            // Section 2: Assistive Edge Bar Fallback Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Assistive Edge Slider (Recommended)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Places a translucent swipeable bar directly beside your broken volume buttons or power key.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isOverlayRunning) "Overlay Active" else "Overlay Disabled")
                        Switch(
                            checked = isOverlayRunning,
                            onCheckedChange = { enable ->
                                if (enable && !Settings.canDrawOverlays(context)) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } else {
                                    isOverlayRunning = enable
                                    coroutineScope.launch {
                                        preferencesRepository.setOverlayEnabled(enable)
                                    }
                                    val intent = Intent(context, OverlayVolumeService::class.java)
                                    if (enable) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(intent)
                                        } else {
                                            context.startService(intent)
                                        }
                                    } else {
                                        context.stopService(intent)
                                    }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onOpenOverlaySettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Customize Overlay Bar Settings")
                    }
                }
            }

            // Section 3: Quick Test Controls Card
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Quick Test Volume Controls", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Test stepping audio volume up and down directly without physical buttons.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                volumeController.stepVolume(AudioManager.ADJUST_LOWER, showUI = true, haptic = true)
                            }
                        ) {
                            Icon(Icons.Default.VolumeDown, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Volume -")
                        }

                        Button(
                            onClick = {
                                volumeController.stepVolume(AudioManager.ADJUST_RAISE, showUI = true, haptic = true)
                            }
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Volume +")
                        }
                    }
                }
            }

            // Section 4: Quick Settings Instructions
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notification Shade Shortcut", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Swipe down twice on your notification panel, tap the edit/pencil icon, and drag 'Volume Slider' into your active tiles for instant access.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun HardwareStatusCard(
    context: Context,
    isRunning: Boolean,
    isSupported: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isRunning) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
                Spacer(Modifier.width(8.dp))
                Text("Fingerprint Sensor Swipe", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(8.dp))

            val statusText = when {
                !isRunning -> "Accessibility service is disabled. Tap below to turn on the sensor listener."
                isSupported -> "Hardware gestures detected! Swipe up/down on your sensor to step volume."
                else -> "Your device's fingerprint driver does not expose swipe gestures. Please enable the Assistive Edge Slider below."
            }

            Text(statusText, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            ) {
                Text(if (isRunning) "Modify Accessibility Settings" else "Enable Accessibility Service")
            }
        }
    }
}
