package dev.ex4.android.musicwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.content.Intent
import android.content.pm.ApplicationInfo

import android.widget.AdapterView.OnItemClickListener
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding


class AppPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)
        // Transparent navbar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.apps_list)) { v, insets ->
            val padding = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = padding.bottom, top = padding.top)
            // Return the insets so that they keep going down the view hierarchy
            insets
        }

        val apps = packageManager.getInstalledPackages(0)
        val appsList: ListView = findViewById(R.id.apps_list)
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