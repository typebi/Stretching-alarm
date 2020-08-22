package typebi.util.stralarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

@Suppress("SpellCheckingInspection")
class Dismisser :BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context!=null && intent!=null) {
            val noti = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            noti.cancel(intent.getIntExtra("notiId",1))
        }
    }

}