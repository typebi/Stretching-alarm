package typebi.util.stralarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log


class AlarmReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        //ring 시스템 호출
        //vibe 시스템 호출
        //noti 시스템 호출
        if(intent.action!=context.getString(R.string.noti_action_name)) return
        val noti = NotificationHandler(context)
        val title = if(intent.getStringExtra("title")!=null) intent.getStringExtra("title") else "Stretching Alarm"
        val content = if(intent.getStringExtra("content")!=null)  intent.getStringExtra("content") else "Stretch your spine"
        noti.showNoti(title,content)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StretchingAlarm:forNextAlarm")
        wakeLock.acquire(10*60*1000L /*10 minutes*/)
        val forMain = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("isDoze",true)
        }
        context.applicationContext.startActivity(forMain)
        wakeLock.release()
        Log.v("########","startActivity")
    }

}