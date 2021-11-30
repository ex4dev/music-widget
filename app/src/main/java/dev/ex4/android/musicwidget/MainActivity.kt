package dev.ex4.android.musicwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.view.Window
import android.widget.Toast

import android.widget.TextView




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun grantNotificationPermission(view: View) {
        val toast = Toast(applicationContext)
        toast.setText("Give permission to this app to access notifications")
        toast.show()
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intent)
    }

    fun clearSong(view: View) {
        MusicNotiListener.latestNotification = null

        // Reload the widget
        val intent = Intent(this, MusicWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(
            applicationContext, MusicWidgetProvider::class.java
        )
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)

        // Send success message
        Toast.makeText(applicationContext, "Song info cleared from widget.", Toast.LENGTH_SHORT).show()
    }

}