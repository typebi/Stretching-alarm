package typebi.util.stralarm

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.add_alarm.*

class AddAlarm : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        //setTheme(R.style.AppTheme_Blue)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_alarm)
        val adRequest = AdRequest.Builder().build()
        //admob_2.loadAd(adRequest)
        time_interval.minValue = 1
        time_interval.maxValue = 1440
        time_interval.value = intent.getIntExtra("intvl",15)
        time_start.hour = intent.getIntExtra("sh",9)
        time_start.minute = intent.getIntExtra("sm",0)
        time_end.hour = intent.getIntExtra("eh",18)
        time_end.minute = intent.getIntExtra("em",0)
        var settings = intent.getIntExtra("settings",0)

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
        if (intent.getStringExtra("name")!=null)
            alarm_name.setText(intent.getStringExtra("name"), TextView.BufferType.EDITABLE)
        btn_add.setOnClickListener {
            settings = 0
            for (set in settingViews)
                if (set.isChecked)
                    settings = settings or (1 shl settingViews.indexOf(set))
            settings = settings or (1 shl 9)
            intent.putExtra("isDelete",false)
                .putExtra("num", num)
                .putExtra("name", alarm_name.text.toString())
                .putExtra("startHour", time_start.hour)
                .putExtra("startMin", time_start.minute)
                .putExtra("endHour", time_end.hour)
                .putExtra("endMin", time_end.minute)
                .putExtra("intvl", time_interval.value)
                .putExtra("settings", settings)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        btn_delete.setOnClickListener {
            intent.putExtra("num", num)
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }
}