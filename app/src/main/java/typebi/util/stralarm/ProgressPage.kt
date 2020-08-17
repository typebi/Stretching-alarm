package typebi.util.stralarm

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.progress_page.*

class ProgressPage() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progress_page)
        startBtn.setOnClickListener{
            if (progressBar.progress==60) progressBar.setProgress(0, true)
            else progressBar.setProgress(progressBar.progress+1,true)
        }
    }
}