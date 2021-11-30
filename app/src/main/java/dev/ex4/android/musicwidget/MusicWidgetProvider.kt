package dev.ex4.android.musicwidget

import android.app.Notification
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

class MusicWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        if (appWidgetIds == null) return // No widgets
        if (appWidgetManager == null) return
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context?.packageName, R.layout.music_widget)

            val noti = MusicNotiListener.latestNotification;

            val openSettingsIntent = Intent(context, MainActivity::class.java)
            val openSettingsPendingIntent =
                PendingIntent.getActivity(context, 0, openSettingsIntent, PendingIntent.FLAG_IMMUTABLE)

            views.setOnClickPendingIntent(R.id.settings_button, openSettingsPendingIntent)

            if (noti != null) {

                // Set up the song details, including the title, artist, and image
                val title = noti.extras.get(Notification.EXTRA_TITLE)
                val subtext = noti.extras.get(Notification.EXTRA_TEXT)
                val icon = noti.getLargeIcon()

                views.setTextViewText(
                    R.id.titleText,
                    // noti.tickerText
                    "$title\n$subtext"
                )
                views.setImageViewIcon(R.id.musicThumbnail, icon)
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

                if (backIntent != null) views.setOnClickPendingIntent(R.id.back_button, backIntent)
                if (playPauseIntent != null) views.setOnClickPendingIntent(
                    R.id.play_button,
                    playPauseIntent
                )
                if (forwardIntent != null) views.setOnClickPendingIntent(
                    R.id.next_button,
                    forwardIntent
                )
                if (openAppIntent != null) {
                    views.setOnClickPendingIntent(R.id.titleText, openAppIntent)
                    views.setOnClickPendingIntent(R.id.music_widget_layout, openAppIntent)
                }
                if (playPauseIcon == "pause") views.setImageViewResource(R.id.play_button, R.drawable.pause_icon)
                else views.setImageViewResource(R.id.play_button, R.drawable.play_icon)
            } else {
                // When no music, the text just opens the Music Widget app

                views.setOnClickPendingIntent(R.id.titleText, openSettingsPendingIntent)
                // If no notification
                views.setImageViewIcon(R.id.musicThumbnail, null) // Remove icon
                views.setTextViewText( // Remove text
                    R.id.titleText,
                    "No media playing"
                )
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}