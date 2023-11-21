package com.emojimerger.mixemojis.emojifun.Activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import com.ads.control.ads.AperoAd
import com.ads.control.ads.AperoAdCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApNativeAd
import com.airbnb.lottie.LottieDrawable
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivityLauncherBinding
import java.util.concurrent.Executors


class LauncherActivity : BaseActivity() {
    lateinit var binding: ActivityLauncherBinding

//    private var progressStatus = 0
//    val executor = Executors.newSingleThreadExecutor()
    var runnable:Runnable?=null
    val handler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieMovinglight.repeatCount = LottieDrawable.INFINITE
        binding.lottieMovinglight.playAnimation()
//        binding.lottieMoving.repeatCount = LottieDrawable.INFINITE
//        binding.lottieMoving.playAnimation()
        preLoadNativeforSplash()

//        executor.execute {
//            while (progressStatus < 100) {
//                progressStatus++
//                handler.post {
//                    binding.progressSplash.progress = progressStatus
//                    val newTranslation =
//                        (binding.progressSplash.width * progressStatus / 100).toFloat()
//                    binding.lottieMoving.translationX = newTranslation
//                }
//                try {
//                    Thread.sleep(50)
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//            }
//        }
//
        val splashTime = getString(R.string.splashTime).toLong()

         runnable = Runnable {
            startActivity(Intent(this,SplashScreen::class.java))
            finish()
        }
        handler.postDelayed(runnable!!, splashTime)


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
//                handler.removeCallbacks(runnable!!)

                finishAffinity()
            }
        })
    }

    fun preLoadNativeforSplash(){
        AperoAd.getInstance().loadNativeAdResultCallback(
            this,
            BuildConfig.welcome_screen_native,
            R.layout.custom_native_with_media,
            object : AperoAdCallback() {
                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                    super.onNativeAdLoaded(nativeAd)
//                    EmojiKitchenApp.getApplication()?.getStorage()?.nativeAd4ContinueScreen?.postValue(nativeAd)
                    EmojiKitchenApp.instance!!.setLoadedNativeAd(nativeAd)
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
//                    EmojiKitchenApp.getApplication()?.getStorage()?.nativeAd4ContinueScreen?.postValue(null)
                    EmojiKitchenApp.instance!!.setLoadedNativeAd(null)

                }
            }
        )
    }




}