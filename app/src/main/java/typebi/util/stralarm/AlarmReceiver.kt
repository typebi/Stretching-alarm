package typebi.util.stralarm

import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.Log


class AlarmReceiver:BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.v("##################", "알람리시버 호출")
        //ring 시스템 호출
        //vibe 시스템 호출
        //noti 시스템 호출
        if(context!=null && intent!=null) {
            val noti = NotificationHandler(context)
            val title = if(intent.getStringExtra("title")!=null) intent.getStringExtra("title") else "Stretching Alarm"
            val content = if(intent.getStringExtra("content")!=null)  intent.getStringExtra("content") else "Stretch your spine"
            noti.showNoti(title,content)
            Log.v("##################", "알람리시버 노티 동작")
        }
    }

}