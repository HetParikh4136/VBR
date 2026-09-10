package com.archy.vbr.service

import android.media.AudioManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.archy.vbr.util.VolumeController

class VolumeTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            label = "Volume Slider"
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val controller = VolumeController(this)
        controller.stepVolume(AudioManager.ADJUST_SAME, showUI = true, haptic = true)
    }
}
