package typebi.util.stralarm

import java.time.LocalDateTime

class Time(val time: LocalDateTime, val state: Int){
    companion object{
        const val NO_ALARMS = -1
        const val FINISHED_ALL = 0
        const val ALARM_EXISTS = 1
    }
}