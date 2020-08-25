package typebi.util.stralarm

import android.os.SystemClock
import android.util.Log
import kotlinx.android.synthetic.main.content_main.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.reflect.typeOf

<<<<<<< HEAD
class TimeCounter(private val context: MainActivity, private var alarmTime: Time, private val i:Int):Thread(){
    override fun run() {
        while (!currentThread().isInterrupted){
            Log.v("####################", "쓰레드 도는 중 $i")
            SystemClock.sleep(1000)
            val hour = reviseTime(ChronoUnit.HOURS.between(LocalDateTime.now(), alarmTime.time))
            val min = reviseTime(ChronoUnit.MINUTES.between(LocalDateTime.now(), alarmTime.time) - hour.toInt()*60)
            val sec = reviseTime(ChronoUnit.SECONDS.between(LocalDateTime.now(), alarmTime.time) - hour.toInt()*60*60 - min.toInt()*60)
            context.time_display.text = if (hour.toInt()>0) context.getString(R.string.alarm_display)+"$hour : $min : $sec"
            else context.getString(R.string.alarm_display)+"$min : $sec"
            if (alarmTime.state == Time.NO_ALARMS) {
                context.time_display.text = context.getString(R.string.alarm_display_no_alarm)
                break
            }
            if(ChronoUnit.SECONDS.between(LocalDateTime.now(), alarmTime.time)<=0) {
                Log.v("####################","새 쓰레드 호출")
                context.makeDisplayThread()
=======
class TimeCounter(private val main: MainActivity, private var closest: Time):Thread(){
    override fun run() {
        while (!currentThread().isInterrupted){
            if (closest.state == Time.NO_ALARMS) {
                main.time_display.text = main.getString(R.string.alarm_display_no_alarm)
                break
            }
            Log.v("#################","쓰레드 도는 중 "+closest.data.num+" "+closest.state)
            val hour = reviseTime(ChronoUnit.HOURS.between(LocalDateTime.now(), closest.time))
            val min = reviseTime(ChronoUnit.MINUTES.between(LocalDateTime.now(), closest.time) - hour.toInt()*60)
            val sec = reviseTime(ChronoUnit.SECONDS.between(LocalDateTime.now(), closest.time) - hour.toInt()*60*60 - min.toInt()*60)
            main.time_display.text = if (hour.toInt()>0) main.getString(R.string.alarm_display)+"$hour : $min : $sec"
            else main.getString(R.string.alarm_display)+"$min : $sec"
            if(ChronoUnit.SECONDS.between(LocalDateTime.now(), closest.time)<=0) {
                Log.v("##################","새 쓰레드 호출")
                main.makeDisplayThread()
                break
>>>>>>> develop
            }
            SystemClock.sleep(1000)
        }
    }
    private fun reviseTime(time:Long) :String{
        return if(time<10) "0$time"
        else time.toString()
    }
}