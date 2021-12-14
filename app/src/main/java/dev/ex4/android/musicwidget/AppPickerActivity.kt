package dev.ex4.android.musicwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.provider.AlarmClock.EXTRA_MESSAGE

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.provider.AlarmClock

import android.widget.AdapterView.OnItemClickListener
import android.content.pm.PackageManager
import android.widget.*


class AppPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        val apps = packageManager.getInstalledPackages(0)
        val appsList: ListView = findViewById(R.id.appsList)
        val appNamesList = HashMap<String, ApplicationInfo>() // display name to package name
        for (app in apps) {
            if (app == null) continue
            if (app.applicationInfo == null) continue
            val packageManager = applicationContext.packageManager
            val appName = packageManager.getApplicationLabel(app.applicationInfo) as String
            appNamesList[appName] = app.applicationInfo
        }
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, appNamesList.keys.toList().sorted())
        appsList.adapter = adapter

        appsList.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val entry = parent.adapter.getItem(position).toString()
            Toast.makeText(applicationContext, "Default music app set to $entry", Toast.LENGTH_SHORT).show()
            setDefaultMusicApp(appNamesList[entry]!!)
            finish()
        }

    }

    private fun setDefaultMusicApp(newApp: ApplicationInfo) {
        // Change the default configured app
        Log.i("MW", "Setting default app to " + newApp.packageName)
        val sharedPref = getSharedPreferences(getString(R.string.config_file), Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("default_player", newApp.packageName)
            apply()
        }

        // Reload the widget to apply the changes
        if (MusicNotiListener.latestNotification == null) {
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
}