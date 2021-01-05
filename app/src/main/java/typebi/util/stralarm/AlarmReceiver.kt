package typebi.util.stralarm

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log


class AlarmReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        Log.v("###############################","AlarmReceiver onReceive")
        if(intent.action!=context.getString(R.string.noti_action_name)) return
        val noti = NotificationHandler(context)
        val title = if(intent.getStringExtra("title")!=null) intent.getStringExtra("title") else "Stretching Alarm"
        val content = if(intent.getStringExtra("content")!=null)  intent.getStringExtra("content") else "Stretch your spine"
        noti.showNoti(title,content)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StretchingAlarm:forNextAlarm")
        wakeLock.acquire(10*60*1000L /*10 minutes*/)
        ClosestChecker(context.applicationContext as Application).setAlarm()
        Log.v("###############################","AlarmReceiver setAlarm")
        wakeLock.release()
    }
}