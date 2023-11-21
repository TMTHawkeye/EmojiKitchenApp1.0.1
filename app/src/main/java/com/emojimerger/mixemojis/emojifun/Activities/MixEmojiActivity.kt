package com.emojimerger.mixemojis.emojifun.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
//import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ads.control.ads.AperoAd
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.adapters.EmojiAdapter
import com.emojimerger.mixemojis.emojifun.databinding.ActivityMixEmojiBinding
import com.emojimerger.mixemojis.emojifun.modelClasses.fileDetails
import com.emojimerger.mixemojis.emojifun.repositories.emojisRepository
import com.emojimerger.mixemojis.emojifun.viewModelFactories.MainViewModelFactory
import com.emojimerger.mixemojis.emojifun.viewmodels.MainViewModel
import io.paperdb.Paper
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MixEmojiActivity : BaseActivity() {
    lateinit var binding: ActivityMixEmojiBinding
    lateinit var adapter: EmojiAdapter

    lateinit var animation: Animation
    private lateinit var viewModel: MainViewModel

    var mExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    var mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMixEmojiBinding.inflate(layoutInflater)

         setContentView(binding.root)
        AperoAd.getInstance().loadBanner(this, BuildConfig.mix_emoji_banner)

        mExecutor.execute {
            initComponents()
            mHandler.post {
                viewModel.getListOfImagePathsFromAssets(this) {
                    Log.d("TAG", "Size of Images List: " + it.size.toString())
                    binding.progressRV.visibility = View.GONE
                    adapter = EmojiAdapter(this, it)
                    binding.emojiRV.layoutManager = GridLayoutManager(this, 3)
                    binding.emojiRV.adapter = adapter
                }

            }
        }

        binding.btnMerge.setOnClickListener {
            mergeSelectedEmojis()
        }

        binding.relativeBack.setOnClickListener {
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
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);



    }

    override fun onPause() {
        super.onPause()
        binding.emoji1Img.setImageDrawable(null)
        binding.emoji2Img.setImageDrawable(null)
    }
    private fun saveKeyToPaperDb(key: String, value: String) {
        Paper.book().write("$key", value);
    }

    private fun setSelectedEmoji(path: String, view: ImageView) {
        var btmp = getImageFromAssets(path)
        var d = BitmapDrawable(resources, btmp)
        view.setImageDrawable(d)

    }

    private fun getImageFromAssets(filePath: String): Bitmap? {
        val assetManager = assets
        val istr: InputStream
        try {
            istr = assetManager.open(filePath)
            return BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getFileName(filePath: String): String {
        val pathComponents: List<String> = filePath.split("/")
        val fullName = pathComponents[pathComponents.size - 1]
        val (fileName, fileExtension) = fullName.split(".")
        Log.d("TAG", "FileName is: " + fileName + "********************")
        return fileName
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun mergeSelectedEmojis() {
        val selectedEmojiPaths = adapter.getSelectedEmojiPaths()

        // Check if exactly two emojis are selected
        if (selectedEmojiPaths.size == 2) {
            Log.d(
                "TAG",
                "mergeSelectedEmojis: ${selectedEmojiPaths[0]} and ${selectedEmojiPaths[1]}"
            )

//            if ((binding.emoji1Img.drawable != null) && (binding.emoji2Img.drawable != null)) {
            binding.progressImg.visibility = View.VISIBLE
            binding.btnMerge.visibility = View.GONE
            binding.emojiRV.visibility = View.GONE
            mExecutor.execute {
                var emoji1 = getFileName(selectedEmojiPaths[0])
                var emoji2 = getFileName(selectedEmojiPaths[1])

                var date = getString(R.string.fixed_date_emoji)
                var databaseKey1 = emoji1 + "_" + emoji2
                var databaseKey2 = emoji2 + "_" + emoji1
                Log.d("TAG", "onCreate Database keys are: $databaseKey1 and $databaseKey2")
//                binding.progressRV.visibility=View.VISIBLE
                mHandler.post {
                    if (isInternetAvailable()) {
                        viewModel.checkImageInDatabase(
                            databaseKey1,
                            databaseKey2,
                            emoji1,
                            emoji2,
                            date
                        ) { emojiUrl, fileName, boolean ->
                            Log.d("TAG", "Emoji loaded is: $emojiUrl")

//                            binding.progressImg.visibility = View.GONE

                            val fileDetails = fileDetails(
                                emojiUrl,
                                fileName,
                                "New Emoji",
                                boolean,
                                databaseKey1,
                                databaseKey2,
                                getString(R.string.mixEmojis)
                            )
                            Paper.book().write<fileDetails>("temp_list_file", fileDetails)

                            startActivity(
                                Intent(
                                    this,
                                    CreatedEmojiActivity::class.java
                                )
                            )


                        }
                    } else {
                        Toast.makeText(this, "No Internet Available!!", Toast.LENGTH_SHORT).show()
                        binding.progressImg.visibility = View.GONE
                        finish()
                    }
                }
            }
//            } else {
//                Toast.makeText(this, getString(R.string.mergeEmojisToast), Toast.LENGTH_SHORT).show()
//            }
        } else {
            Log.d("TAG", "mergeSelectedEmojis: please select 2 emojis")
            Toast.makeText(this, getString(R.string.mergeEmojisToast), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.clearSelectedItems()
        }
        binding.progressImg.visibility = View.GONE
        binding.emojiRV.visibility = View.VISIBLE
        binding.btnMerge.visibility = View.VISIBLE

    }


}
