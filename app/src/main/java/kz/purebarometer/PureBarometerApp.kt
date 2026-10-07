package kz.purebarometer

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class PureBarometerApp : Application() {
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                BarometerUpdater.readAndPublish(applicationContext)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
    }
}
