package typebi.util.stralarm

import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import kotlinx.android.synthetic.main.alarm.view.*
import kotlinx.android.synthetic.main.content_main.*

class ViewDrawer {
    fun addNewBtn(main : MainActivity) : ImageButton {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (110*main.resources.displayMetrics.density+0.5f).toInt()
        )
        val dp = (10*main.resources.displayMetrics.density+0.5f).toInt()
        params.setMargins(0,dp,0,dp)
        val plusAlarmBtn = ImageButton(main).apply {
            layoutParams = params
            setBackgroundResource(R.drawable.border_layout)
            setImageResource(R.drawable.pointer_cell_large)
        }
        val intentFromNewbtn = Intent(main, AddAlarm::class.java).putExtra("isNew",true)
        plusAlarmBtn.setOnClickListener {
            main.startActivityForResult(intentFromNewbtn,1001)
        }
        return plusAlarmBtn
    }
    fun addNewAlarmToLayout(mainActivity : MainActivity, data : DTO){
        val fixedSh = if (data.startHour>=12) "PM"+(data.startHour-12) else "AM"+data.startHour
        val fixedEh = if (data.endHour>=12) "PM"+(data.endHour-12) else "AM"+data.endHour
        val content = "$fixedSh:"+reviseTime(data.startMin)+" ~ $fixedEh:"+reviseTime(data.endMin)+"  간격:"+data.interval+"분\n월화수목금토일"
        val layoutInflater = mainActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.alarm,mainActivity.alarm_list,true)
        if (data.name.isNotEmpty()) mainActivity.alarm_list.children.last().innerLayout.alarm_name.text = data.name
        mainActivity.alarm_list.children.last().innerLayout.content.text = setStringStyle(mainActivity, content, data.settings)
        mainActivity.alarm_list.children.last().outerLayout.addView(mainActivity.makeSwitch(data))
        val intent = Intent(mainActivity, AddAlarm::class.java).apply {
            putExtra("num", data.num)
            putExtra("name", data.name)
            putExtra("sh", data.startHour)
            putExtra("sm", data.startMin)
            putExtra("eh", data.endHour)
            putExtra("em", data.endMin)
            putExtra("intvl", data.interval)
            putExtra("settings", data.settings)
        }
        mainActivity.alarm_list.children.last().innerLayout.setOnClickListener{
            mainActivity.startActivityForResult(intent, 1002)
        }
    }
    private fun setStringStyle(mainActivity : MainActivity, text : String, settings: Int) : SpannableString {
        val spannableString = SpannableString(text)
        if (text.contains("AM")) {
            spannableString.setSpan(RelativeSizeSpan(0.5f), text.indexOf("AM"), text.indexOf("AM") + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (text.indexOf("AM")!=text.lastIndexOf("AM"))
                spannableString.setSpan(RelativeSizeSpan(0.5f), text.lastIndexOf("AM"), text.lastIndexOf("AM") + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (text.contains("PM")) {
            spannableString.setSpan(RelativeSizeSpan(0.5f), text.indexOf("PM"), text.indexOf("PM") + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (text.indexOf("PM")!=text.lastIndexOf("PM"))
                spannableString.setSpan(RelativeSizeSpan(0.5f), text.lastIndexOf("PM"), text.lastIndexOf("PM") + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        spannableString.setSpan(RelativeSizeSpan(0.5f), text.indexOf("월화수목금토일"), text.indexOf("월화수목금토일") + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(RelativeSizeSpan(0.5f), text.lastIndexOf("간격"), text.lastIndexOf("간격") + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(RelativeSizeSpan(0.5f), text.lastIndexOf("분"), text.lastIndexOf("분") + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(mainActivity.getColor(R.color.textColor_4)), text.indexOf("월화수목금토일"), text.indexOf("월화수목금토일") + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        for (i in 0 .. 6) {
            if (settings == settings or (1 shl i)) {
                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),text.length - (7-i), text.length - (7-i)+1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.setSpan(RelativeSizeSpan(1.2f), text.length - (7-i), text.length - (7-i)+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.setSpan(ForegroundColorSpan(mainActivity.getColor(R.color.textColor_1)), text.length - (7-i), text.length - (7-i)+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return spannableString
    }
    private fun reviseTime(time:Int) :String{
        return if(time<10) "0$time"
        else time.toString()
    }
}