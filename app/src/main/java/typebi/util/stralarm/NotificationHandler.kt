package typebi.util.stralarm

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build

class NotificationHandler(private val context: Context){
    private val noti : NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    companion object{
    }
    fun createNotificationChannel(id: String, name: String, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notiChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            notiChannel.enableLights(true)
            notiChannel.lightColor = Color.RED
            notiChannel.description = description
            notiChannel.enableVibration(true)
            noti.createNotificationChannel(notiChannel)
        }
    }
    fun showNoti(title: String, text: String, redirectIntent: Intent){
        val pendingIntent = PendingIntent.getActivity(context,0, redirectIntent,0)
        lateinit var myBuilder : Notification.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("noti","Stretching Alarm","This is Stretching Alarm")
            myBuilder = Notification.Builder(context,"noti")
        }else {
            myBuilder = Notification.Builder(context)
            myBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_HIGH)
        }
        myBuilder.setContentTitle(title)
        .setContentText(text)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setSmallIcon(R.drawable.ic_dialog_email)
        noti.notify(redirectIntent.getIntExtra("num",0), myBuilder.build())
    }
}