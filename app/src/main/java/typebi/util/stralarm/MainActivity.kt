package typebi.util.stralarm

import android.app.*
import android.content.ContentValues
import android.content.Intent
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
import kotlinx.android.synthetic.main.content_main.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class MainActivity : AppCompatActivity() {
    private lateinit var db:SQLiteDatabase
    private lateinit var timeChecker:TimeChecker
    private val am : AlarmManager by lazy {
        getSystemService(ALARM_SERVICE) as AlarmManager
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = openOrCreateDatabase("stretchingAlarm",MODE_PRIVATE, null)
        db.execSQL(getString(R.string.createTable))

        timeChecker = TimeChecker(this, checkClosest())
        timeChecker.start()
        renewAlarms()
        testBtn.setOnClickListener{
            startActivity(Intent(this, ProgressPage::class.java))
        }
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
            var num = data.getIntExtra("num", 0)
            if (!isDelete) {
                val name = data.getStringExtra("name")
                val sh = data.getIntExtra("startHour", 0)
                val sm = data.getIntExtra("startMin", 0)
                val eh = data.getIntExtra("endHour", 0)
                val em = data.getIntExtra("endMin", 0)
                val intvl = data.getIntExtra("intvl", 0)
                when (requestCode) {
                    1001 -> {
                        db.insert("STRALARM","SETTINGS", makeDataRow(name, sh, sm, eh, em, intvl))
                        val row = db.rawQuery(getString(R.string.selectLatest), null)
                        row.moveToNext()
                        num = row.getInt(0)
                        row.close()
                        //adsense.text = name + sh.toString()
                        alarm_list.removeView(alarm_list.children.last())
                        alarm_list.addView( makeNewAlarm(num, name, sh.toString(), reviseTime(sm), eh.toString(), reviseTime(em), intvl.toString() ) )
                        alarm_list.addView(addNewBtn())
                        makeDisplayThread()
                    }
                    1002 -> {
                        db.update("STRALARM", makeDataRow(name, sh, sm, eh, em, intvl), "NUM = ?", arrayOf(num.toString()))
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
        timeChecker = TimeChecker(this, checkClosest())
        timeChecker.start()
    }
    private fun makeNewAlarm(num:Int, name:String?, sh:String, sm:String, eh:String, em:String,intvl:String): TextView{
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (110*resources.displayMetrics.density+0.5f).toInt()
        )
        val dp = (10*resources.displayMetrics.density+0.5f).toInt()
        params.setMargins(0,dp,0,dp)

        val alarm = Button(this)
        alarm.setBackgroundResource(R.drawable.border_layout)
        alarm.layoutParams = params
        alarm.gravity = Gravity.CENTER_VERTICAL
        var content = "$sh:$sm ~ $eh:$em  / $intvl m\n월화수목금토일"
        if (name!=null && name.isNotEmpty()) content = "$name\n$content"
        alarm.text = content
        alarm.textSize = 25.toFloat()
        alarm.setTextColor(Color.parseColor("#000000"))
        alarm.id = num
        val intent = Intent(this, AddAlarm::class.java)
        intent.putExtra("num", alarm.id)
        .putExtra("name", name)
        .putExtra("sh", sh.toInt())
        .putExtra("sm", sm.toInt())
        .putExtra("eh", eh.toInt())
        .putExtra("em", em.toInt())
        .putExtra("intvl", intvl.toInt())
        alarm.setOnClickListener{
            startActivityForResult(intent, 1002)
        }
        return alarm
    }
    private fun addNewBtn() :ImageButton{
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (125*resources.displayMetrics.density+0.5f).toInt()
        )
        val dp = (10*resources.displayMetrics.density+0.5f).toInt()
        params.setMargins(0,dp,0,dp)
        val plusAlarmBtn = ImageButton(this)
        plusAlarmBtn.layoutParams = params
        plusAlarmBtn.setBackgroundResource(R.drawable.border_layout)
        plusAlarmBtn.setImageResource(android.R.drawable.ic_input_add)
        val intentFromNewbtn = Intent(this, AddAlarm::class.java).putExtra("isNew",true)
        plusAlarmBtn.setOnClickListener {
            startActivityForResult(intentFromNewbtn,1001)
        }
        return plusAlarmBtn
    }
    private fun renewAlarms(){
        alarm_list.removeAllViews()
        val alarms = db.rawQuery("select * from STRALARM", null)
        if(alarms.count!=0) {
            for (i in 1..alarms.count) {
                alarms.moveToNext()
                alarm_list.addView(
                    makeNewAlarm(
                        alarms.getInt(0),
                        alarms.getString(1),
                        alarms.getInt(2).toString(),
                        reviseTime(alarms.getInt(3)),
                        alarms.getInt(4).toString(),
                        reviseTime(alarms.getInt(5)),
                        alarms.getInt(6).toString()
                    )
                )
            }
        }
        alarms.close()
        alarm_list.addView(addNewBtn())
    }
    private fun makeDataRow(name:String?, sh:Int, sm:Int, eh:Int, em:Int,intvl:Int) :ContentValues{
        val cv = ContentValues()
        cv.put("NAME", name)
        cv.put("START_H", sh)
        cv.put("START_M", sm)
        cv.put("END_H", eh)
        cv.put("END_M", em)
        cv.put("INTERVAL", intvl)
        return cv
    }
    private fun checkClosest() : Time {
        var closestTime = LocalDateTime.now().plusYears(1)
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

                if (alarmTime.isBefore(closestTime)) //가장 가까운 알람시간 갱신
                    closestTime = alarmTime
            }
            val alarmIntent = Intent(this, AlarmReceiver::class.java).putExtra("num",alarms.getInt(0))
            val pender = PendingIntent.getBroadcast(this, alarms.getInt(0), alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            am.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+ChronoUnit.MILLIS.between(today, closestTime), pender)
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
}
