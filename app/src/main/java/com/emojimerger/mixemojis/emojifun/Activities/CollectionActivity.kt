package com.emojimerger.mixemojis.emojifun.Activities

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.emojimerger.mixemojis.emojifun.adapters.CollectionAdapter
import com.emojimerger.mixemojis.emojifun.databinding.ActivityCollectionBinding
import com.emojimerger.mixemojis.emojifun.repositories.emojisRepository
import com.emojimerger.mixemojis.emojifun.viewModelFactories.MainViewModelFactory
import com.emojimerger.mixemojis.emojifun.viewmodels.MainViewModel



class CollectionActivity : BaseActivity() {
    lateinit var binding: ActivityCollectionBinding
    lateinit var viewModel: MainViewModel
    lateinit var intentFrom:String
//    val mExecutor: ExecutorService = Executors.newSingleThreadExecutor()
//    val mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectionBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initComponents()

        binding.titleCollection.text=intentFrom

        viewModel.getListOfImagePathsFromAssets(this) {
            Log.d("TAG", "Size of Images List: " + it.size.toString())

            val adapter = CollectionAdapter(this, it,intentFrom)
            binding.collectionRV.layoutManager = GridLayoutManager(this, 3)
            binding.collectionRV.adapter = adapter
        }

        binding.relativeBack.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                finish()
            }
        })
    }

    private fun initComponents() {
        intentFrom=intent.getStringExtra("intentFrom")!!
        val repository = emojisRepository(this)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(repository)).get(MainViewModel::class.java)
    }
}