package typebi.util.stralarm

import android.app.*
import android.content.Intent
import android.database.Cursor
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class MainActivity : AppCompatActivity() {
    private lateinit var timeChecker:TimeCounter
    private lateinit var mAdView : AdView
    private val am : AlarmManager by lazy { getSystemService(ALARM_SERVICE) as AlarmManager }
    private val DB : DBAccesser by lazy { DBAccesser(openOrCreateDatabase("stretchingAlarm",MODE_PRIVATE, null)) }
    private var i : Int = 0
    private val snackbar by lazy {
        Snackbar.make(this.main_layout, "", Snackbar.LENGTH_LONG)
        .setActionTextColor(Color.WHITE)
        .setAction("EXIT"){ this.finish() }
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
        timeChecker = TimeCounter(this, checkClosest(DB.selectAlarms()), i++).apply { isDaemon = true }
        timeChecker.start()
        renewAlarms()
        testBtn.setOnClickListener{
            startActivity(Intent(this, ProgressPage::class.java))
        }
        setting_menu.setOnClickListener {
            registerForContextMenu(setting_menu)
            openContextMenu(setting_menu)
            unregisterForContextMenu(setting_menu)
        }
        if (intent.getBooleanExtra("isDoze",false)) {
            timeChecker.interrupt()
            this.finish()
        }
    }
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
    }
    override fun onContextItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (resultCode != Activity.RESULT_OK || intentData==null) {
            Log.v("Error in onActivityResult : ", "resultCode != Activity.RESULT_OK  or  intentData==null")
            return
        }
        if (intentData.getBooleanExtra("isDelete",false)){
            DB.deleteAlarm(intentData)
            val pended = PendingIntent.getBroadcast(applicationContext,intentData.getIntExtra("num", 0),Intent(this, AlarmReceiver::class.java).setAction("STRETCHING_TIME"),PendingIntent.FLAG_CANCEL_CURRENT)
            am.cancel(pended)
            pended.cancel()
            makeDisplayThread()
            renewAlarms()
            return
        }
        when (requestCode) {
            1001 -> {
                alarm_list.removeView(alarm_list.children.last())
                ViewDrawer(this).addNewAlarmToLayout(DB.insertAlarm(intentData, getString(R.string.selectLatest)))
                alarm_list.addView(ViewDrawer(this).addNewBtn())
                makeDisplayThread()
            }
            1002 -> {
                DB.updateAlarm(intentData)
                makeDisplayThread()
                renewAlarms()
            }
        }
    }
    fun makeDisplayThread(){
        timeChecker.interrupt()
        Log.v("$$$$$$$$$$$$$$$$$$$$$$$$$$$", "스레드 중지 중")
        timeChecker = TimeCounter(this, checkClosest(DB.selectAlarms()), i++)
        timeChecker.start()
    }
    fun makeSwitch(data : DTO) : Switch{
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
            val pended = PendingIntent.getBroadcast(applicationContext, data.num, Intent(this, AlarmReceiver::class.java).setAction("STRETCHING_TIME"), PendingIntent.FLAG_UPDATE_CURRENT)
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
    private fun renewAlarms(){
        alarm_list.removeAllViews()
        DB.selectAlarms().use {alarms ->
            while (alarms.moveToNext())
                ViewDrawer(this).addNewAlarmToLayout(DTO(alarms))
        }
        alarm_list.addView(ViewDrawer(this).addNewBtn())
    }
    private fun checkClosest(alarmList : Cursor) : Time {
        var closestTime = Time(LocalDateTime.now().plusYears(5), DTO(-1,"",-1,-1,-1,-1,-1,-1), Time.NO_ALARMS)
        val now = LocalDateTime.now().plusSeconds(1)
        alarmList.use {
            while (alarmList.moveToNext()) {
                val interval = alarmList.getLong(6)
                val setting = alarmList.getInt(7)
                if (setting != setting or (1 shl 9)) continue
                if (setting == (setting shr 7) shl 7) continue
                var startTime = moveNextDay(LocalDateTime.of(now.year,now.month,now.dayOfMonth,alarmList.getInt(2),alarmList.getInt(3),0),setting)
                var endTime = moveNextDay(LocalDateTime.of(now.year,now.month,now.dayOfMonth,alarmList.getInt(4),alarmList.getInt(5),0), setting)
                if(startTime.isEqual(endTime)) endTime = endTime.plusDays(1)
                if (now.plusDays(1).isBefore(startTime)){
                    closestTime = Time(startTime,DTO(alarmList),Time.ALARM_EXISTS)
                    continue
                }
                Log.v("@@@ checkClosest @@@","checkClosest 1")
                if (startTime.isAfter(endTime))//자정에 걸쳐있는지 여부로 분기
                    if (now.isBefore(endTime))
                        startTime = startTime.minusDays(1)
                    else
                        endTime = endTime.plusDays(1)
                Log.v("@@@ checkClosest @@@","checkClosest 2")
                //시작시 이전 |--v--(-----)-----|
                if (now.isBefore(startTime))
                    if(startTime.isBefore(closestTime.time)) {
                        Log.v("#########1111###########","시작시 이전 "+startTime.toString())
                        closestTime = Time(startTime,DTO(alarmList),Time.ALARM_EXISTS)
                        continue
                    }
                Log.v("@@@ checkClosest @@@","checkClosest 3")
                //시작시 이후 |-----(--v--)-----|
                if (now.isAfter(startTime) && now.isBefore(endTime)) {
                    Log.v("@@@ moveNextDay @@@","시작시 이후")
                    while (startTime.isBefore(now))
                        startTime = startTime.plusMinutes(interval)
                    Log.v("#########2222###########", "시작이후 종료이전 "+startTime.toString())
                    if (startTime.isAfter(endTime)) startTime = moveNextDay(startTime, setting)
                    Log.v("#########2222###########", "시작이후 종료이전 "+startTime.toString())
                    if (startTime.isBefore(closestTime.time)) {
                        closestTime = Time(startTime, DTO(alarmList), Time.ALARM_EXISTS)
                        continue
                    }
                }
                //종료시 이후 |-----(-----)--v--|
                startTime = moveNextDay(startTime, setting)
                Log.v("#########3333###########","종료시 이후 "+startTime.toString())
                if(startTime.isBefore(closestTime.time)) {
                    closestTime = Time(startTime,DTO(alarmList),Time.ALARM_EXISTS)
                    continue
                }
            }
        }
        if (closestTime.state==Time.ALARM_EXISTS) setAlarm(closestTime)
        return closestTime
    }
    private fun setAlarm(closest : Time){
        val title = if (closest.data.name.isNotEmpty()) closest.data.name else getString(R.string.noti_title)
        val vibration = closest.data.settings == closest.data.settings or (1 shl 8)
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
            .putExtra("num", closest.data.num)
            .putExtra("title",title)
            .putExtra("content", getString(R.string.noti_content))
            .setAction("STRETCHING_TIME")
            .putExtra("vibration", vibration)
        val pended = PendingIntent.getBroadcast(applicationContext,closest.data.num,alarmIntent,PendingIntent.FLAG_CANCEL_CURRENT)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(System.currentTimeMillis() + ChronoUnit.MILLIS.between(LocalDateTime.now(),closest.time), pended),pended)
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
    override fun onBackPressed() {
        if (snackbar.isShown) snackbar.dismiss()
        else snackbar.show()
    }
    override fun onDestroy() {
        super.onDestroy()
        timeChecker.interrupt()
    }
}
