package typebi.util.stralarm

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import android.util.Log


class AlarmReceiver:BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        //ring 시스템 호출
        //vibe 시스템 호출
        //noti 시스템 호출
        if(context!=null) {
            val noti = NotificationHandler(context)
            noti.showNoti("스트레칭 할 시간이에요", "스트레칭 하면 15초, 안 하면 디스크수술 3천만원", Intent(context, MainActivity::class.java))
            Log.v("##################", "알람리시버 동작")
        }
    }

}