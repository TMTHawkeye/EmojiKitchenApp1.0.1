package com.emojimerger.mixemojis.emojifun.Activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ads.control.admob.AppOpenManager
import com.airbnb.lottie.LottieDrawable
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivitySplashScreenBinding
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.ads.AperoAd
import com.ads.control.ads.AperoAdCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.billing.AppPurchase
import com.ads.control.funtion.AdCallback
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class SplashScreen : BaseActivity() {
    lateinit var binding: ActivitySplashScreenBinding
    private var isFirstRunApp = true

    //    private val typeAdsSplash = "app_open_start"
    private val TIMEOUT_SPLASH = 30000
    private val TIME_DELAY_SPLASH = 20

    private val typeAdsSplash = "inter"
    private var progressStatus = 0
    val executor = Executors.newSingleThreadExecutor()
    var runnable: Runnable? = null
    val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preLoadNativeForMain()

        binding.lottieMovinglight.repeatCount = LottieDrawable.INFINITE
        binding.lottieMovinglight.playAnimation()

        binding.lottieMoving.repeatCount = LottieDrawable.INFINITE
        binding.lottieMoving.playAnimation()

        executor.execute {
            while (progressStatus < 100) {
                progressStatus++
                handler.post {
                    binding.progressSplash.progress = progressStatus
                    val newTranslation =
                        (binding.progressSplash.width * progressStatus / 100).toFloat()
                    binding.lottieMoving.translationX = newTranslation
                }
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        val splashTime = getString(R.string.splashTime).toLong()

        runnable = Runnable {
            binding.progressSplash.visibility = View.INVISIBLE
            binding.lottieMoving.visibility = View.INVISIBLE

            if (isReadStorageAllowed() && isWriteStorageAllowed()) {
                loadAdsSplash()

            }
            else{
                binding.cardLetsStart.visibility = View.VISIBLE
            }
        }
        handler.postDelayed(runnable!!, splashTime)

        binding.cardLetsStart.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                // Permission is already granted
                runOnUiThread {
                    loadAdsSplash()
                }
            }

        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                finishAffinity()
            }
        })

        if (!isInternetAvailable()) {
            binding.imgHome.visibility = View.VISIBLE
            binding.settingsAdConatiner.visibility = View.GONE
        } else {
            loadNativeAd()
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isWriteStorageAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        //android 13
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                ),
                getString(R.string.storagePermissionCode).toInt()
            )
            //android 12 or lesser
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                getString(R.string.storagePermissionCode).toInt()
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == getString(R.string.storagePermissionCode).toInt()) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.cardLetsStart.visibility=View.INVISIBLE
                // Permission is granted, start the MainActivity
                startActivity(Intent(this@SplashScreen, MainActivity::class.java))


            } else {
                // Permissions are denied
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    && shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ) {
                    // User denied permissions, show rationale and request again
                    Toast.makeText(
                        this,
                        getString(R.string.allowStoragePermToast),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // User denied permissions and selected "Don't ask again"
                    openAppSettings()
                }
            }
        }
    }

    private fun loadNativeAd() {
        EmojiKitchenApp.instance!!.getLoadedNativeAd() { appNative ->
            if (appNative == null && !AppPurchase.getInstance().isPurchased) {
//                printLog("preloaded_welcome_native", "Preloaded WelcomeNative is Null, New request is sent on WelcomeScreen")
                AperoAd.getInstance().loadNativeAdResultCallback(this@SplashScreen,
                    BuildConfig.welcome_screen_native,
                    R.layout.custom_native_with_media, object :
                        AperoAdCallback() {
                        override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                            super.onNativeAdLoaded(nativeAd)
                            binding.settingsAdConatiner.visibility = View.VISIBLE
                            AperoAd.getInstance().populateNativeAdView(
                                this@SplashScreen,
                                nativeAd,
                                binding.settingsAdConatiner,
                                binding.homeNative.shimmerContainerNative
                            )
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
            } else {
//                printLog("preloaded_welcome_native", "Preloaded WelcomeNative is Displayed")
                binding.settingsAdConatiner.visibility = View.VISIBLE
                AperoAd.getInstance().populateNativeAdView(
                    this@SplashScreen,
                    appNative,
                    binding.settingsAdConatiner,
                    binding.homeNative.shimmerContainerNative
                )
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        // Check if the internet is available and the device is connected
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                )
    }

    fun preLoadNativeForMain() {
        AperoAd.getInstance().loadNativeAdResultCallback(
            this,
            BuildConfig.home_screen_native,
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


    private fun loadAdsSplash() {
        if (AppPurchase.getInstance().isPurchased) {
            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
//            navigateToNextScreen()
        } else {
            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(false)

            if (typeAdsSplash == "inter") {
                AperoAd.getInstance().loadSplashInterstitialAds(
                    this@SplashScreen,
                    BuildConfig.splash_inters,
                    TIMEOUT_SPLASH.toLong(),
                    TIME_DELAY_SPLASH.toLong(),
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
//                            startActivity(Intent(this@SplashScreen, MainActivity::class.java))

//                            navigateToNextScreen()
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
                            startActivity(Intent(this@SplashScreen, MainActivity::class.java))

//                            navigateToNextScreen()
                        }

                        override fun onAdFailedToLoad(i: LoadAdError?) {
                            super.onAdFailedToLoad(i)
                            if (isDestroyed || isFinishing) return
                            EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                            startActivity(Intent(this@SplashScreen, MainActivity::class.java))

//                            navigateToNextScreen()
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
//                startActivity(Intent(this@SplashScreen, MainActivity::class.java))

//                navigateToNextScreen()
            }

            override fun onAdClosed() {
                super.onAdClosed()
                if (isDestroyed || isFinishing) return
                EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)

                // Check if the permission has already been granted
                if (isReadStorageAllowed() && isWriteStorageAllowed()) {
                    startActivity(Intent(this@SplashScreen, MainActivity::class.java))

                } else {
                    // Request permission
                    requestStoragePermission()
                }

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
                    startActivity(Intent(this@SplashScreen, MainActivity::class.java))

//                    navigateToNextScreen()
                }

                override fun onAdFailedToShow(adError: AdError?) {
                    super.onAdFailedToShow(adError)
                    if (isDestroyed || isFinishing) return
                    EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                    startActivity(Intent(this@SplashScreen, MainActivity::class.java))

//                    navigateToNextScreen()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    if (isDestroyed || isFinishing) return
                    EmojiKitchenApp.getApplication()?.isSplashAdClosed?.postValue(true)
                    startActivity(Intent(this@SplashScreen, MainActivity::class.java))

//                    navigateToNextScreen()
                }
            }
        )
    }

}