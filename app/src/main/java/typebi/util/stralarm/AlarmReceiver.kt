package typebi.util.stralarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log


class AlarmReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "STRETCHING_TIME" -> noteAndsetAlarm(context, intent)
        }
    }
    private fun noteAndsetAlarm(context: Context, intent: Intent){
        val noti = NotificationHandler(context)
        val title = if(intent.getStringExtra("title")!=null) intent.getStringExtra("title") else "Stretching Alarm"
        val content = if(intent.getStringExtra("content")!=null)  intent.getStringExtra("content") else "Stretch your spine"
        val vibration = intent.getBooleanExtra("vibration", true)
        noti.showNoti(title,content,vibration)
        Log.v("##################", "알람리시버 동작")
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StretchingAlarm:forNextAlarm")
        wakeLock.acquire(1*60*1000L /*1 minutes*/)
        val forMain = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("isDoze",true)
        }
        context.startActivity(forMain)
        wakeLock.release()
    }
}