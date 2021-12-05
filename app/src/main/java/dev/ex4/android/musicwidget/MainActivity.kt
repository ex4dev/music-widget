package dev.ex4.android.musicwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
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
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intent)

        val sharedPref = getSharedPreferences(getString(R.string.config_file), Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("permission_granted", true)
            apply()
        }

        reloadWidget()

        val toast = Toast(applicationContext)
        toast.setText("Give permission to \"Music Widget\" to access notifications")
        toast.show()

    }

    fun clearSong(view: View) {
        MusicNotiListener.latestNotification = null

        // Reload the widget
        reloadWidget()

        // Send success message
        Toast.makeText(applicationContext, "Song info cleared from widget.", Toast.LENGTH_SHORT).show()
    }

    fun chooseDefaultApp(view: View) {
        val intent = Intent(this, AppPickerActivity::class.java)
        startActivity(intent)
    }

    private fun reloadWidget() {
        val intent = Intent(this, MusicWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(
                applicationContext, MusicWidgetProvider::class.java
            )
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

}