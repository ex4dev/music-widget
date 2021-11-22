package dev.ex4.android.musicwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
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

            val intent = Intent(context, MainActivity::class.java)

            if (noti != null) {
                // Update song name on widget
                views.setTextViewText(
                    R.id.titleText,
                    noti.tickerText
                )
                val manager = AppWidgetManager.getInstance(context)

                // Update back/foward buttons to make them work with this new notification
                var backIntent: PendingIntent? = null
                var playPauseIntent: PendingIntent? = null
                var forwardIntent: PendingIntent? = null
                var openAppIntent: PendingIntent? = null

                for (action in noti.actions) {
                    Log.v("MusicNotificationListener", "Action: " + action.title)
                    if (action.title.toString().lowercase().contains("previous")) backIntent =
                        action.actionIntent
                    if (action.title.toString().lowercase()
                            .contains("play") || action.title.toString().lowercase()
                            .contains("pause")
                    ) {
                        playPauseIntent = action.actionIntent
                    }
                    if (action.title.toString().lowercase().contains("next")) forwardIntent =
                        action.actionIntent
                    else if (action.title == "Next") forwardIntent = action.actionIntent
                }

                openAppIntent = noti.contentIntent

                if (backIntent != null) views.setOnClickPendingIntent(R.id.back_button, backIntent)
                if (playPauseIntent != null) views.setOnClickPendingIntent(R.id.play_button, playPauseIntent)
                if (forwardIntent != null) views.setOnClickPendingIntent(R.id.next_button, forwardIntent)
                if (openAppIntent != null) {
                    views.setOnClickPendingIntent(R.id.titleText, openAppIntent)
                    views.setOnClickPendingIntent(R.id.music_widget_layout, openAppIntent)
                }
            } else {
                // When no music, the text just opens the Music Widget app
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.titleText, pendingIntent)
                // If no notification
                views.setTextViewText(
                    R.id.titleText,
                    "No media playing"
                )
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}