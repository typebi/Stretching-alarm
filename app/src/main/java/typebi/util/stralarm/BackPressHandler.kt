package typebi.util.stralarm

import android.graphics.Color
import android.os.SystemClock
import android.view.View
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main.*

class BackPressHandler(private val activity: MainActivity){
    fun onBackPressed(){
        Snackbar.make(activity.main_layout, "", Snackbar.LENGTH_LONG)
            .setActionTextColor(Color.WHITE)
            .setAction("EXIT"){
                activity.finish()
                }
            .show();
    }
}