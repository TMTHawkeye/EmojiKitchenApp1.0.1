package com.emojimerger.mixemojis.emojifun.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ads.control.ads.AperoAd
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.adapters.CreationAdapter
import com.emojimerger.mixemojis.emojifun.databinding.ActivityMyCreationBinding
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.isInternetAvailable
import com.emojimerger.mixemojis.emojifun.repositories.emojisRepository
import com.emojimerger.mixemojis.emojifun.viewModelFactories.MainViewModelFactory
import com.emojimerger.mixemojis.emojifun.viewmodels.MainViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MyCreationActivity :  BaseActivity() {
    lateinit var binding: ActivityMyCreationBinding
    lateinit var viewModel: MainViewModel
    val mExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    val mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCreationBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initComponents()

        viewModel.getListOfFilesFromInternalStorage(getString(R.string.my_creationFolderName)) {
            if(it.size==0) {
                binding.linearNoItem.visibility = View.VISIBLE
                binding.ageBanner.visibility=View.GONE
            } else {
                loadBanner()
                val adapter = CreationAdapter(
                    this@MyCreationActivity,
                    it,
                    getString(R.string.mycreation)
                )
                binding.collectionRV.layoutManager = GridLayoutManager(this, 3)
                binding.collectionRV.adapter = adapter
            }

        }

        binding.relativeBack.setOnClickListener {
            finish()
        }

        binding.cardCreateEmoji.setOnClickListener {
            startActivity(Intent(this,MixEmojiActivity::class.java))
            finish()

        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                finish()
            }
        })
    }

    fun loadBanner() {
        if(isInternetAvailable()) {
            AperoAd.getInstance().loadBanner(this, BuildConfig.my_gif_screen_bannner)
        }else{
            binding.ageBanner.visibility=View.GONE
        }


    }


    private fun initComponents() {
        val repository = emojisRepository(this)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(repository)).get(MainViewModel::class.java)
    }
}