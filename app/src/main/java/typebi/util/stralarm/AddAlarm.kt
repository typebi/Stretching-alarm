package typebi.util.stralarm

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
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
        time_interval.maxValue=86399
        time_interval.value=intent.getIntExtra("intvl",15)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            time_start.hour = intent.getIntExtra("sh",9)
            time_start.minute = intent.getIntExtra("sm",0)
            time_end.hour = intent.getIntExtra("eh",18)
            time_end.minute = intent.getIntExtra("em",0)
        }
        if(intent.getBooleanExtra("isNew", false))
            btn_delete.visibility = View.INVISIBLE;
        switch_vibe.isChecked =  true
        val num = intent.getIntExtra("num",0)
        var name = intent.getStringExtra("name")
        if (name!=null)
            alarm_name.setText(name, TextView.BufferType.EDITABLE)
        btn_add.setOnClickListener {
            name = alarm_name.text.toString()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.putExtra("isDelete",false)
                intent.putExtra("num", num)
                intent.putExtra("name", name)
                intent.putExtra("startHour", time_start.hour)
                intent.putExtra("startMin", time_start.minute)
                intent.putExtra("endHour", time_end.hour)
                intent.putExtra("endMin", time_end.minute)
                intent.putExtra("intvl", time_interval.value)
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