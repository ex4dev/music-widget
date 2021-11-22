package dev.ex4.android.musicwidget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Toast

import android.widget.TextView




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun grantNotificationPermission(view: View) {
        val toast = Toast(applicationContext)
        toast.setText("Give permission to this app to access notifications")
        toast.show()
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intent)
    }

}