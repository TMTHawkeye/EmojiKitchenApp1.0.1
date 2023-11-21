package com.emojimerger.mixemojis.emojifun.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.emojimerger.mixemojis.emojifun.R


open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

       hideNavBar()
    }

    override fun onResume() {
        super.onResume()
        hideNavBar()
    }

    override fun onPause() {
        super.onPause()
        hideNavBar()
    }
    fun hideNavBar(){
        val controller = ViewCompat.getWindowInsetsController(
            window.decorView
        ) ?: return

        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}