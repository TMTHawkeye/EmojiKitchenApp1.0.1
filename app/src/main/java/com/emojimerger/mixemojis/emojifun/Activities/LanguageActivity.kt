package com.emojimerger.mixemojis.emojifun.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.emojimerger.mixemojis.emojifun.databinding.ActivityLanguageBinding

class LanguageActivity : ComponentActivity() {
    lateinit var binding: ActivityLanguageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)




    }
}