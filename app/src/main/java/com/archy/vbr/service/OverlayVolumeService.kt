package com.archy.vbr.service

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.archy.vbr.MainActivity
import com.archy.vbr.data.PreferencesRepository
import com.archy.vbr.util.VolumeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.abs

class OverlayVolumeService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private lateinit var volumeController: VolumeController
    private lateinit var preferencesRepository: PreferencesRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var fadeJob: Job? = null

    private var overlayWidthPx = 48
    private var overlayHeightPx = 500
    private var overlayPositionLeft = false
    private var overlayOffsetYPx = 0
    private var overlayAlphaValue = 0.6f
    private var idleAlphaValue = 0.0f
    private var autoFadeEnabled = true
    private var hapticEnabled = true
    private var hideFromScreenshots = false

    companion object {
        private const val CHANNEL_ID = "overlay_volume_service_channel"
        private const val NOTIFICATION_ID = 1001
        var isServiceRunning = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        volumeController = VolumeController(this)
        preferencesRepository = PreferencesRepository(this)

        createNotificationChannel()
        startForegroundServiceWithNotification()

        setupOverlayWindow()
        observePreferences()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Assistive Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the VBR edge slider active in the background"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Assistive Volume Slider Active")
            .setContentText("Swipe the screen edge to adjust volume")
            .setSmallIcon(R.drawable.ic_lock_silent_mode_off)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun observePreferences() {
        serviceScope.launch {
            combine(
                preferencesRepository.overlayPositionLeft,
                preferencesRepository.overlayOffsetY,
                preferencesRepository.overlayWidth,
                preferencesRepository.overlayHeight,
                preferencesRepository.overlayAlpha
            ) { isLeft, offsetY, width, height, alpha ->
                arrayOf(isLeft, offsetY, width, height, alpha)
            }.combine(
                combine(
                    preferencesRepository.hapticEnabled,
                    preferencesRepository.hideFromScreenshots,
                    preferencesRepository.autoFadeEnabled,
                    preferencesRepository.idleAlpha
                ) { haptic, hide, autoFade, idleAlpha ->
                    arrayOf(haptic, hide, autoFade, idleAlpha)
                }
            ) { arr, extraArr ->
                OverlayConfig(
                    isLeft = arr[0] as Boolean,
                    offsetY = arr[1] as Int,
                    width = arr[2] as Int,
                    height = arr[3] as Int,
                    alpha = arr[4] as Float,
                    haptic = extraArr[0] as Boolean,
                    hideFromScreenshots = extraArr[1] as Boolean,
                    autoFadeEnabled = extraArr[2] as Boolean,
                    idleAlpha = extraArr[3] as Float
                )
            }.collect { config ->
                overlayPositionLeft = config.isLeft
                overlayOffsetYPx = config.offsetY
                overlayWidthPx = config.width
                overlayHeightPx = config.height
                overlayAlphaValue = config.alpha
                hapticEnabled = config.haptic
                hideFromScreenshots = config.hideFromScreenshots
                autoFadeEnabled = config.autoFadeEnabled
                idleAlphaValue = config.idleAlpha

                updateOverlayLayoutParams()
            }
        }
    }

    private data class OverlayConfig(
        val isLeft: Boolean,
        val offsetY: Int,
        val width: Int,
        val height: Int,
        val alpha: Float,
        val haptic: Boolean,
        val hideFromScreenshots: Boolean,
        val autoFadeEnabled: Boolean,
        val idleAlpha: Float
    )

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOverlayWindow() {
        val params = createLayoutParams()

        overlayView = View(this).apply {
            background = GradientDrawable().apply {
                setColor(Color.argb((overlayAlphaValue * 255).toInt(), 100, 100, 100))
                cornerRadius = 24f
            }
            alpha = if (autoFadeEnabled) idleAlphaValue else 1.0f

            var startY = 0f
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        fadeJob?.cancel()
                        animate().alpha(1.0f).setDuration(150).start()
                        startY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = startY - event.rawY
                        if (abs(deltaY) > 30) {
                            val dir = if (deltaY > 0) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                            volumeController.stepVolume(dir, showUI = true, haptic = hapticEnabled)
                            startY = event.rawY
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        scheduleFadeOut()
                        true
                    }
                    else -> false
                }
            }
        }

        try {
            windowManager.addView(overlayView, params)
            scheduleFadeOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleFadeOut() {
        if (!autoFadeEnabled) return
        fadeJob?.cancel()
        fadeJob = serviceScope.launch {
            delay(2000L)
            overlayView?.animate()?.alpha(idleAlphaValue)?.setDuration(400)?.start()
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val gravityValue = (if (overlayPositionLeft) Gravity.START else Gravity.END) or Gravity.CENTER_VERTICAL

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        if (hideFromScreenshots) {
            flags = flags or WindowManager.LayoutParams.FLAG_SECURE
        }

        return WindowManager.LayoutParams(
            overlayWidthPx,
            overlayHeightPx,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = gravityValue
            x = 0
            y = overlayOffsetYPx
        }
    }

    private fun updateOverlayLayoutParams() {
        overlayView?.let { view ->
            val alphaInt = (overlayAlphaValue * 255).toInt().coerceIn(0, 255)
            view.background = GradientDrawable().apply {
                setColor(Color.argb(alphaInt, 100, 100, 100))
                cornerRadius = 24f
            }

            val newParams = createLayoutParams()
            try {
                windowManager.updateViewLayout(view, newParams)
                scheduleFadeOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        fadeJob?.cancel()
        serviceScope.cancel()
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
    }
}
