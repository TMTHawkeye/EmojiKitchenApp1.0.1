package com.emojimerger.mixemojis.emojifun.Activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity() {
    lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)

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
        }
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
            data = Uri.parse("mailto:alawraqmarketing@gmail.com" +
                    "feedbackemail") // Replace with your feedback email address
            putExtra(Intent.EXTRA_SUBJECT, "Feedback for YourApp") // Replace with your email subject
        }
        startActivity(Intent.createChooser(emailIntent, "Send feedback"))
//        Toast.makeText(this, getString(R.string.commingSoon), Toast.LENGTH_SHORT).show()

    }

    private fun privacyPolicy(){
        val privacyPolicyUrl = "https://sites.google.com/view/alawraq-studio/home"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
        startActivity(browserIntent)

//        Toast.makeText(this, getString(R.string.commingSoon), Toast.LENGTH_SHORT).show()

    }

    private fun aboutUs(){
//        val aboutUsUrl = "https://www.yourwebsite.com/about-us" // Replace with your About Us webpage URL
//        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(aboutUsUrl))
//        startActivity(browserIntent)

        Toast.makeText(this, getString(R.string.commingSoon), Toast.LENGTH_SHORT).show()
    }

}