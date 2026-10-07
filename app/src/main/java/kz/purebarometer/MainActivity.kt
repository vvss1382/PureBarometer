package kz.purebarometer

import android.app.Activity
import android.app.StatusBarManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var value: TextView
    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        value = findViewById(R.id.pressureValue)
        info = findViewById(R.id.pressureInfo)
        findViewById<Button>(R.id.addTileButton).setOnClickListener { requestTile() }
        findViewById<Button>(R.id.refreshButton).setOnClickListener { refresh() }

        showCached()
        if (PressureStore.load(this) == null) refresh()
    }

    override fun onResume() {
        super.onResume()
        showCached()
    }

    private fun showCached() {
        val state = PressureStore.load(this)
        if (state == null) {
            value.text = "—"
            info.text = "Нажмите «Обновить»"
        } else {
            value.text = "${PressureReader.formatMmHg(state.value)} ${state.trend}"
            info.text = "мм рт. ст. · обновлено ${PressureStore.formatTime(state.timestamp)}"
        }
    }

    private fun refresh() {
        value.text = "…"
        info.text = "Считываю датчик"
        BarometerUpdater.readAndPublish(this) { result ->
            runOnUiThread {
                if (result.isSuccess) showCached()
                else {
                    value.text = "—"
                    info.text = "Датчик недоступен"
                }
            }
        }
    }

    private fun requestTile() {
        val manager = getSystemService(StatusBarManager::class.java)
        manager.requestAddTileService(
            ComponentName(this, BarometerTileService::class.java),
            getString(R.string.app_name),
            android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_barometer),
            mainExecutor,
            { _ -> }
        )
    }
}
