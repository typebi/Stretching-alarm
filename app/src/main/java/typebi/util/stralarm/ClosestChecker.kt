package typebi.util.stralarm

import android.database.Cursor
import android.util.Log
import java.time.DayOfWeek
import java.time.LocalDateTime

class ClosestChecker(private val main : MainActivity) {
    fun check(data : Cursor) : Time {
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
                Log.v("@@@ startTime @@@",startTime.toString())
                Log.v("@@@ endTime @@@",endTime.toString())
                if (now.isAfter(startTime) && now.isBefore(endTime)) {
                    while (startTime.isBefore(now))
                        startTime = startTime.plusMinutes(interval)
                    if (startTime.isAfter(endTime))
                        startTime = moveNextDay(LocalDateTime.of(now.year,now.month,now.dayOfMonth,it.getInt(2),it.getInt(3),0).plusDays(1),setting)
                }else if (now.isAfter(endTime))
                    startTime = moveNextDay(startTime.plusDays(1), setting)
                Log.v("@@@ startTime2 @@@",startTime.toString())
                if (startTime.isBefore(closestTime.time))
                    closestTime = Time(startTime, DTO(it), Time.ALARM_EXISTS)
            }
        }
        Log.v("@@@ closestTime @@@",closestTime.time.toString())
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
}