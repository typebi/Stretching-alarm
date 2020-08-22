package typebi.util.stralarm

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

class AlarmSchedulerService : JobService(){
    override fun onStartJob(p0: JobParameters?): Boolean {
        val noti = NotificationHandler(this)
        noti.showNoti(getString(R.string.noti_title), getString(R.string.noti_content), true)
        Log.v("##################", "알람스케쥴러서비스 노티 동작")
        return false
    }
    override fun onStopJob(p0: JobParameters?): Boolean {
        Log.v("##################", "알람스케쥴러서비스 onStopJob 동작")
        return false
    }
}