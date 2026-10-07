package kz.purebarometer

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService

object BarometerUpdater {
    fun readAndPublish(context: Context, callback: ((Result<PressureStore.State>) -> Unit)? = null) {
        PressureReader.readOnce(context.applicationContext) { reading ->
            val stateResult = reading.map { PressureStore.save(context.applicationContext, it) }
            updateWidgetsFromCache(context.applicationContext)
            requestTileRefresh(context.applicationContext)
            callback?.invoke(stateResult)
        }
    }

    fun updateWidgetsFromCache(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, BarometerWidgetProvider::class.java))
        BarometerWidgetProvider.updateAllFromCache(context, manager, ids)
    }

    fun requestTileRefresh(context: Context) {
        TileService.requestListeningState(
            context,
            ComponentName(context, BarometerTileService::class.java)
        )
    }
}
