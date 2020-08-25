package typebi.util.stralarm

import android.app.*
import android.content.Intent
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
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class MainActivity : AppCompatActivity() {
    private lateinit var mAdView : AdView
    @property:Suppress("PrivatePropertyName")
    private val DB : DBAccesser by lazy { DBAccesser(this) }
    private val am : AlarmManager by lazy { getSystemService(ALARM_SERVICE) as AlarmManager }
    private lateinit var timeChecker:TimeCounter
    private val snackBar by lazy {
        Snackbar.make(main_layout, "", Snackbar.LENGTH_LONG)
            .setActionTextColor(Color.WHITE)
            .setAction("EXIT"){
                finish()
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        //setTheme(R.style.AppTheme_Blue)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.admob)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        Log.v("@@@@@@@@@@@@@@@@@","onCreate() 실행")
        DB.createTable(getString(R.string.createTable))
        renewAlarms()
        testBtn.setOnClickListener{
            startActivity(Intent(this, ProgressPage::class.java))
        }
        setting_menu.setOnClickListener {
            registerForContextMenu(setting_menu)
            openContextMenu(setting_menu)
            unregisterForContextMenu(setting_menu)
        }
        timeChecker = TimeCounter(this, checkClosest())
        timeChecker.start()
        if (intent.getBooleanExtra("isDoze",false)) {
            timeChecker.interrupt()
            this.finish()
        }
    }
    fun makeDisplayThread(){
        timeChecker.interrupt()
        timeChecker = TimeCounter(this, checkClosest())
        timeChecker.start()
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
            R.id.action_settings_1 -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data!=null) {
            when (requestCode) {
                1001 -> {
                    alarm_list.removeView(alarm_list.children.last())
                    ViewDrawer().addNewAlarmToLayout(this, DB.insertAlarm(data, getString(R.string.selectLatest)))
                    alarm_list.addView(ViewDrawer().addNewBtn(this))
                    makeDisplayThread()
                }
                1002 -> {
                    DB.updateAlarm(data)
                    makeDisplayThread()
                    renewAlarms()
                }
            }
        }else if (resultCode == Activity.RESULT_CANCELED && data!=null){
            DB.deleteAlarm(data)
            val alarmIntent = Intent(this, AlarmReceiver::class.java)
            val pended = PendingIntent.getBroadcast(applicationContext, data.getIntExtra("num",-1), alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            am.cancel(pended)
            makeDisplayThread()
            renewAlarms()
        }
    }
    private fun renewAlarms(){
        alarm_list.removeAllViews()
        DB.selectAlarms().use {
            while (it.moveToNext())
                ViewDrawer().addNewAlarmToLayout(this, DTO(it))
        }
        alarm_list.addView(ViewDrawer().addNewBtn(this))
    }
    fun checkClosest() : Time {
        val closest = ClosestChecker(this).check(DB.selectAlarms())
        val now = LocalDateTime.now().plusSeconds(1)
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
            .putExtra("num",closest.data.num)
            .putExtra("title",getString(R.string.noti_title))
            .putExtra("content",getString(R.string.noti_content))
            .setAction(getString(R.string.noti_action_name))
        val pended = PendingIntent.getBroadcast(applicationContext, closest.data.num, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(System.currentTimeMillis()+ChronoUnit.MILLIS.between(now, closest.time),pended),pended)
        Log.v("###############################","알람 셋팅")
        return closest
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
    override fun onBackPressed() {
        if (snackBar.isShown) snackBar.dismiss()
        else snackBar.show()
    }
    override fun onDestroy() {
        super.onDestroy()
        timeChecker.interrupt()
    }
}
