package typebi.util.stralarm

import android.graphics.Color
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main.*

class BackPressHandler(private val main: MainActivity){
    private val snackbar = Snackbar.make(main.main_layout, "", Snackbar.LENGTH_LONG)
        .setActionTextColor(Color.WHITE)
        .setAction("EXIT"){
            main.finish()
        }
    fun showSnackbar(){
        if (snackbar.isShown) snackbar.dismiss()
        else snackbar.show()
    }
}