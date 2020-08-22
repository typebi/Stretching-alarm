package typebi.util.stralarm

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.add_alarm.*

class AddAlarm : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_alarm)
        time_interval.minValue=1
        time_interval.maxValue=1440
        time_interval.value=intent.getIntExtra("intvl",15)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            time_start.hour = intent.getIntExtra("sh",9)
            time_start.minute = intent.getIntExtra("sm",0)
            time_end.hour = intent.getIntExtra("eh",18)
            time_end.minute = intent.getIntExtra("em",0)
        }
        val settings = intent.getIntExtra("settings",0)
        val settingViews = arrayOf(day1, day2, day3, day4, day5, day6, day7, switch_ring, switch_vibe)
        if(intent.getBooleanExtra("isNew", false)) {
            btn_delete.visibility = View.INVISIBLE
            switch_vibe.isChecked =  true
            for (i in 0 .. 4)
                settingViews[i].isChecked = true
        }else
            for (i in 0 .. 8)
                if (settings == settings or (1 shl i))
                    settingViews[i].isChecked = true
        val num = intent.getIntExtra("num",0)
        if (intent.getStringExtra("name") !=null)
            alarm_name.setText(intent.getStringExtra("name"), TextView.BufferType.EDITABLE)
        btn_add.setOnClickListener {
            val name = alarm_name.text.toString()
            var setting = 0b0
            for (compo in settingViews){
                if (compo.isChecked){
                    val idx = settingViews.indexOf(compo)
                    setting = setting or (1 shl idx)
                }
            }
            setting = setting or (1 shl 9)
            Log.v("#####################",setting.toString(2))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.putExtra("isDelete",false)
                intent.putExtra("num", num)
                intent.putExtra("name", name)
                intent.putExtra("startHour", time_start.hour)
                intent.putExtra("startMin", time_start.minute)
                intent.putExtra("endHour", time_end.hour)
                intent.putExtra("endMin", time_end.minute)
                intent.putExtra("intvl", time_interval.value)
                intent.putExtra("settings", setting)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        btn_delete.setOnClickListener {
            intent.putExtra("isDelete",true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}