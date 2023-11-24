package com.emojimerger.mixemojis.emojifun.emojiMixerUtils

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class GDPRUtil(var activity: Activity) {
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)



    var TAG="GDPR_TAG"

    fun setGdpr(){
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("33BE2250B43518CCDA7DE426D04EE231")
            .build()

        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

//        val params = ConsentRequestParameters
//            .Builder()
//            .setTagForUnderAgeOfConsent(false)
//            .build()

        var consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            object:ConsentInformation.OnConsentInfoUpdateSuccessListener {
                override fun onConsentInfoUpdateSuccess() {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity,
                        ConsentForm.OnConsentFormDismissedListener {
                                loadAndShowError ->
                            if(loadAndShowError!=null) {
                                // Consent gathering failed.
                                Log.w(
                                    TAG, String.format(
                                        "%s: %s",
                                        loadAndShowError!!.errorCode,
                                        loadAndShowError.message
                                    )
                                )
                            }

                            if (consentInformation.canRequestAds()) {
                                initializeMobileAdsSdk()
                            }
                            // Consent has been gathered.
                        }
                    )
                }
            },
            ConsentInformation.OnConsentInfoUpdateFailureListener {
                    requestConsentError ->
                // Consent gathering failed.
                Log.w(TAG, String.format("%s: %s",
                    requestConsentError.errorCode,
                    requestConsentError.message
                ))
            })
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk()
        }

    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Google Mobile Ads SDK.
        MobileAds.initialize(activity)

    }



}