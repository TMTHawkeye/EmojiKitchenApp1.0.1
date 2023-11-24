package com.emojimerger.mixemojis.emojifun.emojiMixerUtils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emojimerger.mixemojis.emojifun.R


public var IS_LANGUAGE_SELECTED = "IS_LANGUAGE_SELECTED"


fun Activity.isInternetAvailable(): Boolean {
    val connectivityManager =
        getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    // Check if the internet is available and the device is connected
    return capabilities != null && (
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
}

fun isReadStorageAllowed(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

fun isWriteStorageAllowed(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

 fun requestStoragePermission(activity: Activity) {
    //android 13
    if (Build.VERSION.SDK_INT >= 33) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
            ),
            activity.getString(R.string.storagePermissionCode).toInt()
        )
        //android 12 or lesser
    } else {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            activity.getString(R.string.storagePermissionCode).toInt()
        )
    }
}

 fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.parse("package:$context.packageName")
    context.startActivity(intent)
}

