package typebi.util.stralarm

import android.database.Cursor
import java.time.LocalDateTime

class Time(val time: LocalDateTime, val data : DTO, val state: Int){
    companion object{
        const val NO_ALARMS = -1
        const val ALARM_EXISTS = 1
    }
}