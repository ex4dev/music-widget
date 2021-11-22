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
            // open main
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val views = RemoteViews(context?.packageName, R.layout.music_widget)
            views.setOnClickPendingIntent(R.id.titleText, pendingIntent)

            val noti = MusicNotiListener.latestNotification;

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

                if (backIntent != null) views.setOnClickPendingIntent(R.id.back_button, backIntent)
                if (playPauseIntent != null) views.setOnClickPendingIntent(
                    R.id.play_button,
                    playPauseIntent
                )
                if (forwardIntent != null) views.setOnClickPendingIntent(
                    R.id.next_button,
                    forwardIntent
                )
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}