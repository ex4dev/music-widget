package dev.ex4.android.musicwidget

import android.app.Notification
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import android.os.Looper




class MusicWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        if (appWidgetIds == null) return // No widgets
        if (appWidgetManager == null) return
        for (appWidgetId in appWidgetIds) {

            var noti = MusicNotiListener.latestNotification
            // Update layout
            val tinyView = RemoteViews(context?.packageName, R.layout.music_widget_size_1)
            updateData(context, tinyView, noti)
            val smallView = RemoteViews(context?.packageName, R.layout.music_widget_size_2)
            updateData(context, smallView, noti)
            val mediumView = RemoteViews(context?.packageName, R.layout.music_widget_size_3)
            updateData(context, mediumView, noti)
            val largeView = RemoteViews(context?.packageName, R.layout.music_widget)
            updateData(context, largeView, noti)

            val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                SizeF(50f, 100f) to tinyView,
                SizeF(130f, 100f) to smallView,
                SizeF(200f, 100f) to mediumView,
                SizeF(270f, 100f) to largeView
            )
            val views = RemoteViews(viewMapping)

            // Update information
            // updateData(context, views)
            if (noti != null)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            // When the notification was cleared, wait 5 seconds before deciding whether to clear the widget to avoid stuttering
            else if (Looper.myLooper() != null) Handler(Looper.myLooper()!!).postDelayed({
                Log.i("MW", "Clearing music widget.")
                if (MusicNotiListener.latestNotification == null)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                // else Log.i("MW", "Notification received within 5 second of being cleared.")
            }, 5000)
        }
    }

    fun updateData(context: Context?, views: RemoteViews, noti: Notification?) {
        val openSettingsIntent = Intent(context, MainActivity::class.java)
        val openSettingsPendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                openSettingsIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

        var openDefaultAppIntent = openSettingsIntent
        if (context != null) {
            val sharedPref =
                context.getSharedPreferences(
                    context.getString(R.string.config_file),
                    Context.MODE_PRIVATE
                )

            val packageManager = context.packageManager
            val packageName = sharedPref.getString("default_player", context.packageName)
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName!!)
            if (launchIntent != null) openDefaultAppIntent =
                packageManager.getLaunchIntentForPackage(packageName)!!
        }
        var openDefaultAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openDefaultAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )


        if (views.layoutId == R.layout.music_widget) views.setOnClickPendingIntent(
            R.id.settings_button,
            openSettingsPendingIntent
        )

        if (noti != null) {

            // Set up the song details, including the title, artist, and image
            val title = noti.extras.get(Notification.EXTRA_TITLE)
            val subtext = noti.extras.get(Notification.EXTRA_TEXT)
            val icon = noti.getLargeIcon()

            if (sizeMin(views, 3)) {
                views.setTextViewText(
                    R.id.titleText,
                    // noti.tickerText
                    "$title"
                )
                views.setTextViewText(R.id.subtitleText, "$subtext")
            }
            if (sizeMin(views, 2)) views.setImageViewIcon(R.id.musicThumbnail, icon)
            for (key in noti.extras.keySet()) {
                Log.i("MusicWidgetProvider", key + "=" + noti.extras.get(key).toString())
            }

            // Update back/foward buttons to make them work with this new notification
            var backIntent: PendingIntent? = null
            var playPauseIntent: PendingIntent? = null
            var forwardIntent: PendingIntent? = null
            var openAppIntent: PendingIntent? = null
            var playPauseIcon: String? = "play"

            for (action in noti.actions) {
                Log.v("MusicNotificationListener", "Action: " + action.title)
                when {
                    action.title.toString().lowercase().contains("previous") -> backIntent =
                        action.actionIntent
                    action.title.toString().lowercase().contains("play") -> {
                        playPauseIntent = action.actionIntent
                        playPauseIcon = "play"
                    }
                    action.title.toString().lowercase().contains("pause") -> {
                        playPauseIntent = action.actionIntent
                        playPauseIcon = "pause"
                    }
                    action.title.toString().lowercase().contains("next") -> forwardIntent =
                        action.actionIntent
                }
            }

            openAppIntent = noti.contentIntent

            if (sizeMin(
                    views,
                    2
                ) && backIntent != null
            ) views.setOnClickPendingIntent(R.id.back_button, backIntent)
            if (playPauseIntent != null) views.setOnClickPendingIntent(
                R.id.play_button,
                playPauseIntent
            )
            if (sizeMin(views, 2) && forwardIntent != null) views.setOnClickPendingIntent(
                R.id.next_button,
                forwardIntent
            )
            if (openAppIntent != null) {
                if (sizeMin(views, 3)) {
                    views.setOnClickPendingIntent(R.id.titleText, openAppIntent)
                    views.setOnClickPendingIntent(R.id.subtitleText, openAppIntent)
                }
                if (sizeMin(views, 2)) views.setOnClickPendingIntent(
                    R.id.musicThumbnail,
                    openAppIntent
                )
                views.setOnClickPendingIntent(R.id.music_widget_layout, openAppIntent)
            }
            if (playPauseIcon == "pause") views.setImageViewResource(
                R.id.play_button,
                R.drawable.pause_icon
            )
            else views.setImageViewResource(R.id.play_button, R.drawable.play_icon)
        } else {
            // When no music, the text just opens the Music Widget app
            // If no notification

            if (sizeMin(views, 3)) {
                views.setTextViewText(R.id.titleText, "No media playing")
                views.setTextViewText(R.id.subtitleText, "")
                views.setOnClickPendingIntent(R.id.titleText, openDefaultAppPendingIntent)
                views.setOnClickPendingIntent(R.id.subtitleText, openDefaultAppPendingIntent)
            }
            if (sizeMin(views, 2)) {
                views.setOnClickPendingIntent(R.id.musicThumbnail, null)
                views.setOnClickPendingIntent(R.id.music_widget_layout, openDefaultAppPendingIntent)
                views.setImageViewIcon(R.id.musicThumbnail, null)
            }
        }
    }

    fun sizeMin(view: RemoteViews, min: Int): Boolean {
        if (min == 4) return (view.layoutId == R.layout.music_widget)
        if (min == 3) return (view.layoutId == R.layout.music_widget || view.layoutId == R.layout.music_widget_size_3)
        if (min == 2) return (view.layoutId == R.layout.music_widget || view.layoutId == R.layout.music_widget_size_3 || view.layoutId == R.layout.music_widget_size_2)
        if (min == 1) return (view.layoutId == R.layout.music_widget || view.layoutId == R.layout.music_widget_size_3 || view.layoutId == R.layout.music_widget_size_2 || view.layoutId == R.layout.music_widget_size_1)
        return false
    }
}