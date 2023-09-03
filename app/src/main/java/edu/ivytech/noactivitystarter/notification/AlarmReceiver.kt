package edu.ivytech.noactivitystarter.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.ivytech.noactivitystarter.MainActivity
import edu.ivytech.noactivitystarter.R
import java.util.Date.from

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            handleAlarm(context, it)
        }
    }

    private fun handleAlarm(context: Context?, intent: Intent?) {
        context?.let {

            val title = intent?.getStringExtra("title")
            val desc = intent?.getStringExtra("timeUntil")
            createChannel(context = it)
            createNotification(context,title!!,desc!!)
        }
    }

    private fun createNotification(
        context: Context,
        title: String = "",
        desc: String = ""
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "CHANNEL_ID")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)){
        notify(System.currentTimeMillis().toInt(),builder.build())
        }

    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "ADMIN_NOTIFICATIONS",
                "ADMINISTRATION OPEN LABS",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for Open Lab Events"
            }
            val notifManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as
                        NotificationManager
            notifManager.createNotificationChannel(channel)
        }
    }

}