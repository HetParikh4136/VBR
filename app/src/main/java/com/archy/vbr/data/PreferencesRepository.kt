package com.archy.vbr.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vbr_settings")

class PreferencesRepository(private val context: Context) {

    companion object {
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val HIDE_FROM_SCREENSHOTS = booleanPreferencesKey("hide_from_screenshots")
        val AUTO_FADE_ENABLED = booleanPreferencesKey("auto_fade_enabled")
        val IDLE_ALPHA = floatPreferencesKey("idle_alpha")
        val OVERLAY_POSITION_LEFT = booleanPreferencesKey("overlay_position_left")
        val OVERLAY_OFFSET_Y = intPreferencesKey("overlay_offset_y")
        val OVERLAY_HEIGHT = intPreferencesKey("overlay_height")
        val OVERLAY_WIDTH = intPreferencesKey("overlay_width")
        val OVERLAY_ALPHA = floatPreferencesKey("overlay_alpha")
    }

    val overlayEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[OVERLAY_ENABLED] ?: false
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAPTIC_ENABLED] ?: true
    }

    val hideFromScreenshots: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HIDE_FROM_SCREENSHOTS] ?: false
    }

    val autoFadeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTO_FADE_ENABLED] ?: true
    }

    val idleAlpha: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[IDLE_ALPHA] ?: 0.0f
    }

    val overlayPositionLeft: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[OVERLAY_POSITION_LEFT] ?: false
    }

    val overlayOffsetY: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[OVERLAY_OFFSET_Y] ?: 0
    }

    val overlayHeight: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[OVERLAY_HEIGHT] ?: 500
    }

    val overlayWidth: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[OVERLAY_WIDTH] ?: 48
    }

    val overlayAlpha: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[OVERLAY_ALPHA] ?: 0.6f
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[OVERLAY_ENABLED] = enabled
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setHideFromScreenshots(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HIDE_FROM_SCREENSHOTS] = enabled
        }
    }

    suspend fun setAutoFadeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_FADE_ENABLED] = enabled
        }
    }

    suspend fun setIdleAlpha(alpha: Float) {
        context.dataStore.edit { prefs ->
            prefs[IDLE_ALPHA] = alpha
        }
    }

    suspend fun setOverlayPositionLeft(isLeft: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[OVERLAY_POSITION_LEFT] = isLeft
        }
    }

    suspend fun setOverlayOffsetY(offsetY: Int) {
        context.dataStore.edit { prefs ->
            prefs[OVERLAY_OFFSET_Y] = offsetY
        }
    }

    suspend fun setOverlayHeight(height: Int) {
        context.dataStore.edit { prefs ->
            prefs[OVERLAY_HEIGHT] = height
        }
    }

    suspend fun setOverlayWidth(width: Int) {
        context.dataStore.edit { prefs ->
            prefs[OVERLAY_WIDTH] = width
        }
    }

    suspend fun setOverlayAlpha(alpha: Float) {
        context.dataStore.edit { prefs ->
            prefs[OVERLAY_ALPHA] = alpha
        }
    }
}
