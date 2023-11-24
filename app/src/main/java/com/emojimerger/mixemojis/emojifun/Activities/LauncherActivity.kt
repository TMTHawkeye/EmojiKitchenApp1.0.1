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
import com.ads.control.ads.wrapper.ApInterstitialAd
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.billing.AppPurchase
import com.ads.control.funtion.AdCallback
import com.airbnb.lottie.LottieDrawable
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivityLauncherBinding
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.GDPRUtil
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.IS_LANGUAGE_SELECTED
import com.emojimerger.mixemojis.emojifun.modelClasses.fileDetails
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import io.paperdb.Paper
import java.util.concurrent.Executors


class LauncherActivity : BaseActivity() {
    lateinit var binding: ActivityLauncherBinding

    var runnable: Runnable? = null
    val handler = Handler(Looper.getMainLooper())

//    private val typeAdsSplash = "app_open_start"
    private val TIMEOUT_SPLASH = 30000L
    private val TIME_DELAY_SPLASH = 2000L

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
        loadAdsSplash()
//        runnable = Runnable {
//
//        }
//        handler.postDelayed(runnable!!, splashTime)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
//                handler.removeCallbacks(runnable!!)
                finishAffinity()
            }
        })
        preLoadNativeforSplash()
        preloadInters4Welcome()
    }

    private fun preloadInters4Welcome() {
        AperoAd.getInstance()
            .getInterstitialAds(this, BuildConfig.welcome_inters, object : AperoAdCallback() {
                override fun onInterstitialLoad(interstitialAd: ApInterstitialAd?) {
                    super.onInterstitialLoad(interstitialAd)
                    EmojiKitchenApp.getApplication()?.inters4WelcomeScreen = interstitialAd
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
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
                    EmojiKitchenApp.instance!!.setLoadedNativeAd(nativeAd)
                }

                override fun onAdFailedToLoad(adError: ApAdError?) {
                    super.onAdFailedToLoad(adError)
                    EmojiKitchenApp.instance!!.setLoadedNativeAd(null)

                }
            }
        )
    }
    

    fun initConsent(){
        val gdpr= GDPRUtil(this)
        gdpr.setGdpr()
    }

    private var isFirstRunApp = true

    private val typeAdsSplash = "app_open_start"
//    private val typeAdsSplash = "inter"

    private fun loadAdsSplash() {
        if (AppPurchase.getInstance().isPurchased) {
            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
            navigateToNextScreen()
        } else {
            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(false)

            /* In the splash screen will usually use inter splash or ads open app.
            Depending on which ad is used, the function will be used to load that ad */

            if (typeAdsSplash == "inter") {
                AperoAd.getInstance().loadSplashInterstitialAds(
                    this,
                    BuildConfig.splash_inters,
                    TIMEOUT_SPLASH,
                    TIME_DELAY_SPLASH,
                    false,
                    object : AperoAdCallback() {
                        override fun onAdFailedToLoad(adError: ApAdError?) {
                            super.onAdFailedToLoad(adError)
                            if (isDestroyed || isFinishing) return
                            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                        }

                        override fun onNextAction() {
                            super.onNextAction()
                            if (isDestroyed || isFinishing) return
                            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                            navigateToNextScreen()
                        }

                        override fun onAdSplashReady() {
                            super.onAdSplashReady()
                            if (isDestroyed || isFinishing) return
                            showInterSplash()
                        }
                    }
                )
            } else {
                AppOpenManager.getInstance().loadOpenAppAdSplash(
                    this,
                    TIMEOUT_SPLASH,
                    TIME_DELAY_SPLASH,
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
                            navigateToNextScreen()
                        }

                        override fun onAdFailedToLoad(i: LoadAdError?) {
                            super.onAdFailedToLoad(i)
                            if (isDestroyed || isFinishing) return
                            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                            navigateToNextScreen()
                        }
                    }
                )
            }
        }
    }

    private fun showInterSplash() {
        AperoAd.getInstance().onShowSplash(this, object : AperoAdCallback() {
            override fun onAdFailedToShow(adError: ApAdError?) {
                super.onAdFailedToShow(adError)
                if (isDestroyed || isFinishing) return
                EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
            }

            override fun onNextAction() {
                super.onNextAction()
                if (isDestroyed || isFinishing) return
                navigateToNextScreen()
            }

            override fun onAdClosed() {
                super.onAdClosed()
                if (isDestroyed || isFinishing) return
                EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
            }
        })
    }

    private fun showAdsOpenAppSplash() {
        AppOpenManager.getInstance().showAppOpenSplash(
            this,
            object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    if (isDestroyed || isFinishing) return
                    navigateToNextScreen()
                }

                override fun onAdFailedToShow(adError: AdError?) {
                    super.onAdFailedToShow(adError)
                    if (isDestroyed || isFinishing) return
                    EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                    navigateToNextScreen()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    if (isDestroyed || isFinishing) return
                    EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                    navigateToNextScreen()
                }
            }
        )
    }

    private fun navigateToNextScreen() {
        if (isDestroyed || isFinishing) {
            return
        }

        navigateToMain()

    /*var isLanguageSelected = Paper.book().read<Boolean>(IS_LANGUAGE_SELECTED, false)

        isLanguageSelected.let {
            if (it == null){
                navigateToLFO()
            }else{
                if (!isLanguageSelected!!) {
                    navigateToLFO()
                } else {
                    navigateToMain()
                }
            }
        }*/

    }

    private fun navigateToMain() {
        val intent = Intent(this, SplashScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLFO() {
        val intent = Intent(this, LanguageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstRunApp) {
            isFirstRunApp = false
            return
        }
        if (typeAdsSplash == "inter") {
            AperoAd.getInstance().onCheckShowSplashWhenFail(this, object : AperoAdCallback() {
                override fun onAdFailedToShow(adError: ApAdError?) {
                    super.onAdFailedToShow(adError)
                    if (isDestroyed || isFinishing) return
                    EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                }

                override fun onNextAction() {
                    super.onNextAction()
                    if (isDestroyed || isFinishing) return
                    navigateToNextScreen()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    if (isDestroyed || isFinishing) return
                    EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                }
            }, 1000)
        } else {
            AppOpenManager.getInstance().onCheckShowAppOpenSplashWhenFail(
                this,
                object : AdCallback() {
                    override fun onNextAction() {
                        super.onNextAction()
                        if (isDestroyed || isFinishing) return
                        navigateToNextScreen()
                    }

                    override fun onAdFailedToShow(adError: AdError?) {
                        super.onAdFailedToShow(adError)
                        if (isDestroyed || isFinishing) return
                        EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                        navigateToNextScreen()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        if (isDestroyed || isFinishing) return
                        EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                        navigateToNextScreen()
                    }
                },
                1000
            )
        }
    }

}