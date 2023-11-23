package com.emojimerger.mixemojis.emojifun.Activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.AperoAd
import com.ads.control.ads.AperoAdCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApInterstitialAd
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.billing.AppPurchase
import com.ads.control.funtion.AdCallback
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivityMainBinding
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.GDPRUtil
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.isInternetAvailable
import com.emojimerger.mixemojis.emojifun.repositories.emojisRepository
import com.emojimerger.mixemojis.emojifun.viewModelFactories.MainViewModelFactory
import com.emojimerger.mixemojis.emojifun.viewmodels.MainViewModel
import com.google.android.gms.ads.AdError
import io.paperdb.Paper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var animation: Animation
    private lateinit var viewModel: MainViewModel
    var mInterstitialAd: ApInterstitialAd? = null


    var mExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    var mHandler = Handler(Looper.getMainLooper())

    lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initComponents()

        if (!isInternetAvailable()) {
            binding.imgHome.visibility = View.VISIBLE
            binding.settingsAdConatiner.visibility = View.GONE
        } else {
            binding.imgHome.visibility = View.GONE
            loadNativeAd()
            loadInterCreate()
        }

        binding.cardMixEmoji.setOnClickListener {
            if (mInterstitialAd!!.isReady) {
                AperoAd.getInstance()
                    .showInterstitialAdByTimes(this, mInterstitialAd, object : AperoAdCallback() {
                        override fun onNextAction() {
                            super.onNextAction()
                            Log.d("TAG", "onNextAction")
                            val intent = Intent(applicationContext, MixEmojiActivity::class.java)
                            startActivity(intent)
                        }
                    }, true)
            } else {
                val intent = Intent(this, MixEmojiActivity::class.java)
                startActivity(intent)
                loadInterCreate()
            }
        }

        binding.cardCollectionEmoji.setOnClickListener {
            if (mInterstitialAd!!.isReady) {
                AperoAd.getInstance()
                    .showInterstitialAdByTimes(this, mInterstitialAd, object : AperoAdCallback() {
                        override fun onNextAction() {
                            super.onNextAction()
                            Log.d("TAG", "onNextAction")
                            startActivity(
                                Intent(this@MainActivity, CollectionActivity::class.java).putExtra(
                                    "intentFrom",
                                    getString(R.string.collection)
                                )
                            )
                        }
                    }, true)
            } else {
                startActivity(
                    Intent(this@MainActivity, CollectionActivity::class.java).putExtra(
                        "intentFrom",
                        getString(R.string.collection)
                    )
                )
                loadInterCreate()
            }
        }

        binding.cardMyCreation.setOnClickListener {
            val intent = Intent(applicationContext, MyCreationActivity::class.java)
            startActivity(intent)
        }

        binding.cardMyGifs.setOnClickListener {
            val intent = Intent(applicationContext, MyGifActivity::class.java)
            startActivity(intent)
        }

        binding.favourites.setOnClickListener {
            val intent = Intent(applicationContext, FavouritesActivity::class.java)
            startActivity(intent)
        }

        binding.cardCreateGif.setOnClickListener {
            if (mInterstitialAd!!.isReady) {
                AperoAd.getInstance()
                    .showInterstitialAdByTimes(this, mInterstitialAd, object : AperoAdCallback() {
                        override fun onNextAction() {
                            super.onNextAction()
                            Log.d("TAG", "onNextAction")
                            startActivity(
                                Intent(this@MainActivity, CollectionActivity::class.java).putExtra(
                                    "intentFrom",
                                    getString(R.string.createGif)
                                )
                            )
                        }
                    }, true)
            } else {
                startActivity(
                    Intent(this@MainActivity, CollectionActivity::class.java).putExtra(
                        "intentFrom",
                        getString(R.string.createGif)
                    )
                )
            }
        }

        binding.rateUsBtn.setOnClickListener {
            val rateus = show_rateus_dialog()
            rateus.show()

            val later = rateus.findViewById<TextView>(R.id.later)
            later.setOnClickListener {
                rateus.dismiss()
                hideNavBar()
            }

            val rate = rateus.findViewById<RelativeLayout>(R.id.card_rateUs)
            rate.setOnClickListener {
                rateApp()
                rateus.dismiss()
                hideNavBar()
            }

            val lottie = rateus.findViewById<LottieAnimationView>(R.id.lottie_bg_rateUs)
            lottie.repeatCount = LottieDrawable.INFINITE
            lottie.playAnimation()


        }

        binding.constrainSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                showExitDialog()
            }
        })

//        val crashButton = Button(this)
//        crashButton.text = "Test Crash"
//        crashButton.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }

//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))

        loadInterCreate()

    }

    private fun showExitDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_exit_app)
        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val exitBtn = dialog.findViewById<RelativeLayout>(R.id.card_Exit)
        val cancelBtn = dialog.findViewById<TextView>(R.id.cancel)
        val lottie = dialog.findViewById<LottieAnimationView>(R.id.lottie_bg_exit)
        loadNativeAd()
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

//        viewModel.zipEmojisFromFirebaseStorage() {
//            if (it) {
//                Log.d("TAG", "files unzipped success ")
//            } else {
//                Log.d("TAG", "files unzipped failure ")
//            }
//        }
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);

        preLoadNativeForSettings()

    }

    private fun show_rateus_dialog(): Dialog {
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

    private fun rateApp() {
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

    private fun loadNativeAd() {
        binding.imgHome.visibility=View.GONE
        EmojiKitchenApp.instance!!.getLoadedNativeAd() { appNative->
            if (appNative == null  && !AppPurchase.getInstance().isPurchased) {
//                printLog("preloaded_welcome_native", "Preloaded WelcomeNative is Null, New request is sent on WelcomeScreen")
                AperoAd.getInstance().loadNativeAdResultCallback(this@MainActivity,
                    BuildConfig.home_screen_native,
                    R.layout.custom_native_with_media, object :
                        AperoAdCallback(){
                        override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                            super.onNativeAdLoaded(nativeAd)
                            binding.settingsAdConatiner.visibility = View.VISIBLE
                            AperoAd.getInstance().populateNativeAdView(this@MainActivity, nativeAd, binding.settingsAdConatiner, binding.homeNative.shimmerContainerNative)
                        }

                        override fun onAdFailedToLoad(adError: ApAdError?) {
                            super.onAdFailedToLoad(adError)
                            binding.settingsAdConatiner.visibility = View.GONE
                        }

                        override fun onAdFailedToShow(adError: ApAdError?) {
                            super.onAdFailedToShow(adError)
                            binding.settingsAdConatiner.visibility = View.GONE
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                        }
                    })
            }else{
//                printLog("preloaded_welcome_native", "Preloaded WelcomeNative is Displayed")
                binding.settingsAdConatiner.visibility = View.VISIBLE
                AperoAd.getInstance().populateNativeAdView(
                    this@MainActivity,
                    appNative,
                    binding.settingsAdConatiner,
                    binding.homeNative.shimmerContainerNative)
            }
        }
    }




    private fun loadInterCreate() {
        mInterstitialAd =
            AperoAd.getInstance().getInterstitialAds(this@MainActivity, BuildConfig.home_inters)
    }

    fun preLoadNativeForSettings(){
        AperoAd.getInstance().loadNativeAdResultCallback(
            this,
            BuildConfig.setting_native,
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


    override fun onResume() {
        super.onResume()
        preLoadNativeForSettings()
    }



}
