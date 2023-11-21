package com.emojimerger.mixemojis.emojifun.Activities

import androidx.lifecycle.MutableLiveData
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.AperoAd
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.application.AdsMultiDexApplication
import com.ads.control.config.AdjustConfig
import com.ads.control.config.AperoAdConfig
import com.emojimerger.mixemojis.emojifun.BuildConfig

class EmojiKitchenApp : AdsMultiDexApplication(){

    private val context: EmojiKitchenApp? = null

    /* fun getApplication(): EmojiKitchenApp? {
         return context
     }*/


    private var loadedNativeAd: ApNativeAd? = null
    private var loadedBannerAd: AperoAd? = null

    fun getLoadedBannerAd(callback:(AperoAd?)->Unit){
        callback(loadedBannerAd)
    }

    fun getLoadedNativeAd(callback: (ApNativeAd?)->Unit) {
         callback(loadedNativeAd)
    }

    fun setLoadedNativeAd(nativeAd: ApNativeAd?) {
        loadedNativeAd = nativeAd
    }

    fun setLoadedBannerAd(bannerAd: AperoAd?) {
        loadedBannerAd = bannerAd
    }
    var isSplashAdClosed = MutableLiveData<Boolean>()
    companion object {

        @JvmField
        var instance: EmojiKitchenApp? = null

        @JvmStatic
        fun getApplication(): EmojiKitchenApp? {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        adsInit()
        AppOpenManager.getInstance().disableAppResumeWithActivity(EmojiKitchenApp::class.java)
        Admob.getInstance().setNumToShowAds(0)
        AperoAd.getInstance().setCountClickToShowAds(2)
    }


    fun adsInit() {

        val environment =
            if (BuildConfig.env_dev) AperoAdConfig.ENVIRONMENT_DEVELOP else AperoAdConfig.ENVIRONMENT_PRODUCTION
        aperoAdConfig = AperoAdConfig(this, AperoAdConfig.PROVIDER_ADMOB, environment)

        // Optional: setup Adjust event
        val adjustConfig = AdjustConfig("ADJUST_TOKEN")
        adjustConfig.eventAdImpression = "EVENT_AD_IMPRESSION_ADJUST"
        adjustConfig.eventNamePurchase = "EVENT_PURCHASE_ADJUST"
        aperoAdConfig.adjustConfig = adjustConfig

        // Optional: enable ads resume
        aperoAdConfig.idAdResume = BuildConfig.app_open

        // set id app_open_app ( if use )
        AppOpenManager.getInstance().setSplashAdId(BuildConfig.app_open)

        // Optional: setup list device test - recommended to use

        // Optional: setup list device test - recommended to use
//        listTestDevice.add("EC25F576DA9B6CE74778B268CB87E431")
        aperoAdConfig.listDeviceTest = listTestDevice

        AperoAd.getInstance().init(this, aperoAdConfig, false)

        // Auto disable ad resume after user click ads and back to app

        // Auto disable ad resume after user click ads and back to app
        Admob.getInstance().setDisableAdResumeWhenClickAds(true)
        // If true -> onNextAction() is called right after Ad Interstitial showed
        Admob.getInstance().setOpenActivityAfterShowInterAds(false)

        AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity::class.java)

    }

}