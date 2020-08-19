package typebi.util.stralarm

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import kotlinx.android.synthetic.main.progress_page.*

class ProgressPage() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        noti.cancel(intent.getIntExtra("notiId",1))
        setContentView(R.layout.progress_page)
        val vibe = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibeEffect = VibrationEffect.createWaveform(longArrayOf(0,500,500,250), 1)
        startBtn.setOnClickListener{
            startBtn.visibility = View.INVISIBLE
            val time : Int = intent.getIntExtra("time",15)
            val tick : Long = 1000 / (600 / time.toLong())
            time_remaining.text = time.toString()
            Thread(Runnable {
                while (progressBar.progress<600){
                    progressBar.setProgress(progressBar.progress + 1, true)
                    Thread.sleep(tick)
                }
                runOnUiThread {
                    exitBtn.visibility = View.VISIBLE
                    vibe.vibrate(vibeEffect)
                }
            }).start()
            Thread(Runnable {
                for (i in 0 .. time) {
                    time_remaining.text = (time - i).toString()
                    Thread.sleep(1000)
                }
            }).start()
        }
        exitBtn.setOnClickListener{
            vibe.cancel()
            finish()
        }
    }
}