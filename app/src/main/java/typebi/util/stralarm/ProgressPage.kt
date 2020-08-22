package typebi.util.stralarm

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.progress_page.*

class ProgressPage : AppCompatActivity() {
    private val vibe by lazy {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    private val vibeEffect = VibrationEffect.createWaveform(longArrayOf(500,1000,500,1000), 0)
    private var vibeFlag = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vibeFlag = intent.getBooleanExtra("vibration",true)
        Log.v("##################", "ProgressPage vibeFlag : "+vibeFlag)
        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        noti.cancel(intent.getIntExtra("notiId",1))
        setContentView(R.layout.progress_page)
        start_anim_imageview.startAnimation(AnimationUtils.loadAnimation(this, R.anim.progress_bar_circle_anim))
        startBtn.setOnClickListener{
            startBtn.visibility = View.INVISIBLE
            start_anim_imageview.clearAnimation()
            start_anim_imageview.visibility = View.INVISIBLE
            val time : Int = intent.getIntExtra("time",15)
            val tick : Long = 1000 / (600 / time.toLong())
            time_remaining.text = time.toString()
            Thread {
                while (progressBar.progress < 600) {
                    progressBar.setProgress(progressBar.progress + 1, false)
                    Thread.sleep(tick)
                }
                if (vibeFlag) vibe.vibrate(vibeEffect)
            }.start()
            Thread{
                for (i in 0 .. time) {
                    time_remaining.text = (time - i).toString()
                    Thread.sleep(1000)
                }
            }.start()
        }
        exitBtn.setOnClickListener{
            vibe.cancel()
            finish()
        }
    }
    override fun onBackPressed(){
    }
    override fun onDestroy() {
        super.onDestroy()
        vibe.cancel()
        vibeFlag = false
    }
}