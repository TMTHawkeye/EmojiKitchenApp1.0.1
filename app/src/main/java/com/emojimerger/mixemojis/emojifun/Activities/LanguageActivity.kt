package com.emojimerger.mixemojis.emojifun.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.emojimerger.mixemojis.emojifun.databinding.ActivityLanguageBinding
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.IS_LANGUAGE_SELECTED
import com.emojimerger.mixemojis.emojifun.modelClasses.fileDetails
import io.paperdb.Paper

class LanguageActivity : ComponentActivity() {
    lateinit var binding: ActivityLanguageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Paper.book().write<Boolean>(IS_LANGUAGE_SELECTED, true)
    }
}