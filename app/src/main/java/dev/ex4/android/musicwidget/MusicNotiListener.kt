package dev.ex4.android.musicwidget

import android.app.Notification
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.KeyEvent
import android.widget.RemoteViews
import android.widget.Toast




class MusicNotiListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.notification.category != Notification::CATEGORY_TRANSPORT.get()) return // Check for media notification

        latestNotification = sbn.notification;
        Log.i("MW", "New song notification: " + sbn.notification.tickerText)


        /*// Update widget now that there's a new media notification
        val views = RemoteViews(applicationContext.packageName, R.layout.music_widget)
        views.setTextViewText(R.id.titleText, sbn.notification.tickerText)
        val manager = AppWidgetManager.getInstance(applicationContext)
        manager.updateAppWidget(
            ComponentName(
                this.packageName,
                MusicWidgetProvider::class.java.name
            ), views
        ) */

        reloadWidget()

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.notification.category != Notification::CATEGORY_TRANSPORT.get()) return

        if (sbn.notification.extras.getString(Notification.EXTRA_MEDIA_SESSION).equals(sbn.notification.extras.getString(Notification.EXTRA_MEDIA_SESSION))) {
            latestNotification = null
        }
        reloadWidget()

    }

    private fun reloadWidget() {
        val intent = Intent(this, MusicWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(
            applicationContext, MusicWidgetProvider::class.java
        ))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    companion object {
        @JvmStatic
        var latestNotification: Notification? = null
    }
}