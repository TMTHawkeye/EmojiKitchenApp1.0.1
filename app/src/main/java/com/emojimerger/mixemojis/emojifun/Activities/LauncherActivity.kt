package com.emojimerger.mixemojis.emojifun.Activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.AperoAd
import com.ads.control.ads.AperoAdCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.billing.AppPurchase
import com.ads.control.funtion.AdCallback
import com.airbnb.lottie.LottieDrawable
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivityLauncherBinding
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.GDPRUtil
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import java.util.concurrent.Executors


class LauncherActivity : BaseActivity() {
    lateinit var binding: ActivityLauncherBinding

    var runnable: Runnable? = null
    val handler = Handler(Looper.getMainLooper())

//    private val typeAdsSplash = "app_open_start"
    private val TIMEOUT_SPLASH = 30000
    private val TIME_DELAY_SPLASH = 2000

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initConsent()

        binding.lottieMovinglight.repeatCount = LottieDrawable.INFINITE
        binding.lottieMovinglight.playAnimation()
        preLoadNativeforSplash()

        val splashTime = getString(R.string.splashTime).toLong()

        runnable = Runnable {
            loadAdsSplash()
        }
        handler.postDelayed(runnable!!, splashTime)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                handler.removeCallbacks(runnable!!)

                finishAffinity()
            }
        })
    }
    fun preLoadNativeforSplash() {
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
    private fun showAdsOpenAppSplash() {
        AppOpenManager.getInstance().showAppOpenSplash(
            this,
            object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    if (isDestroyed || isFinishing) return
//                    startActivity(Intent(this@LauncherActivity, SplashScreen::class.java))

//                    navigateToNextScreen()
                }

                override fun onAdFailedToShow(adError: AdError?) {
                    super.onAdFailedToShow(adError)
                    if (isDestroyed || isFinishing) return
                    EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                    startActivity(Intent(this@LauncherActivity, SplashScreen::class.java))

//                    navigateToNextScreen()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    if (isDestroyed || isFinishing) return
                    EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                    startActivity(Intent(this@LauncherActivity, SplashScreen::class.java))

//                    navigateToNextScreen()
                }
            }
        )
    }
    private fun loadAdsSplash() {
        if (AppPurchase.getInstance().isPurchased) {
            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
            startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
//            navigateToNextScreen()
        } else {
            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(false)

            AppOpenManager.getInstance().loadOpenAppAdSplash(
                this,
                TIMEOUT_SPLASH.toLong(),
                TIME_DELAY_SPLASH.toLong(),
                false,
                object : AdCallback() {
                    override fun onAdSplashReady() {
                        super.onAdSplashReady()
                        if (isDestroyed || isFinishing) return
                        showAdsOpenAppSplash()
                    }

                    override fun onNextAction() {
                        super.onNextAction()
                        if (isDestroyed || isFinishing) return
                        EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
//                            startActivity(Intent(this@LauncherActivity, MainActivity::class.java))

//                            navigateToNextScreen()
                    }

                    override fun onAdFailedToLoad(i: LoadAdError?) {
                        super.onAdFailedToLoad(i)
                        if (isDestroyed || isFinishing) return
                        EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                        startActivity(Intent(this@LauncherActivity, SplashScreen::class.java))

//                            navigateToNextScreen()
                    }
                }
            )

        }
    }

    fun initConsent(){
        val gdpr= GDPRUtil(this)
        gdpr.setGdpr()
    }


}