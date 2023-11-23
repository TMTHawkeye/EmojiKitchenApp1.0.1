package com.emojimerger.mixemojis.emojifun.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.ads.control.ads.AperoAd
import com.ads.control.ads.AperoAdCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.billing.AppPurchase
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivitySettingsBinding
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.isInternetAvailable

class SettingsActivity : BaseActivity() {
    lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.settingsAdConatiner.visibility=View.GONE

        if(isInternetAvailable()) {
            binding.settingsAdConatiner.visibility=View.VISIBLE
            loadNativeAd()
        }

        binding.cardShareapp.setOnClickListener {
            shareApplication()
        }

        binding.cardRateUs.setOnClickListener {
            rateApp()
        }

        binding.cardFeedback.setOnClickListener {
            feedBack()
        }
        binding.cardPrivacy.setOnClickListener {
            privacyPolicy()
        }
        binding.cardLanguage.setOnClickListener {
            Toast.makeText(this, getString(R.string.commingSoon), Toast.LENGTH_SHORT).show()

        }
        binding.cardAboutUs.setOnClickListener {
            aboutUs()
        }

        binding.relativeBack.setOnClickListener {
            finish()
            EmojiKitchenApp.instance!!.setLoadedNativeAd(null)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                finish()
                EmojiKitchenApp.instance!!.setLoadedNativeAd(null)
            }
        })
    }

    private fun shareApplication() {
//        Toast.makeText(this, getString(R.string.commingSoon), Toast.LENGTH_SHORT).show()

        val appPackageName = packageName
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Check out this amazing app: https://play.google.com/store/apps/details?id=$appPackageName"
        )
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Share via"))
    }

    private fun rateApp() {
        var rateus=show_rateus_dialog()
        rateus.show()

        var later=rateus.findViewById<TextView>(R.id.later)
        later.setOnClickListener {
            rateus.dismiss()
            hideNavBar()
        }

        var rate=rateus.findViewById<RelativeLayout>(R.id.card_rateUs)
        rate.setOnClickListener {
            rateOurApp()
            rateus.dismiss()
            hideNavBar()
        }
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

    private fun rateOurApp(){
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


    private fun feedBack(){
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:alawraqmarketing@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Feedback for YourApp")
        }
        startActivity(Intent.createChooser(emailIntent, "Send feedback"))
    }

    private fun privacyPolicy(){
        val privacyPolicyUrl = "https://sites.google.com/view/alawraq-studio/home"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
        startActivity(browserIntent)
    }

    private fun aboutUs(){
//        val aboutUsUrl = "https://www.yourwebsite.com/about-us" // Replace with your About Us webpage URL
//        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(aboutUsUrl))
//        startActivity(browserIntent)

        Toast.makeText(this, getString(R.string.commingSoon), Toast.LENGTH_SHORT).show()
    }

    private fun loadNativeAd() {
        EmojiKitchenApp.instance!!.getLoadedNativeAd() { appNative->
            if (appNative == null  && !AppPurchase.getInstance().isPurchased) {
//                printLog("preloaded_welcome_native", "Preloaded WelcomeNative is Null, New request is sent on WelcomeScreen")
                AperoAd.getInstance().loadNativeAdResultCallback(this@SettingsActivity,
                    BuildConfig.setting_native,
                    R.layout.custom_native_with_media, object :
                        AperoAdCallback(){
                        override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                            super.onNativeAdLoaded(nativeAd)
                            binding.settingsAdConatiner.visibility = View.VISIBLE
                            AperoAd.getInstance().populateNativeAdView(this@SettingsActivity, nativeAd, binding.settingsAdConatiner, binding.homeNative.shimmerContainerNative)
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
                    this@SettingsActivity,
                    appNative,
                    binding.settingsAdConatiner,
                    binding.homeNative.shimmerContainerNative)
            }
        }




//        var savedNativeAd=EmojiKitchenApp.instance!!.getLoadedNativeAd()
//        val fl_adplaceholder = binding.settingsAdConatiner
//        val shimmerFrameLayout = binding.homeNative.shimmerContainerNative
//
//        AperoAd.getInstance().loadNativeAdResultCallback(
//            this@SplashScreen,
//            BuildConfig.native_ad,
//            R.layout.custom_native_with_media,
//            object :
//                AperoAdCallback() {
//
//                override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
//                    super.onNativeAdLoaded(nativeAd)
//                    AperoAd.getInstance().populateNativeAdView(
//                        this@SplashScreen,
//                        nativeAd,
//                        fl_adplaceholder,
//                        shimmerFrameLayout
//                    )
//                }
//            })
    }



}