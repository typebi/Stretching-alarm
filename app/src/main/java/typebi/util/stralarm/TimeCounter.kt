package typebi.util.stralarm

import android.content.Context
import android.os.SystemClock
import android.util.Log
import kotlinx.android.synthetic.main.content_main.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TimeChecker(private val context: Context, var alarmTime: Time):Thread(){
    override fun run() {
        val mainActivity = context as MainActivity
        while (!interrupted()){
            SystemClock.sleep(1000)
            val hour = reviseTime(ChronoUnit.HOURS.between(LocalDateTime.now(), alarmTime.time))
            val min = reviseTime(ChronoUnit.MINUTES.between(LocalDateTime.now(), alarmTime.time) - hour.toInt()*60)
            val sec = reviseTime(ChronoUnit.SECONDS.between(LocalDateTime.now(), alarmTime.time) - hour.toInt()*60*60 - min.toInt()*60)
            mainActivity.time_display.text = if (hour.toInt()>0) context.getString(R.string.alarm_display_hour)+"$hour : $min : $sec"
            else context.getString(R.string.alarm_display_no_hour)+"$min : $sec"
            if (alarmTime.state == Time.NO_ALARMS) {
                mainActivity.time_display.text = context.getString(R.string.alarm_display_no_alarm)
                break
            }
            if(ChronoUnit.SECONDS.between(LocalDateTime.now(), alarmTime.time)<=0)
                mainActivity.makeDisplayThread()
        }
    }
    private fun reviseTime(time:Long) :String{
        return if(time<10) "0$time"
        else time.toString()
    }
}