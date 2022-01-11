package dev.ex4.android.musicwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.switchmaterial.SwitchMaterial

class WidgetConfigActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private var settingsIconSwitch: SwitchMaterial? = null
    private var roundedCornersSwitch: SwitchMaterial? = null
    private var reduceStutteringSwitch: SwitchMaterial? = null
    private var hideEmptyWidgetSwitch: SwitchMaterial? = null
    private var updatePausedSwitch: SwitchMaterial? = null
    private var oneLineOnlySwitch: SwitchMaterial? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)
        // Transparent navbar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.config_layout)) { v, insets ->
            val padding = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = padding.bottom, top = padding.top)
            // Return the insets so that they keep going down the view hierarchy
            insets
        }

        val extras = intent.extras
        if (extras != null) widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_CANCELED, resultValue)

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()

        settingsIconSwitch = findViewById(R.id.setting_settings_icon)
        roundedCornersSwitch = findViewById(R.id.setting_rounded_corners)
        reduceStutteringSwitch = findViewById(R.id.setting_reduce_stuttering)
        hideEmptyWidgetSwitch = findViewById(R.id.setting_hide_empty_widget)
        updatePausedSwitch = findViewById(R.id.setting_update_paused)
        oneLineOnlySwitch = findViewById(R.id.setting_one_line_only)

        val grantPermissionButton = findViewById<Button>(R.id.permission_grant_button)


        // Put existing values
        val sharedPref =
            applicationContext.getSharedPreferences(
                applicationContext.getString(R.string.config_file),
                Context.MODE_PRIVATE
            )

        settingsIconSwitch?.isChecked = sharedPref.getBoolean("setting_settings_icon_$widgetId", true)
        roundedCornersSwitch?.isChecked = sharedPref.getBoolean("setting_rounded_corners_$widgetId", true)
        reduceStutteringSwitch?.isChecked = sharedPref.getBoolean("setting_reduce_stuttering_$widgetId", true)
        hideEmptyWidgetSwitch?.isChecked = sharedPref.getBoolean("setting_hide_empty_widget_$widgetId", false)
        updatePausedSwitch?.isChecked = !sharedPref.getBoolean("setting_update_paused_$widgetId", false)
        oneLineOnlySwitch?.isChecked = sharedPref.getBoolean("setting_one_line_only_$widgetId", false)

        grantPermissionButton.visibility = if (sharedPref.getBoolean("permission_granted", false)) View.GONE else View.VISIBLE
    }

    fun confirmConfiguration(view: View) {
        // Save preferences
        val sharedPref = getSharedPreferences(getString(R.string.config_file), Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("setting_settings_icon_$widgetId", settingsIconSwitch!!.isChecked)
            putBoolean("setting_rounded_corners_$widgetId", roundedCornersSwitch!!.isChecked)
            putBoolean("setting_reduce_stuttering_$widgetId", reduceStutteringSwitch!!.isChecked)
            putBoolean("setting_hide_empty_widget_$widgetId", hideEmptyWidgetSwitch!!.isChecked)
            putBoolean("setting_update_paused_$widgetId", !(updatePausedSwitch!!.isChecked))
            putBoolean("setting_one_line_only_$widgetId", oneLineOnlySwitch!!.isChecked)
            apply()
        }

        // Reload widget
        val intent = Intent(this, MusicWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(
            applicationContext, MusicWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)

        // Close menu
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, resultValue)
        finish()
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