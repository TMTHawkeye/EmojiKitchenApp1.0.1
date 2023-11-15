package com.emojimerger.mixemojis.emojifun.Activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieDrawable
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivitySplashScreenBinding

class SplashScreen : BaseActivity() {
    lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieMovinglight.repeatCount = LottieDrawable.INFINITE
        binding.lottieMovinglight.playAnimation()

        binding.cardLetsStart.setOnClickListener {
            // Check if the permission has already been granted
            if (isReadStorageAllowed() && isWriteStorageAllowed()) {
                // Permission is already granted
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // Request permission
                requestStoragePermission()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                finishAffinity()
            }
        })
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
                // Permission is granted, start the MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
//                // Permissions are denied, show a toast to inform the user
//                Toast.makeText(
//                    this,
//                    getString(R.string.allowStoragePermToast),
//                    Toast.LENGTH_SHORT
//                ).show()

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

    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}