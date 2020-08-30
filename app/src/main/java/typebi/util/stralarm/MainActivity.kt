package typebi.util.stralarm

import android.annotation.SuppressLint
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
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    @property:Suppress("PrivatePropertyName")
    private val DB : DBAccesser by lazy { DBAccesser(application) }
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
        Log.v("@@@@@@@@@@@@@@@@@","onCreate() 실행")
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        //admob_1.loadAd(adRequest)
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
        timeChecker = TimeCounter(this, ClosestChecker(application).setAlarm())
        timeChecker.start()
        if (intent.getBooleanExtra("isDoze",false)) {
            timeChecker.interrupt()
            this.finish()
        }
    }
    fun makeDisplayThread(){
        timeChecker.interrupt()
        timeChecker = TimeCounter(this, ClosestChecker(application).setAlarm())
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
            ClosestChecker(application).cancel(data.getIntExtra("num",-1), data.getStringExtra("name"))
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
    @SuppressLint("UseSwitchCompatOrMaterialCode")
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
            ClosestChecker(application).cancel(data.num, data.name)
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
