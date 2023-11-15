package com.emojimerger.mixemojis.emojifun.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.adapters.CreationAdapter
import com.emojimerger.mixemojis.emojifun.databinding.ActivityFavouritesBinding
import com.emojimerger.mixemojis.emojifun.repositories.emojisRepository
import com.emojimerger.mixemojis.emojifun.viewModelFactories.MainViewModelFactory
import com.emojimerger.mixemojis.emojifun.viewmodels.MainViewModel

import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FavouritesActivity :BaseActivity() {
    lateinit var binding: ActivityFavouritesBinding
    lateinit var viewModel: MainViewModel
    val mExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    val mHandler = Handler(Looper.getMainLooper())

    lateinit var adapter: CreationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouritesBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initComponents()

        setUpRecyclerView()

        binding.relativeBack.setOnClickListener {
            finish()
        }

        binding.cardCreateEmoji.setOnClickListener {
            startActivity(Intent(this, MixEmojiActivity::class.java))
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                finish()
            }
        })
    }

    private fun initComponents() {
        val repository = emojisRepository(this)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(repository)).get(MainViewModel::class.java)
    }

    private fun getListOfFilesFromInternalStorage(filenameList: List<String>): List<File> {
        val directory = File(getExternalFilesDir(getString(R.string.my_creationFolderName)), "")
        val files = directory.listFiles()?.toList() ?: emptyList()

        val filteredFiles = files.filter { file ->
            filenameList.any { fileName -> file.name.contains(fileName) }
        }

        return filteredFiles
    }

    override fun onResume() {
        super.onResume()
        setUpRecyclerView()
    }

    fun setUpRecyclerView() {
        viewModel.readListFromPaperDB { list ->
            Log.d("TAG", "List of favourite emojis: ${list.size}")
            list.forEach {
                println("favourite item : $it")
            }

            var filteredList = getListOfFilesFromInternalStorage(list)
            if (filteredList.size == 0) {
                binding.linearNoItem.visibility = View.VISIBLE
                binding.collectionRV.visibility=View.GONE
            } else {
                adapter = CreationAdapter(this, filteredList, getString(R.string.favourites))
                binding.collectionRV.layoutManager = GridLayoutManager(this, 3)
                binding.collectionRV.adapter = adapter
                adapter.notifyChange()

            }

        }
    }

}