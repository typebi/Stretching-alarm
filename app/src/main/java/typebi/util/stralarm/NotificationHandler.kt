package typebi.util.stralarm

import android.R
import android.app.Notification
import android.app.Notification.Action.Builder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log

class NotificationHandler(private val context: Context){
    private val noti : NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private fun createNotificationChannel(vibration:Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notiChannel = NotificationChannel("noti_channel", "Stretching Alarm Channel", NotificationManager.IMPORTANCE_HIGH)
            //if(!vibration) notiChannel.importance = NotificationManager.IMPORTANCE_DEFAULT
            notiChannel.enableLights(true)
            notiChannel.lightColor = Color.RED
            notiChannel.description = "This is Stretching Alarm Channel"
            //notiChannel.vibrationPattern = longArrayOf(500,500,500,500)
            noti.createNotificationChannel(notiChannel)
        }
    }
    fun showNoti(title: String, text: String, vibration:Boolean){
        lateinit var myBuilder : Notification.Builder
        @Suppress("DEPRECATION")
        myBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createNotificationChannel(vibration)
                        Notification.Builder(context,"noti_channel")
                    }else{
                        Notification.Builder(context)
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setPriority(Notification.PRIORITY_HIGH)
                    }
        Log.v("##################", "showNoti vibeFlag : "+vibration)
        val intent = Intent(context, ProgressPage::class.java)
            .putExtra("time",15)
            .putExtra("vibration",vibration)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pender = PendingIntent.getActivity(context,1, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        myBuilder.setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pender)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(Builder(Icon.createWithResource(context, R.drawable.ic_lock_idle_alarm), "15sec", pender).build())
            .addAction(Builder(Icon.createWithResource(context, R.drawable.ic_lock_idle_alarm), "30sec", PendingIntent.getActivity(context,0, Intent(context, ProgressPage::class.java).putExtra("time",30).putExtra("vibration",vibration).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0)).build())
            .addAction(Builder(Icon.createWithResource(context, R.drawable.ic_lock_idle_alarm), "Pass", PendingIntent.getBroadcast(context,0, Intent(context, Dismisser::class.java).putExtra("notiId",1).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0)).build())
        noti.notify(1, myBuilder.build())
    }
}