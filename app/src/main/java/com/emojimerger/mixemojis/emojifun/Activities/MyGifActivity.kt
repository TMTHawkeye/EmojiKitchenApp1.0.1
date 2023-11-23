package com.emojimerger.mixemojis.emojifun.Activities

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ads.control.ads.AperoAd
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.adapters.CreationAdapter
import com.emojimerger.mixemojis.emojifun.databinding.ActivityMyGifBinding
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.isInternetAvailable
import com.emojimerger.mixemojis.emojifun.repositories.emojisRepository
import com.emojimerger.mixemojis.emojifun.viewModelFactories.MainViewModelFactory
import com.emojimerger.mixemojis.emojifun.viewmodels.MainViewModel

class MyGifActivity : BaseActivity() {
    lateinit var binding: ActivityMyGifBinding
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyGifBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        viewModel.getListOfFilesFromInternalStorage(getString(R.string.my_created_gifs_folderName)) {
            if (it.size == 0) {
                binding.linearNoItem.visibility = View.VISIBLE
                binding.ageBanner.visibility=View.GONE
            } else {
                loadBanner()

                val adapter = CreationAdapter(
                    this@MyGifActivity,
                    it, getString(R.string.mygifs)
                )
                binding.collectionRV.layoutManager = GridLayoutManager(this, 3)
                binding.collectionRV.adapter = adapter
            }
        }

        binding.relativeBack.setOnClickListener {
            finish()
        }


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