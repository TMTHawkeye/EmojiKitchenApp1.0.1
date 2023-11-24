package com.emojimerger.mixemojis.emojifun.Activities

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import com.ads.control.ads.AperoAd
import com.ads.control.ads.AperoAdCallback
import com.ads.control.ads.wrapper.ApAdError
import com.ads.control.ads.wrapper.ApNativeAd
import com.ads.control.billing.AppPurchase
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.emojimerger.mixemojis.emojifun.BuildConfig
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.databinding.ActivityCreatedEmojiBinding
import com.emojimerger.mixemojis.emojifun.databinding.CustomDialogEmojiNotFoundBinding
import com.emojimerger.mixemojis.emojifun.databinding.CustomDialogLoadGifBinding
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.UIMethods.shadAnim
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.isReadStorageAllowed
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.isWriteStorageAllowed
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.openAppSettings
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.requestStoragePermission
import com.emojimerger.mixemojis.emojifun.modelClasses.fileDetails
import com.emojimerger.mixemojis.emojifun.repositories.emojisRepository
import com.emojimerger.mixemojis.emojifun.viewModelFactories.MainViewModelFactory
import com.emojimerger.mixemojis.emojifun.viewmodels.MainViewModel
import com.facebook.shimmer.ShimmerFrameLayout

import com.iambedant.text.OutlineTextView
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CreatedEmojiActivity : BaseActivity() {
    lateinit var binding: ActivityCreatedEmojiBinding
    lateinit var viewModel: MainViewModel
    lateinit var fileName: String
    lateinit var intentName: String
    lateinit var intentFrom: String
    lateinit var emojiUrl: String
    lateinit var databaseKey1: String
    lateinit var databaseKey2: String
    private var isFineToUseListeners = false
    private var isAlreadyDloaded = false
    lateinit var filePath: String


    var mExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    var mHandler = Handler(Looper.getMainLooper())
    var state: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatedEmojiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.settingsAdConatiner.visibility = View.INVISIBLE

        initComponents()
        receivedData()
        val splashTime = getString(R.string.loading_emoji_time).toLong()
        val loadingDialog = showLoadingDialog()
        loadingDialog.show()
        loadNativeAd(this@CreatedEmojiActivity, binding.settingsAdConatiner, binding.homeNative.shimmerContainerNative,
            BuildConfig.new_emoji_native)

        Log.d("TAG", "intent name is: $intentFrom")
        Log.d("TAG", "Emoji URL is: $emojiUrl")

        val runnable = Runnable {
            //intent from mix emojis
            if (intentFrom.equals(getString(R.string.mixEmojis))) {
                Glide.with(this).asDrawable().load(emojiUrl)
                    .into(object : CustomTarget<Drawable>() {

                    override fun onStart() {
                    }

                    override fun onStop() {
                    }

                    override fun onDestroy() {
                    }

                    override fun onLoadStarted(placeholder: Drawable?) {
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
//                        Glide.with(this@CreatedEmojiActivity).load(emojiUrl).into(binding.createdEmojiId)
                        loadingDialog.dismiss()
                        val noEmojiDialog = showNoEmojiFoundDialog()
                        noEmojiDialog.show()
                        binding.settingsAdConatiner.visibility=View.INVISIBLE
                        val button_create_new =
                            noEmojiDialog.findViewById<RelativeLayout>(R.id.card_create_new)
                        button_create_new.setOnClickListener {
                            noEmojiDialog.dismiss()
                            finish()
                        }

                        Log.d("TAG", "onBitmapFailed: ${errorDrawable.toString()}")
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }


                    override fun onResourceReady(
                        drawable: Drawable, transition: Transition<in Drawable>?
                    ) {
                        val bitmap = drawable.toBitmap()
                        Log.d("TAG", "onBitmapLoaded: bitmap have been loaded?")
                        // Convert the Bitmap to a Drawable
                        val drawableFromURL = BitmapDrawable(resources, bitmap)
                        shouldShowEmoji(true)
                        viewModel.saveToFile(drawableFromURL, fileName, state) { status, path ->
                            loadingDialog.dismiss()
                            filePath = path
                            binding.lottieCreated.visibility = View.VISIBLE
                            binding.lottieCreated.repeatCount = LottieDrawable.INFINITE
                            binding.lottieCreated.playAnimation()
                            binding.lottieMovingLight.visibility = View.VISIBLE
                            binding.lottieMovingLight.repeatCount = LottieDrawable.INFINITE
                            binding.lottieMovingLight.playAnimation()
                            binding.relativeItem.visibility = View.VISIBLE
                            binding.linearNew.visibility = View.VISIBLE
                            binding.createNewEmojiId.visibility = View.VISIBLE
                            binding.createGif.visibility = View.VISIBLE

                            binding.createdEmojiId.setImageDrawable(
                                drawableFromURL
                            )
                        }
                    }
                })
            }
            //if intent from favourites, collection or my creation
            else {
                binding.createGif.visibility = View.VISIBLE
                binding.lottieCreated.visibility = View.VISIBLE
                binding.lottieCreated.repeatCount = LottieDrawable.INFINITE
                binding.lottieCreated.playAnimation()

                binding.lottieMovingLight.visibility = View.VISIBLE
                binding.lottieMovingLight.repeatCount = LottieDrawable.INFINITE
                binding.lottieMovingLight.playAnimation()
                loadingDialog.dismiss()

                binding.titleCollection.text = intentName

                //intent from favourites
                if (intentFrom.equals(getString(R.string.favourites))) {
                    viewModel.setImageFromFilePath(fileName) { btmp, path ->
                        filePath = path
                        val drawable = BitmapDrawable(resources, btmp)
                        binding.createdEmojiId.setImageDrawable(drawable)
                        binding.addFavEmoji.setImageDrawable(getDrawable(R.drawable.unfavourite_emoji))


//                        binding.constraintFavourite.visibility = View.GONE
                    }

                    //intent from collections
                } else if (intentFrom.equals(getString(R.string.collection))) {
                    Log.d("TAG", "collection filename: $fileName")
                    var btmp = getImageFromAssets(fileName)
                    val bitmapDrawable = BitmapDrawable(resources, btmp)
                    viewModel.saveToFile(bitmapDrawable, fileName, state) { status, path ->
                        filePath = path
                        fileName = path
                    }
                    Glide.with(this).asBitmap().load(btmp).centerCrop()
                        .placeholder(R.drawable.progress_emoji).into(binding.createdEmojiId)
                    binding.constrainShare.visibility = View.GONE

                    //intent from create gifs
                } else if (intentFrom.equals(getString(R.string.createGif))) {
                    Log.d("TAG", "create gif filename: $fileName")
                    var btmp = getImageFromAssets(fileName)
                    // Convert Bitmap to Drawable
                    val bitmapDrawable = BitmapDrawable(resources, btmp)
                    createGif(bitmapDrawable, fileName)
                    binding.constraintFavourite.visibility = View.GONE
                    binding.createGif.visibility = View.GONE
                    binding.constrainDload.visibility = View.GONE

                    //intent from my gifs
                } else if (intentFrom.equals(getString(R.string.mygifs))) {
                    val file = File(fileName)
                    if (file.exists()) {
                        filePath = fileName
                        Glide.with(this).asGif().load(file.absolutePath)
                            .into(binding.createdEmojiId)
                    }
                    binding.constraintFavourite.visibility = View.GONE
                    binding.constrainDload.visibility = View.GONE
                    binding.createGif.visibility = View.GONE
                }

                //intent from my creation
                else {
                    binding.titleCollection.text = intentName
                    viewModel.setImageFromFilePath(fileName) { btmp, path ->
                        filePath = path
                        val drawable = BitmapDrawable(resources, btmp)
                        binding.createdEmojiId.setImageDrawable(drawable)
                    }
                }
                binding.createNewEmojiId.visibility = View.GONE
                binding.linearNew.visibility = View.VISIBLE
                binding.relativeItem.visibility = View.VISIBLE
            }

            viewModel.readListFromPaperDB() { listFromPaper ->
                listFromPaper.forEach {
                    if (fileName.contains(it)) {
                        binding.addFavEmoji.setImageDrawable(getDrawable(R.drawable.unfavourite_emoji))
                    }
                }
            }
        }
        if (!intentFrom.equals(getString(R.string.createGif))) Handler(Looper.getMainLooper()).postDelayed(
            runnable,
            splashTime
        )
        else {
            runnable.run()
        }

        binding.createNewEmojiId.setOnClickListener {
            finish()
        }



        binding.dloadEmoji.setOnClickListener {
            if(!isAlreadyDloaded) {
                if (!isReadStorageAllowed(this@CreatedEmojiActivity) && !isWriteStorageAllowed(this@CreatedEmojiActivity)) {
                    requestStoragePermission(this)
                }
                else{
                    isAlreadyDloaded=true
                    val file = File(filePath)
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Use MediaStore for Android 10 (Q) and above
                        val resolver = applicationContext.contentResolver
                        val contentValues = ContentValues()
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        val imageUri = resolver.insert(contentUri, contentValues)

                        if (imageUri != null) {
                            val outputStream = resolver.openOutputStream(imageUri)
                            outputStream.use { output ->
                                val inputStream = FileInputStream(file)
                                inputStream.use { input ->
                                    input.copyTo(output!!)
                                }
                            }
                        }
                        imageUri
                    } else {
                        // For Android 9 and below, use FileProvider
                        val authority = applicationContext.packageName + ".provider"
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        FileProvider.getUriForFile(applicationContext, authority, file)
                    }

                    Toast.makeText(this, getString(R.string.savedToGalleryToast), Toast.LENGTH_SHORT)
                        .show()
                }

            }
            else{
                Toast.makeText(this, getString(R.string.alreadysavedToast), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.addFavEmoji.setOnClickListener {
            Log.d("TAG", "filename added to fav: $fileName")
            fileName = fileName.removeSuffix(".png")
            val file = File(fileName)
            viewModel.updatePaperDB(file.name, getString(R.string.favListPaperDb)) {
                if (it) {
                    binding.addFavEmoji.setImageResource(R.drawable.unfavourite_emoji)
                    Toast.makeText(
                        this, getString(R.string.addToFavouritesToast), Toast.LENGTH_SHORT
                    ).show()

                } else {
                    binding.addFavEmoji.setImageResource(R.drawable.add_fav)
                    Toast.makeText(
                        this, getString(R.string.removeFromFavouritesToast), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.shareEmoji.setOnClickListener {
            Log.d("TAG", "shareGif filepath: $filePath")
            if(filePath.isEmpty()||filePath.isBlank()){
                Toast.makeText(this, "Can't share emoji right now!", Toast.LENGTH_SHORT).show()
            }
            else {
                if (intentFrom.equals(getString(R.string.mygifs)) || intentFrom.equals(getString(R.string.createGif))) {
                    shareFile("gif", filePath)
                } else {
                    shareFile("png", filePath)
                }
            }
        }

        binding.relativeBack.setOnClickListener {
            finish()
        }

        binding.createGif.setOnClickListener {
            binding.linearNew.visibility = View.INVISIBLE
            createGif(binding.createdEmojiId.drawable, fileName)

        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("Back button pressed")
                finish()
                EmojiKitchenApp.instance!!.setLoadedNativeAd(null)
            }
        })
    }

    private fun initComponents() {
        val repository = emojisRepository(this)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(repository)).get(MainViewModel::class.java)

        if (isInternetAvailable())
            binding.settingsAdConatiner.visibility = View.VISIBLE

    }

    private fun shouldShowEmoji(shouldShow: Boolean) {
        isFineToUseListeners = true
        if (shouldShow) {
            shadAnim(binding.createdEmojiId, "scaleY", 1.0, 300)
            shadAnim(binding.createdEmojiId, "scaleX", 1.0, 300)
        } else {
            shadAnim(binding.createdEmojiId, "scaleY", 0.0, 300)
            shadAnim(binding.createdEmojiId, "scaleX", 0.0, 300)
        }
    }

    private fun receivedData() {
        var fileDetails = Paper.book().read<fileDetails>("temp_list_file")
        fileName = fileDetails!!.fileName
        intentName = fileDetails!!.intentName
        intentFrom = fileDetails!!.intentFrom
        emojiUrl = fileDetails!!.emojiUrl
        databaseKey1 = fileDetails!!.databaseKey1
        databaseKey2 = fileDetails!!.databaseKey2
        state = fileDetails!!.state
    }

    override fun onDestroy() {
        super.onDestroy()
        fileName = ""
        intentName = ""
        intentFrom = ""
        emojiUrl = ""
        databaseKey1 = ""
        databaseKey2 = ""
        state = false
    }

    private fun getImageFromAssets(filePath: String): Bitmap? {
        val assetManager = assets
        val istr: InputStream
        try {
            istr = assetManager.open(filePath)
            return BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun showNoEmojiFoundDialog(): Dialog {
        var binding=CustomDialogEmojiNotFoundBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(binding.root)

        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setGravity(Gravity.CENTER)

        return dialog
    }

    private fun showLoadingDialog(): Dialog {
        val binding = CustomDialogLoadGifBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(binding.root)
        binding.settingsAdConatiner.visibility = View.INVISIBLE
        preLoadNativeForCreatedEmoji()

//        val fl_adplaceholder = dialog.findViewById<FrameLayout>(R.id.settingsAdConatiner)
////        val shimmerFrameLayout = binding.homeNative.shimmerContainerNative
//        val shimmerFrameLayout = dialog.findViewById<ShimmerFrameLayout>(R.id.shimmerContainerNative)
        if (isInternetAvailable()) {
            binding.settingsAdConatiner.visibility = View.VISIBLE
            loadNativeAd(
                this@CreatedEmojiActivity,
                binding.settingsAdConatiner,
                binding.homeNative.shimmerContainerNative,
                BuildConfig.emoji_loading_dilog_native)
        }

        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setGravity(Gravity.CENTER)

        val title = dialog.findViewById<OutlineTextView>(R.id.loading_title)
        if (intentFrom.equals(this.getString(R.string.mygifs))) {
            title.text = getString(R.string.gifLoading)
        } else {
            title.text = getString(R.string.emojiLoading)
        }

        val lottie_loading = dialog.findViewById<LottieAnimationView>(R.id.lottie_loading)
        lottie_loading.repeatCount = LottieDrawable.INFINITE
        lottie_loading.playAnimation()

        val lottie_loading_bg = dialog.findViewById<LottieAnimationView>(R.id.lottie_bg_loading)
        lottie_loading_bg.repeatCount = LottieDrawable.INFINITE
        lottie_loading_bg.playAnimation()

        return dialog
    }

    private fun loadNativeAd(
        context: CreatedEmojiActivity,
        fl_adplaceholder: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout,
        adId:String) {
        EmojiKitchenApp.instance!!.getLoadedNativeAd() { appNative ->
            if (appNative == null && !AppPurchase.getInstance().isPurchased) {
//                printLog("preloaded_welcome_native", "Preloaded WelcomeNative is Null, New request is sent on WelcomeScreen")
                AperoAd.getInstance().loadNativeAdResultCallback(context,
                    adId,
                    R.layout.custom_native_with_media, object :
                        AperoAdCallback() {
                        override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                            super.onNativeAdLoaded(nativeAd)
                            fl_adplaceholder.visibility = View.VISIBLE
                            AperoAd.getInstance().populateNativeAdView(
                                context,
                                nativeAd,
                                fl_adplaceholder,
                                shimmerFrameLayout
                            )
                        }

                        override fun onAdFailedToLoad(adError: ApAdError?) {
                            super.onAdFailedToLoad(adError)
                            fl_adplaceholder.visibility = View.GONE
                        }

                        override fun onAdFailedToShow(adError: ApAdError?) {
                            super.onAdFailedToShow(adError)
                            fl_adplaceholder.visibility = View.GONE
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                        }
                    })
            } else {
//                printLog("preloaded_welcome_native", "Preloaded WelcomeNative is Displayed")
                fl_adplaceholder.visibility = View.VISIBLE
                AperoAd.getInstance().populateNativeAdView(
                    context,
                    appNative,
                    fl_adplaceholder,
                    shimmerFrameLayout
                )
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }


    fun showGifLoadingDialog(): Dialog {
        val binding = CustomDialogLoadGifBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(binding.root)
        binding.settingsAdConatiner.visibility = View.INVISIBLE
        preLoadNativeForCreatedEmoji()


        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setGravity(Gravity.CENTER)

        if (isInternetAvailable()) {
            binding.settingsAdConatiner.visibility = View.VISIBLE
            loadNativeAd(
                this@CreatedEmojiActivity,
                binding.settingsAdConatiner,
                binding.homeNative.shimmerContainerNative,
                BuildConfig.create_gif_dilog_native
            )
        }

        val lottie_loading = dialog.findViewById<LottieAnimationView>(R.id.lottie_loading)
        lottie_loading.repeatCount = LottieDrawable.INFINITE
        lottie_loading.playAnimation()

        val lottie_loading_bg = dialog.findViewById<LottieAnimationView>(R.id.lottie_bg_loading)
        lottie_loading_bg.repeatCount = LottieDrawable.INFINITE
        lottie_loading_bg.playAnimation()

        return dialog
    }

    fun createGif(imageDrawable: Drawable, mfileName: String) {
        var dialog = showGifLoadingDialog()
        dialog.show()

        val splashTime = getString(R.string.loading_emoji_time).toLong()

        val runnable = Runnable {
            // Convert the drawable to BitmapDrawable
            val bitmapDrawable = imageDrawable as BitmapDrawable
            var file = File(mfileName)
            var gifFileName = file.name.split(".").first()
            Log.d("TAG", "gif file Name: ${gifFileName}")

            MainScope().launch(Dispatchers.IO) {
                viewModel.createGif(bitmapDrawable.bitmap, gifFileName) { fileExists, path ->
                    filePath = path!!
                    runOnUiThread {
                        if (fileExists) {
                            Toast.makeText(
                                this@CreatedEmojiActivity,
                                getString(R.string.alreadyExistsGif),
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.linearNew.visibility = View.VISIBLE
//                            dialog.dismiss()
                            finish()
                        } else {
                            if (!intentFrom.equals(getString(R.string.createGif))) {
                                startActivity(
                                    Intent(
                                        this@CreatedEmojiActivity, MyGifActivity::class.java
                                    )
                                )
                                finish()
                                dialog.dismiss()

                            } else {
                                dialog.dismiss()
                                Glide.with(this@CreatedEmojiActivity).asGif().load(path)
                                    .centerCrop().into(binding.createdEmojiId)
                            }
                        }
                    }
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, splashTime)
    }

    fun shareFile(fileType: String, pathFile:String) {
        val file = File(pathFile)
        val shareIntent = Intent(Intent.ACTION_SEND)
        val photoURI: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10 (Q) and above
            val resolver = applicationContext.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/${fileType}")
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val imageUri = resolver.insert(contentUri, contentValues)

            if (imageUri != null) {
                val outputStream = resolver.openOutputStream(imageUri)
                outputStream.use { output ->
                    val inputStream = FileInputStream(file)
                    inputStream.use { input ->
                        input.copyTo(output!!)
                    }
                }
            }
            imageUri
        } else {
            // For Android 9 and below, use FileProvider
            val authority = applicationContext.packageName + ".provider"
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            FileProvider.getUriForFile(applicationContext, authority, file)
        }

        shareIntent.type = "image/${fileType}" // Set the appropriate MIME type for your image
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI)
        kotlin.runCatching {
            startActivity(Intent.createChooser(shareIntent, null))
        }
            .onFailure {
                it.printStackTrace()
            }
    }

    fun preLoadNativeForCreatedEmoji() {
        AperoAd.getInstance().loadNativeAdResultCallback(
            this,
            BuildConfig.new_emoji_native,
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == getString(R.string.storagePermissionCode).toInt()) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                binding.cardLetsStart.visibility = View.INVISIBLE
                // Permission is granted, start the MainActivity
//                onGranted()
//                startActivity(Intent(this@SplashScreen, MainActivity::class.java))

                isAlreadyDloaded=true
                val file = File(filePath)
                val shareIntent = Intent(Intent.ACTION_SEND)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Use MediaStore for Android 10 (Q) and above
                    val resolver = applicationContext.contentResolver
                    val contentValues = ContentValues()
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val imageUri = resolver.insert(contentUri, contentValues)

                    if (imageUri != null) {
                        val outputStream = resolver.openOutputStream(imageUri)
                        outputStream.use { output ->
                            val inputStream = FileInputStream(file)
                            inputStream.use { input ->
                                input.copyTo(output!!)
                            }
                        }
                    }
                    imageUri
                } else {
                    // For Android 9 and below, use FileProvider
                    val authority = applicationContext.packageName + ".provider"
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    FileProvider.getUriForFile(applicationContext, authority, file)
                }

                Toast.makeText(this, getString(R.string.savedToGalleryToast), Toast.LENGTH_SHORT)
                    .show()
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
                    openAppSettings(this@CreatedEmojiActivity)
                }
            }
        }
    }

}