package typebi.util.stralarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.database.Cursor
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Suppress("PrivatePropertyName")
class ClosestChecker(private val application: Application) {
    private val am : AlarmManager by lazy { application.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager }
    private val DB : DBAccesser by lazy { DBAccesser(application) }
    fun setAlarm() : Time {
        val closest = check(DB.selectAlarms())
        val now = LocalDateTime.now().plusSeconds(1)
        val notiTime = closest.time.hour.toString()+":"+reviseMin(closest.time.minute)+" "
        val alarmIntent = Intent(application, AlarmReceiver::class.java)
            .putExtra("num",closest.data.num)
            .putExtra("title",notiTime+application.getString(R.string.noti_title))
            .putExtra("content",application.getString(R.string.noti_content))
            .setAction(application.getString(R.string.noti_action_name))
        if (closest.data.name.isNotEmpty()) alarmIntent.putExtra("title",notiTime+closest.data.name)
        val pended = PendingIntent.getBroadcast(application, closest.data.num, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(System.currentTimeMillis()+ ChronoUnit.MILLIS.between(now, closest.time),pended),pended)
        Log.v("###############################","알람 셋팅")
        return closest
    }
    fun cancel(num :Int, name:String?){
        val alarmIntent = Intent(application, AlarmReceiver::class.java)
            .putExtra("num",num)
            .putExtra("title",application.getString(R.string.noti_title))
            .putExtra("content",application.getString(R.string.noti_content))
            .setAction(application.getString(R.string.noti_action_name))
        if (name!=null && name.isNotEmpty()) alarmIntent.putExtra("title",name)
        val pended = PendingIntent.getBroadcast(application, num, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        (application.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager).cancel(pended)
        Log.v("###############################","알람 캔슬")
    }
    private fun check(data : Cursor) : Time {
        var closestTime = Time(LocalDateTime.now().plusYears(5),DTO(-1,"",-1,-1,-1,-1,-1,-1),Time.NO_ALARMS)
        val now = LocalDateTime.now().plusSeconds(1)
        data.use {
            while (it.moveToNext()) {
                val interval : Long = it.getLong(6)
                val setting = it.getInt(7)
                if (setting != setting or (1 shl 9)) continue
                if (setting == (setting shr 7) shl 7) continue
                var startTime = moveNextDay(LocalDateTime.of(now.year,now.month,now.dayOfMonth,it.getInt(2),it.getInt(3),0),setting)
                var endTime = moveNextDay(LocalDateTime.of(now.year,now.month,now.dayOfMonth,it.getInt(4),it.getInt(5),0), setting)
                if (startTime.isEqual(endTime)) endTime = endTime.plusDays(1)
                if (startTime.isAfter(endTime)) endTime = endTime.plusDays(1)
                if (now.isAfter(startTime) && now.isBefore(endTime)) {
                    while (startTime.isBefore(now))
                        startTime = startTime.plusMinutes(interval)
                    if (startTime.isAfter(endTime))
                        startTime = moveNextDay(LocalDateTime.of(now.year,now.month,now.dayOfMonth,it.getInt(2),it.getInt(3),0).plusDays(1),setting)
                }else if (now.isAfter(endTime))
                    startTime = moveNextDay(startTime.plusDays(1), setting)
                if (startTime.isBefore(closestTime.time))
                    closestTime = Time(startTime, DTO(it), Time.ALARM_EXISTS)
            }
        }
        Log.v("@@@ closestTime @@@",closestTime.time.toString()+" 알람상태 : "+closestTime.state)
        return closestTime
    }
    private fun moveNextDay(day : LocalDateTime, settings: Int):LocalDateTime{
        when(day.dayOfWeek){
            DayOfWeek.MONDAY -> if (settings != settings or (1 shl 0)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.TUESDAY -> if (settings != settings or (1 shl 1)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.WEDNESDAY -> if (settings != settings or (1 shl 2)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.THURSDAY -> if (settings != settings or (1 shl 3)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.FRIDAY -> if (settings != settings or (1 shl 4)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.SATURDAY -> if (settings != settings or (1 shl 5)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.SUNDAY -> if (settings != settings or (1 shl 6)) return moveNextDay(day.plusDays(1), settings)
            else -> return day
        }
        return day
    }
    private fun reviseMin(time:Int) : String{
        return if(time<10)  "0$time"
        else  time.toString()
    }
}