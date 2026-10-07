package kz.purebarometer

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class BarometerWidgetProvider : AppWidgetProvider() {
    companion object {
        const val ACTION_REFRESH = "kz.purebarometer.ACTION_REFRESH_WIDGET"

        fun updateAllFromCache(context: Context, manager: AppWidgetManager, ids: IntArray) {
            ids.forEach { updateOneFromCache(context, manager, it) }
        }

        private fun updateOneFromCache(context: Context, manager: AppWidgetManager, id: Int) {
            val state = PressureStore.load(context)
            val views = RemoteViews(context.packageName, R.layout.widget_barometer)

            if (state == null) {
                views.setTextViewText(R.id.widgetPressure, "—")
                views.setTextViewText(R.id.widgetUnit, "нажмите для замера")
            } else {
                views.setTextViewText(
                    R.id.widgetPressure,
                    "${PressureReader.formatMmHg(state.value)} ${state.trend}"
                )
                views.setTextViewText(
                    R.id.widgetUnit,
                    "мм рт. ст. · ${PressureStore.formatTime(state.timestamp)}"
                )
            }

            val refreshIntent = Intent(context, BarometerWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val pending = PendingIntent.getBroadcast(
                context,
                1001,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pending)
            manager.updateAppWidget(id, views)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        updateAllFromCache(context, manager, ids)
        if (PressureStore.load(context) == null) {
            BarometerUpdater.readAndPublish(context)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            BarometerUpdater.readAndPublish(context)
        }
    }
}
