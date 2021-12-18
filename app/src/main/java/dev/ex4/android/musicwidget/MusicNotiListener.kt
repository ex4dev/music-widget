package dev.ex4.android.musicwidget

import android.app.Notification
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log


class MusicNotiListener : NotificationListenerService() {

    private var latestUnpausedSBN: StatusBarNotification? = null


    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.notification.category != Notification::CATEGORY_TRANSPORT.get()) return // Check for media notification

        var hideLatestNotification = false // if true, the notification is not updated

        latestNotification = sbn.notification

        // if the noti is from a different app
        if (latestUnpausedSBN == null || sbn.uid != latestUnpausedSBN?.uid) {
            // if the noti is paused
            hideLatestNotification = (MusicWidgetProvider.isPaused(sbn.notification))
            if (!hideLatestNotification) {
                latestUnpausedSBN = sbn
                latestTimestamp = System.currentTimeMillis()
            }
            Log.i("MW", "New song notification is from a different app (${sbn.packageName}). Paused? $hideLatestNotification (old app: ${latestUnpausedSBN?.uid}, new app: ${sbn.uid})")
        } else {
            // if from same app
            hideLatestNotification = false
            latestTimestamp = System.currentTimeMillis()
            Log.i("MW", "New song notification is from the same app (${sbn.packageName}). Noti will never be hidden. Music paused? ${MusicWidgetProvider.isPaused(sbn.notification)} (old app: ${latestUnpausedSBN?.uid}, new app: ${sbn.uid})")
        }

        Log.i("MW", "New song notification: " + sbn.notification.tickerText)


        reloadWidget(latestNotification, latestTimestamp, hideLatestNotification)

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.notification.category != Notification::CATEGORY_TRANSPORT.get()) return

        if (sbn.notification.extras.getString(Notification.EXTRA_MEDIA_SESSION).equals(sbn.notification.extras.getString(Notification.EXTRA_MEDIA_SESSION))) {
            reloadWidget(null, System.currentTimeMillis(), false)
        }

    }

    /**
     * Reloads the widget and passes along the notification information.
     * @param notificationObject The notification to add to the widget
     * @param hideLatestNotification Whether or not the notification should be hidden
     * if the "don't update with paused media" option is on
     */
    private fun reloadWidget(notificationObject: Notification?, timestamp: Long, hideLatestNotification: Boolean) {
        val intent = Intent(this, MusicWidgetProvider::class.java)
        intent.action = getString(R.string.widget_update_intent)
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(
            applicationContext, MusicWidgetProvider::class.java
        ))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        intent.putExtra("notificationObject", notificationObject)
        intent.putExtra("notificationTime", timestamp)
        intent.putExtra("hideLatestNotification", hideLatestNotification)
        sendBroadcast(intent)
    }

    companion object {
        @JvmStatic
        var latestNotification: Notification? = null
        @JvmStatic
        var latestTimestamp = 0L
        /*
        @JvmStatic
        var latestUnpausedSBN: StatusBarNotification? = null
        @JvmStatic
        var hideLatestNotification = false // if true, the notification is not updated
         */
    }
}