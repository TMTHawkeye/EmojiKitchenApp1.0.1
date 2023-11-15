package com.emojimerger.mixemojis.emojifun.Activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivityMainBinding
import com.emojimerger.mixemojis.emojifun.repositories.emojisRepository
import com.emojimerger.mixemojis.emojifun.viewModelFactories.MainViewModelFactory
import com.emojimerger.mixemojis.emojifun.viewmodels.MainViewModel
import io.paperdb.Paper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var animation: Animation
    private lateinit var viewModel: MainViewModel

    var mExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    var mHandler = Handler(Looper.getMainLooper())

    lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContent { setContentView(binding.root) }

        initComponents()

        binding.cardMixEmoji.setOnClickListener {
            startActivity(Intent(this, MixEmojiActivity::class.java))
        }

        binding.cardCollectionEmoji.setOnClickListener {
            startActivity(Intent(this, CollectionActivity::class.java).putExtra("intentFrom",getString(R.string.collection)))
        }

        binding.cardMyCreation.setOnClickListener {
            startActivity(Intent(this, MyCreationActivity::class.java))
        }

        binding.cardMyGifs.setOnClickListener {
            startActivity(Intent(this,MyGifActivity::class.java))
        }

        binding.favourites.setOnClickListener {
            startActivity(Intent(this, FavouritesActivity::class.java))
        }

        binding.cardCreateGif.setOnClickListener {
            startActivity(Intent(this, CollectionActivity::class.java).putExtra("intentFrom",getString(R.string.createGif)))
        }

        binding.rateUsBtn.setOnClickListener {
            val rateus=show_rateus_dialog()
            rateus.show()

            val later=rateus.findViewById<TextView>(R.id.later)
            later.setOnClickListener {
                rateus.dismiss()
                hideNavBar()
            }

            val rate=rateus.findViewById<RelativeLayout>(R.id.card_rateUs)
            rate.setOnClickListener {
                rateApp()
                rateus.dismiss()
                hideNavBar()
            }

            val lottie=rateus.findViewById<LottieAnimationView>(R.id.lottie_bg_rateUs)
            lottie.repeatCount=LottieDrawable.INFINITE
            lottie.playAnimation()


        }

        binding.constrainSettings.setOnClickListener {
            startActivity(Intent(this,SettingsActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                showExitDialog()
            }
        })
    }

    private fun showExitDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_exit_app)
        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val exitBtn=dialog.findViewById<RelativeLayout>(R.id.card_Exit)
        val cancelBtn=dialog.findViewById<TextView>(R.id.cancel)
        val lottie=dialog.findViewById<LottieAnimationView>(R.id.lottie_bg_exit)
        lottie.repeatCount = LottieDrawable.INFINITE
        lottie.playAnimation()
        exitBtn.setOnClickListener {
            finishAffinity()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        window.setGravity(Gravity.CENTER)
        dialog.show()
    }

    private fun initComponents() {
        Log.d("TAG", "initComponents: Initializing viewmodel and paper db!!")
        val repository = emojisRepository(this)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(repository)).get(MainViewModel::class.java)
        Paper.init(this);

//        viewModel.zipEmojisFromFirebaseStorage() {
//            if (it) {
//                Log.d("TAG", "files unzipped success ")
//            } else {
//                Log.d("TAG", "files unzipped failure ")
//            }
//        }
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);

    }

    private fun show_rateus_dialog():Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_rate_us)

        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setGravity(Gravity.CENTER)

        return dialog
    }

    private fun rateApp(){
        val appPackageName = packageName
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

}
