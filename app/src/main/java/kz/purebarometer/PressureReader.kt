package kz.purebarometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper

object PressureReader {
    private const val HPA_TO_MMHG = 0.750061683

    fun readOnce(context: Context, callback: (Result<Double>) -> Unit) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        if (sensor == null) {
            callback(Result.failure(IllegalStateException("Датчик давления не найден")))
            return
        }

        val handler = Handler(Looper.getMainLooper())
        var finished = false

        lateinit var listener: SensorEventListener
        val finish: (Result<Double>) -> Unit = { result ->
            if (!finished) {
                finished = true
                manager.unregisterListener(listener)
                handler.removeCallbacksAndMessages(null)
                callback(result)
            }
        }

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val hPa = event.values.firstOrNull()?.toDouble()
                if (hPa != null) finish(Result.success(hPa * HPA_TO_MMHG))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val registered = manager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        if (!registered) {
            finish(Result.failure(IllegalStateException("Не удалось прочитать датчик")))
            return
        }

        handler.postDelayed({
            finish(Result.failure(IllegalStateException("Нет ответа от датчика")))
        }, 2500)
    }

    fun formatMmHg(value: Double): String = "%.1f".format(value)
}
