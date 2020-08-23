package typebi.util.stralarm

import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.content_main.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class MainActivity : AppCompatActivity() {
    private lateinit var db:SQLiteDatabase
    private lateinit var timeChecker:TimeCounter
    lateinit var mAdView : AdView
    private val DB : DBAccesser by lazy { DBAccesser(openOrCreateDatabase("stretchingAlarm",MODE_PRIVATE, null)) }
    private val am : AlarmManager by lazy {
        getSystemService(ALARM_SERVICE) as AlarmManager
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.admob)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        Log.v("@@@@@@@@@@@@@@@@@","onCreate() 실행")
        DB.createTable(getString(R.string.createTable))
        db = openOrCreateDatabase("stretchingAlarm",MODE_PRIVATE, null)
        timeChecker = TimeCounter(this, checkClosest())
        timeChecker.start()
        renewAlarms()
        testBtn.setOnClickListener{
            startActivity(Intent(this, ProgressPage::class.java))
        }
        if (intent.getBooleanExtra("isDoze",false)) {
            timeChecker.interrupt()
            this.finish()
        }
    }
    override fun onStart() {
        super.onStart()
        Log.v("%%%%%%%%%%%%%%%%%%%","onStart() 실행")
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data!=null) {
            val isDelete = data.getBooleanExtra("isDelete",false)
            val num = data.getIntExtra("num", 0)
            if (isDelete) {
                DB.deleteAlarm(data)
                val alarmIntent = Intent(this, AlarmReceiver::class.java).putExtra("num",num)
                val pended = PendingIntent.getBroadcast(this, num, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                am.cancel(pended)
                makeDisplayThread()
                renewAlarms()
                return
            }
            when (requestCode) {
                1001 -> {
                    alarm_list.removeView(alarm_list.children.last())
                    ViewDrawer(this).addNewAlarmToLayout(DB.insertAlarm(data, getString(R.string.selectLatest)))
                    alarm_list.addView(ViewDrawer(this).addNewBtn())
                    makeDisplayThread()
                }
                1002 -> {
                    DB.updateAlarm(data)
                    makeDisplayThread()
                    renewAlarms()
                }
            }
        }
    }
    fun makeDisplayThread(){
        timeChecker.interrupt()
        timeChecker = TimeCounter(this, checkClosest())
        timeChecker.start()
    }
    private fun renewAlarms(){
        alarm_list.removeAllViews()
        DB.selectAlarms().use {
            while (it.moveToNext())
                ViewDrawer(this).addNewAlarmToLayout(DTO(it))
        }
        alarm_list.addView(ViewDrawer(this).addNewBtn())
    }
    private fun checkClosest() : Time {
        var closestTime = Time(LocalDateTime.now().plusYears(5),DTO(-1,"",-1,-1,-1,-1,-1,-1),Time.NO_ALARMS)
        val today = LocalDateTime.now().plusSeconds(1)
        DB.selectAlarms().use {
            while (it.moveToNext()) {
                val interval : Long = it.getLong(6)
                val setting = it.getInt(7)
                if (setting != setting or (1 shl 9)) continue
                if (setting == (setting shr 7) shl 7) continue
                var alarmTime = LocalDateTime.of(today.year, today.month, today.dayOfMonth,it.getInt(2), it.getInt(3),0)
                val alarmTimeEnd = LocalDateTime.of(today.year, today.month, today.dayOfMonth,it.getInt(4), it.getInt(5),0)
                if(alarmTime.isAfter(alarmTimeEnd) && today.isAfter(alarmTimeEnd)) // 알람시작시가 종료시보다 후인경우, 종료시는 다음날 그 시각으로 설정 && 현시각이 알람종료시 이후인 경우에만(ex. 현시 새벽1시, 알람새벽2시종료)
                    alarmTimeEnd.plusDays(1)
                if(alarmTimeEnd.isBefore(today)) //알람종료시가 현시각보다 전이면, 알람시각은 알람시작시+1일
                    alarmTime.plusDays(1)
                else //알람종료시가 현시각보다 뒤면, 1. 현시각은 알람시작시 이전이거나 2. 알람기간 내에 위치
                    if(alarmTime.isAfter(alarmTimeEnd)) // 알람종료시가 현시각 이후지만, 알람시작시가 어제인 경우 (ex. 현시 새벽1시, 알람종료시 새벽2시)
                        alarmTime = LocalDateTime.of(today.year, today.month, today.dayOfMonth,0, 0,0)
                while(alarmTime.isBefore(today)) // 알람시작시가 현시각보다 이전이면 (알람기간 내 위치)
                    alarmTime = alarmTime.plusMinutes(interval)

                if (alarmTime.isBefore(closestTime.time)) { //가장 가까운 알람시간 갱신
                    closestTime = Time(alarmTime, DTO(it), Time.ALARM_EXISTS)
                }

                val alarmIntent = Intent(this, AlarmReceiver::class.java)
                    .putExtra("num",it.getInt(0))
                    .putExtra("title",getString(R.string.noti_title))
                    .putExtra("content",getString(R.string.noti_content))
                //if (closestAlarmTitle.isNotEmpty()) alarmIntent.putExtra("title",closestAlarmTitle)
                val pended = PendingIntent.getBroadcast(this, it.getInt(0), alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                am.setAlarmClock(AlarmManager.AlarmClockInfo(System.currentTimeMillis()+ChronoUnit.MILLIS.between(today, closestTime.time),pended),pended)
                Log.v("###############################","알람 셋팅 closestTime: "+ closestTime+ " , 차이1 : "+ChronoUnit.MILLIS.between(today, closestTime.time))
            }
        }
        return closestTime
    }
    fun makeSwitch(data : DTO) : Switch {
        val params = LinearLayout.LayoutParams(
            (10*resources.displayMetrics.density+0.5f).toInt(),
            LinearLayout.LayoutParams.MATCH_PARENT
        ).apply {
            weight = 1f
            rightMargin = (5*resources.displayMetrics.density+0.5f).toInt()
        }
        val switch = Switch(this).apply {
            layoutParams = params
            if(data.settings == data.settings or (1 shl 9)) isChecked = true
            id = data.num
        }
        switch.setOnCheckedChangeListener { _, isNotChecked ->
            val pended = PendingIntent.getBroadcast(applicationContext, data.num, Intent(this, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
            am.cancel(pended)
            pended.cancel()
            data.settings = if (isNotChecked) data.settings or (1 shl 9) //off -> on
            else data.settings xor (1 shl 9) //on -> off
            DB.updateAlarm(data)
            makeDisplayThread()
            renewAlarms()
        }
        return switch
    }
    private fun moveNextDay(day : LocalDateTime, settings: Int):LocalDateTime{
        Log.v("@@@ moveNextDay function @@@",day.toString())
        when(day.dayOfWeek){
            DayOfWeek.MONDAY -> if (settings != settings or (1 shl 0)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.TUESDAY -> if (settings != settings or (1 shl 1)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.WEDNESDAY -> if (settings != settings or (1 shl 2)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.THURSDAY -> if (settings != settings or (1 shl 3)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.FRIDAY -> if (settings != settings or (1 shl 4)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.SATURDAY -> if (settings != settings or (1 shl 5)) return moveNextDay(day.plusDays(1), settings)
            DayOfWeek.SUNDAY -> if (settings != settings or (1 shl 6)) return moveNextDay(day.plusDays(1), settings)
        }
        return day
    }
    private fun reviseTime(time:Int) :String{
        return if(time<10) "0$time"
        else time.toString()
    }
    override fun onBackPressed() {
        BackPressHandler(this).onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("##################","onDestroy()")
    }
}
