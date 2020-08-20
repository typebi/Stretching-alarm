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
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.content_main.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class MainActivity : AppCompatActivity() {
    private lateinit var db:SQLiteDatabase
    private lateinit var timeChecker:TimeCounter
    lateinit var mAdView : AdView
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
        db = openOrCreateDatabase("stretchingAlarm",MODE_PRIVATE, null)
        db.execSQL(getString(R.string.createTable))
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
            if (!isDelete) {
                val name = data.getStringExtra("name")
                val sh = data.getIntExtra("startHour", 0)
                val sm = data.getIntExtra("startMin", 0)
                val eh = data.getIntExtra("endHour", 0)
                val em = data.getIntExtra("endMin", 0)
                val intvl = data.getIntExtra("intvl", 0)
                val settings = data.getIntExtra("settings", 0)
                when (requestCode) {
                    1001 -> {
                        db.insert("STRALARM",null, makeDataRow(name, sh, sm, eh, em, intvl, settings))
                        val justInsertedData = db.rawQuery(getString(R.string.selectLatest), null)
                        justInsertedData.moveToNext()
                        alarm_list.removeView(alarm_list.children.last())
                        alarm_list.addView(makeNewAlarm(justInsertedData))
                        alarm_list.addView(addNewBtn())
                        makeDisplayThread()
                        justInsertedData.close()
                    }
                    1002 -> {
                        db.update("STRALARM", makeDataRow(name, sh, sm, eh, em, intvl, settings), "NUM = ?", arrayOf(num.toString()))
                        makeDisplayThread()
                        renewAlarms()
                    }
                }
            }else{
                db.execSQL("delete from STRALARM where num=$num")
                val alarmIntent = Intent(this, AlarmReceiver::class.java).putExtra("num",num)
                val pender = PendingIntent.getBroadcast(this, num, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                am.cancel(pender)
                makeDisplayThread()
                renewAlarms()
            }
        }
    }
    fun makeDisplayThread(){
        timeChecker.interrupt()
        timeChecker = TimeCounter(this, checkClosest())
        timeChecker.start()
    }
    private fun makeNewAlarm(data : Cursor): TextView{
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (110*resources.displayMetrics.density+0.5f).toInt())
        val dp = (10*resources.displayMetrics.density+0.5f).toInt()
        params.setMargins(0,dp,0,dp)
        val fixedSh = if (data.getInt(2)>=12) "PM "+(data.getInt(2)-12)
        else "AM "+data.getInt(2)
        val fixedEh = if (data.getInt(4)>=12) "PM "+(data.getInt(4)-12)
        else "AM "+data.getInt(4)
        var content = "$fixedSh:"+reviseTime(data.getInt(3))+" ~ $fixedEh:"+reviseTime(data.getInt(5))+"  / "+data.getInt(6)+" m\n월화수목금토일"
        val alarm = Button(this).apply {
            setBackgroundResource(R.drawable.border_layout)
            layoutParams = params
            gravity = Gravity.CENTER_VERTICAL
        }

        if (data.getString(1).isNotEmpty()) content = data.getString(1)+"\n$content"
        alarm.text = content
        alarm.textSize = 25.toFloat()
        alarm.setTextColor(Color.parseColor("#000000"))
        alarm.id = data.getInt(0)
        val intent = Intent(this, AddAlarm::class.java).apply {
            putExtra("num", alarm.id)
            putExtra("name", data.getString(1))
            putExtra("sh", data.getInt(2))
            putExtra("sm", data.getInt(3))
            putExtra("eh", data.getInt(4))
            putExtra("em", data.getInt(5))
            putExtra("intvl", data.getInt(6))
            putExtra("settings", data.getInt(7))
        }
        alarm.setOnClickListener{
            startActivityForResult(intent, 1002)
        }
        return alarm
    }
    private fun addNewBtn() :ImageButton{
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (110*resources.displayMetrics.density+0.5f).toInt()
        )
        val dp = (10*resources.displayMetrics.density+0.5f).toInt()
        params.setMargins(0,dp,0,dp)
        val plusAlarmBtn = ImageButton(this)
        plusAlarmBtn.layoutParams = params
        plusAlarmBtn.setBackgroundResource(R.drawable.border_layout)
        plusAlarmBtn.setImageResource(R.drawable.pointer_cell_large)
        val intentFromNewbtn = Intent(this, AddAlarm::class.java).putExtra("isNew",true)
        plusAlarmBtn.setOnClickListener {
            startActivityForResult(intentFromNewbtn,1001)
        }
        return plusAlarmBtn
    }
    private fun renewAlarms(){
        alarm_list.removeAllViews()
        val alarms = db.rawQuery("select * from STRALARM", null)
        if(alarms.count!=0)
            for (i in 1..alarms.count) {
                alarms.moveToNext()
                alarm_list.addView(makeNewAlarm(alarms))
            }
        alarms.close()
        alarm_list.addView(addNewBtn())
    }
    private fun makeDataRow(name:String?, sh:Int, sm:Int, eh:Int, em:Int,intvl:Int, settings:Int) :ContentValues{
        val cv = ContentValues()
        cv.put("NAME", name)
        cv.put("START_H", sh)
        cv.put("START_M", sm)
        cv.put("END_H", eh)
        cv.put("END_M", em)
        cv.put("INTERVAL", intvl)
        cv.put("SETTINGS",settings)
        return cv
    }
    private fun checkClosest() : Time {
        var closestTime = LocalDateTime.now().plusYears(5)
        lateinit var closestAlarmTitle : String
        val today = LocalDateTime.now().plusSeconds(1)
        val alarms = db.rawQuery("select * from STRALARM", null)
        if (alarms.count != 0) {
            for (i in 1..alarms.count) {
                alarms.moveToNext()
                val interval : Long = alarms.getLong(6)
                var alarmTime = LocalDateTime.of(today.year, today.month, today.dayOfMonth,alarms.getInt(2), alarms.getInt(3),0)
                val alarmTimeEnd = LocalDateTime.of(today.year, today.month, today.dayOfMonth,alarms.getInt(4), alarms.getInt(5),0)
                if(alarmTime.isAfter(alarmTimeEnd) && today.isAfter(alarmTimeEnd)) // 알람시작시가 종료시보다 후인경우, 종료시는 다음날 그 시각으로 설정 && 현시각이 알람종료시 이후인 경우에만(ex. 현시 새벽1시, 알람새벽2시종료)
                    alarmTimeEnd.plusDays(1)
                if(alarmTimeEnd.isBefore(today)) //알람종료시가 현시각보다 전이면, 알람시각은 알람시작시+1일
                    alarmTime.plusDays(1)
                else //알람종료시가 현시각보다 뒤면, 1. 현시각은 알람시작시 이전이거나 2. 알람기간 내에 위치
                    if(alarmTime.isAfter(alarmTimeEnd)) // 알람종료시가 현시각 이후지만, 알람시작시가 어제인 경우 (ex. 현시 새벽1시, 알람종료시 새벽2시)
                        alarmTime = LocalDateTime.of(today.year, today.month, today.dayOfMonth,0, 0,0)
                    while(alarmTime.isBefore(today)) // 알람시작시가 현시각보다 이전이면 (알람기간 내 위치)
                        alarmTime = alarmTime.plusMinutes(interval)

                if (alarmTime.isBefore(closestTime)) { //가장 가까운 알람시간 갱신
                    closestTime = alarmTime
                    closestAlarmTitle = alarms.getString(1)
                }
            }
            val alarmIntent = Intent(this, AlarmReceiver::class.java)
                .putExtra("num",alarms.getInt(0))
                .putExtra("title",getString(R.string.noti_title))
                .putExtra("content",getString(R.string.noti_content))
            if (closestAlarmTitle.isNotEmpty()) alarmIntent.putExtra("title",closestAlarmTitle)
            val pender = PendingIntent.getBroadcast(this, alarms.getInt(0), alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)

//            val alarmInfo = JobInfo.Builder(alarms.getInt(0), ComponentName("typebi.util.stralarm", "typebi.util.stralarm.AlarmSchedulerService")).apply {
//                setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                setPersisted(true)
//                setMinimumLatency(TimeUnit.MILLISECONDS.toMillis(ChronoUnit.MILLIS.between(today, closestTime)))
//                setOverrideDeadline(TimeUnit.MILLISECONDS.toMillis(ChronoUnit.MILLIS.between(today, closestTime)+10))
//            }
//            val js : JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
//            js.schedule(alarmInfo.build())

//            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+ChronoUnit.MILLIS.between(today, closestTime), pender)
            am.setAlarmClock(
                AlarmManager.AlarmClockInfo(System.currentTimeMillis()+ChronoUnit.MILLIS.between(today, closestTime),pender),
                pender)
            Log.v("###############################","알람 셋팅 closestTime: "+ closestTime+ " , 차이1 : "+ChronoUnit.MILLIS.between(today, closestTime))
            alarms.close()
            return Time(closestTime, Time.ALARM_EXISTS)
        }else{ //3. 알람이 아예 없는경우
            alarms.close()
            return Time(today, Time.NO_ALARMS)
        }
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
