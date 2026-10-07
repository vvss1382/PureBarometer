package kz.purebarometer

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class BarometerTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        showCachedValue()
    }

    override fun onClick() {
        super.onClick()
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            label = "Обновление…"
            subtitle = "Барометр"
            updateTile()
        }
        BarometerUpdater.readAndPublish(this) { result ->
            if (result.isFailure) showUnavailable() else showCachedValue()
        }
    }

    private fun showCachedValue() {
        val cached = PressureStore.load(this)
        qsTile?.apply {
            if (cached == null) {
                state = Tile.STATE_INACTIVE
                label = "Барометр"
                subtitle = "Нажмите для замера"
            } else {
                state = Tile.STATE_ACTIVE
                label = "${PressureReader.formatMmHg(cached.value)} мм ${cached.trend}"
                subtitle = "обновлено ${PressureStore.formatTime(cached.timestamp)}"
            }
            updateTile()
        }
    }

    private fun showUnavailable() {
        qsTile?.apply {
            state = Tile.STATE_UNAVAILABLE
            label = "Барометр"
            subtitle = "датчик недоступен"
            updateTile()
        }
    }
}
