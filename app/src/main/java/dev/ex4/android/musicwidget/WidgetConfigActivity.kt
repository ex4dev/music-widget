package dev.ex4.android.musicwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.switchmaterial.SwitchMaterial

class WidgetConfigActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private var settingsIconSwitch: SwitchMaterial? = null
    private var roundedCornersSwitch: SwitchMaterial? = null
    private var reduceStutteringSwitch: SwitchMaterial? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)

        val extras = intent.extras
        if (extras != null) widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_CANCELED, resultValue)

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()

        settingsIconSwitch = findViewById(R.id.setting_settings_icon)
        roundedCornersSwitch = findViewById(R.id.setting_rounded_corners)
        reduceStutteringSwitch = findViewById(R.id.setting_reduce_stuttering)

        // Put existing values
        val sharedPref =
            applicationContext.getSharedPreferences(
                applicationContext.getString(R.string.config_file),
                Context.MODE_PRIVATE
            )

        settingsIconSwitch?.isChecked = sharedPref.getBoolean("setting_settings_icon_$widgetId", true)
        roundedCornersSwitch?.isChecked = sharedPref.getBoolean("setting_rounded_corners_$widgetId", true)
        reduceStutteringSwitch?.isChecked = sharedPref.getBoolean("setting_reduce_stuttering_$widgetId", true)
    }

    fun confirmConfiguration(view: View) {
        // Save preferences
        val sharedPref = getSharedPreferences(getString(R.string.config_file), Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("setting_settings_icon_$widgetId", settingsIconSwitch!!.isChecked)
            putBoolean("setting_rounded_corners_$widgetId", roundedCornersSwitch!!.isChecked)
            putBoolean("setting_reduce_stuttering_$widgetId", reduceStutteringSwitch!!.isChecked)
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
}