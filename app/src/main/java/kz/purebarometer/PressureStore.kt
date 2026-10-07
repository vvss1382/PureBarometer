package kz.purebarometer

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PressureStore {
    private const val PREFS = "barometer_state"
    private const val KEY_VALUE = "value_mmhg"
    private const val KEY_PREVIOUS = "previous_mmhg"
    private const val KEY_TIME = "timestamp"

    data class State(
        val value: Double,
        val previous: Double?,
        val timestamp: Long
    ) {
        val trend: String
            get() {
                val old = previous ?: return "→"
                val delta = value - old
                return when {
                    delta >= 0.3 -> "↑"
                    delta <= -0.3 -> "↓"
                    else -> "→"
                }
            }
    }

    fun save(context: Context, value: Double): State {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val old = if (prefs.contains(KEY_VALUE)) {
            prefs.getFloat(KEY_VALUE, Float.NaN).toDouble().takeUnless { it.isNaN() }
        } else null
        val now = System.currentTimeMillis()
        prefs.edit()
            .putFloat(KEY_VALUE, value.toFloat())
            .apply {
                if (old != null) putFloat(KEY_PREVIOUS, old.toFloat()) else remove(KEY_PREVIOUS)
            }
            .putLong(KEY_TIME, now)
            .apply()
        return State(value, old, now)
    }

    fun load(context: Context): State? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_VALUE)) return null
        val value = prefs.getFloat(KEY_VALUE, Float.NaN).toDouble()
        if (value.isNaN()) return null
        val previous = if (prefs.contains(KEY_PREVIOUS)) {
            prefs.getFloat(KEY_PREVIOUS, Float.NaN).toDouble().takeUnless { it.isNaN() }
        } else null
        return State(value, previous, prefs.getLong(KEY_TIME, 0L))
    }

    fun formatTime(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
