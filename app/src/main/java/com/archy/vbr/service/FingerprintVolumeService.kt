package com.archy.vbr.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.FingerprintGestureController
import android.accessibilityservice.FingerprintGestureController.FingerprintGestureCallback
import android.media.AudioManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.archy.vbr.util.VolumeController

class FingerprintVolumeService : AccessibilityService() {

    private lateinit var volumeController: VolumeController
    private var gestureController: FingerprintGestureController? = null
    private var callback: FingerprintGestureCallback? = null

    companion object {
        var isSupported = false
            private set
        var isServiceRunning = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        volumeController = VolumeController(this)
        isServiceRunning = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                gestureController = fingerprintGestureController
                isSupported = gestureController?.isGestureDetectionAvailable == true

                callback = object : FingerprintGestureCallback() {
                    override fun onGestureDetected(gesture: Int) {
                        when (gesture) {
                            FingerprintGestureController.FINGERPRINT_GESTURE_SWIPE_UP -> {
                                volumeController.stepVolume(AudioManager.ADJUST_RAISE)
                            }
                            FingerprintGestureController.FINGERPRINT_GESTURE_SWIPE_DOWN -> {
                                volumeController.stepVolume(AudioManager.ADJUST_LOWER)
                            }
                        }
                    }

                    override fun onGestureDetectionAvailabilityChanged(available: Boolean) {
                        isSupported = available
                    }
                }

                gestureController?.registerFingerprintGestureCallback(callback!!, null)
            } catch (e: Exception) {
                isSupported = false
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && callback != null && gestureController != null) {
            try {
                gestureController?.unregisterFingerprintGestureCallback(callback)
            } catch (e: Exception) {
                // Ignore unregister errors during teardown
            }
        }
    }
}
