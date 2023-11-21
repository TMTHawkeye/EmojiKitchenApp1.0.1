package com.emojimerger.mixemojis.emojifun.repositories

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.AnimatedGifEncoder
import com.emojimerger.mixemojis.emojifun.emojiMixerUtils.EmojiMixer
import com.emojimerger.mixemojis.emojifun.modelClasses.emojiDetails
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.paperdb.Paper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class emojisRepository(context: Activity) {
    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var databaseReference: DatabaseReference? = null
    private var mcontext: Activity? = null

    //initializing firebase components, context etc.
    init {
        mcontext = context
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.getReferenceFromUrl("gs://emoji-mixer-40a84.appspot.com/")
        databaseReference =
            FirebaseDatabase.getInstance("https://emoji-mixer-40a84-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference()
    }

    // Method to check if an image exists in the Firebase database
    fun checkImageInDatabase(
        databaseKey1: String,
        databaseKey2: String,
        emoji1: String?,
        emoji2: String?,
        date: String,
        callback: (String, String, Boolean) -> Unit
    ) {
        databaseReference!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(databaseKey1)) {
                    Log.d("TAG", "Image loaded from firebase: ${databaseKey1}")
                    // Key1 exists in the database
                    val imageDetails =
                        dataSnapshot.child(databaseKey1).getValue(emojiDetails::class.java)
                    var fileUri=Uri.parse(imageDetails!!.fileUrl)
                    callback(
                        fileUri.toString(),
                        imageDetails.fileName.split(".").first(),
                        false
                    )
                    Log.d("TAG", "Image loaded from firebase: ${imageDetails.fileUrl}")

                } else if (dataSnapshot.hasChild(databaseKey2)) {
                    Log.d("TAG", "Image loaded from firebase: ${databaseKey2}")

                    // Key2 exists in the database
                    val imageDetails =
                        dataSnapshot.child(databaseKey2).getValue(emojiDetails::class.java)
                    var fileUri=Uri.parse(imageDetails!!.fileUrl)
                    callback(
                        fileUri.toString(),
                        imageDetails.fileName.split(".").first(),
                        false
                    )
                    Log.d("TAG", "Image loaded from firebase: ${imageDetails.fileUrl}")

                } else {
                    // Keys do not exist in the database
                    mixEmojis(emoji1!!, emoji2!!, date) { emojiUrl, fileName ->
                        callback(emojiUrl, fileName, true)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors
                Log.d("TAG", "onCancelled: ${databaseError.message}")
            }
        })

    }

    //method to store firestore generated url in realtime database
    private fun storeStringInDatabase(
        fileName: String,
        obtainedURL: String,
    ): Boolean {
        var uploadDataStatus = false
        val fileDetails = emojiDetails("${fileName}.png", "$obtainedURL", 0, 0, 0, 0)
//        val (databaseUniqueKey) = fileName
        var newRef = databaseReference!!.child(fileName)
        newRef.setValue(fileDetails)
            .addOnSuccessListener {
                uploadDataStatus = true
                Log.d(
                    "TAG",
                    "storeStringInDataBaseSuccess: file(${fileName}.png) with url($obtainedURL) have been saved successfully in Realtime Databse!"
                )
            }
            .addOnFailureListener {
                uploadDataStatus = false
                // Handle any errors
                Log.d("TAG", "storeStringInDataBaseFailure: ${it.message}")
            }
        return uploadDataStatus
    }

    //method to store image as a file in firestore
    private fun uploadToFireStore(
        str: String,
        nameOfFile: String,
        callback: (Boolean,path:String) -> Unit
    ) {
        var file = File(str)
        var filePath = Uri.fromFile(file)

        var ref: StorageReference = storageReference!!.child("emojis/$nameOfFile")
        ref.putFile(filePath)
            .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
                    ref.downloadUrl
                        .addOnSuccessListener { uri ->
                            var downloadURL = uri.toString()
                            Log.d("TAG", "Obtained url from firestore is: " + downloadURL)
                            callback(storeStringInDatabase(nameOfFile, downloadURL),"")

                        }
                }

            }).addOnFailureListener {
                callback(false,"")

            }

    }

    //method to make bitmap ready to save in internal memory
    fun saveToFile(
        bitmapDrawable: BitmapDrawable,
        filename: String,
        state: Boolean,
        callback: (Boolean,path:String) -> Unit
    ) {
        val sanitizedFileName = sanitizeFilename(filename)!!
        try {
            if (bitmapDrawable.getBitmap() != null) {
                val bt: Bitmap = bitmapDrawable.getBitmap()
                return saveToInternalStorage(bt, sanitizedFileName, state, callback)
            }
            else{
                callback(false,"")
            }
        } catch (e: Exception) {
            Log.d("TAG", "saveBitmapToFileError: ${e.message}")
            callback(false,"")
        }
    }

    //method to mix two selected emojis
    private fun mixEmojis(
        emoji1: String,
        emoji2: String,
        date: String,
        callback: (String, String) -> Unit
    ) {

        val em = EmojiMixer(
            emoji1,
            emoji2,
            date,
            mcontext!!,
            object : EmojiMixer.EmojiListener {
                override fun onSuccess(emojiUrl: String, fileName: String) {
                    Log.d("TAG", "After merging emojis new emoji's url is: $emojiUrl")
//                setImageFromUrl(binding.selectedEmoji, emojiUrl)
                    callback(emojiUrl, fileName)
                }

                override fun onFailure(
                    failedemojiUrl: String?,
                    fileName: String
                ) {
//                Log.d("TAG", "Failure reason of mixing two emojis is: " + failureReason)
                    //check, if merged image exists in out app already?
//                var newFileName = sanitizeFilename(failedemojiUrl!!)
//                checkFileInInternalStorage(newFileName!!)
                    callback(failedemojiUrl!!, fileName)

                }

            })
        val thread = Thread(em)
        thread.start()
    }

    //method to store file in internal memory
    private fun saveToInternalStorage(
        bitmapImage: Bitmap,
        nameOfFile: String,
        state: Boolean,
        callback: (Boolean,path:String) -> Unit
    ) {
        val context = mcontext
        try {
            // Create a new Bitmap with white background
            val backgroundBitmap = Bitmap.createBitmap(
                bitmapImage.width, bitmapImage.height,
                Bitmap.Config.ARGB_8888
            )
            val backgroundCanvas = Canvas(backgroundBitmap)
            backgroundCanvas.drawColor(Color.WHITE)

            // Draw the original bitmap on top of the white background
            val combinedBitmap = Bitmap.createBitmap(
                backgroundBitmap.width, backgroundBitmap.height,
                backgroundBitmap.config
            )
            val combinedCanvas = Canvas(combinedBitmap)
            combinedCanvas.drawBitmap(backgroundBitmap, 0f, 0f, null)
            combinedCanvas.drawBitmap(bitmapImage, 0f, 0f, null)

            val file = File(
                context!!.getExternalFilesDir(mcontext!!.getString(R.string.my_creationFolderName)),
                "$nameOfFile.png"
            )
            val fileOutputStream = FileOutputStream(file)
            combinedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()
            if (state) {
                uploadToFireStore(file.absolutePath, nameOfFile, callback)
            }
            callback(true,file.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
            callback(true,"")

        }
    }

    //method to extract filename from the path of file
    private fun sanitizeFilename(filename: String): String? {
        val pathComponents: List<String> = filename.split("/")
        val fullName = pathComponents[pathComponents.size - 1]
//        val (fileName, fileExtension) = fullName.split(".")
        return fullName
    }

    //method to update a key from the database
    fun updateDatabaseField(
        fileName: String,
        incrementLiked: Boolean,
        incrementDisliked: Boolean,
        incrementShared: Boolean,
        incrementDownloaded: Boolean,
        callback: (emojiDetails) -> Unit
    ) {
        val databaseUniqueKey = fileName.split(".").first()
        val fileRef = databaseReference!!.child(databaseUniqueKey)

        fileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentDetails = snapshot.getValue(emojiDetails::class.java)
                currentDetails?.let {
                    var likedCount = it.liked
                    var dislikedCount = it.disliked
                    var sharedCount = it.shared
                    var downloadedCount = it.downloaded

                    if (incrementLiked) {
                        likedCount += 1
                    } else if (incrementDisliked) {
                        dislikedCount += 1
                        if (likedCount != 0) {
                            likedCount -= 1 // Decrement liked count by 1
                        }
                    } else if (incrementShared) {
                        sharedCount += 1
                    } else if (incrementDownloaded) {
                        downloadedCount += 1
                    }

                    val updatedDetails = emojiDetails(
                        it.fileName,
                        it.fileUrl,
                        likedCount,
                        dislikedCount,
                        sharedCount,
                        downloadedCount
                    )

                    fileRef.setValue(updatedDetails)
                        .addOnSuccessListener {
                            Log.d("TAG", "All fields updated successfully.")
                            callback(updatedDetails)
                        }
                        .addOnFailureListener {
                            Log.d("TAG", "Failed to update fields: ${it.message}")
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "Failed to read value.", error.toException())
            }
        })
    }

    //to read like,dislike,share and dload fields from database
    fun readFieldsFromDatabase(fileName: String, callback: (emojiDetails) -> Unit) {
        val databaseUniqueKey = fileName.split(".").first()
        val fileRef = databaseReference!!.child(databaseUniqueKey)

        fileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emojiDetails = snapshot.getValue(emojiDetails::class.java)
                emojiDetails?.let { callback(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "Failed to read emojiDetails: ${error.message}")
            }
        })
    }

    //to fetch image when there is no internet connection available
    fun checkFileInInternalStorage(
        fileName1: String,
        fileName2: String,
        callback: (Bitmap) -> Unit
    ) {
        val directory = File(
            mcontext!!.getExternalFilesDir(mcontext!!.getString(R.string.my_creationFolderName)),
            ""
        )
        val files = directory.listFiles()
        println("Size of files List in unzipped_Emojis folder:  ${files.size}")

        for ((index, file) in files!!.withIndex()) {
            val filesubString2 = file.name.substring(0, file.name.length - 4)
            Log.d("TAG", "Names for comparison are: $fileName1 and $filesubString2")

            if (fileName1 == filesubString2 || fileName2 == filesubString2) {
                Log.d("TAG", "File $filesubString2 exists in Internal storage")
                // Load the bitmap from the file
                val bitmap = BitmapFactory.decodeFile(file.path)
                callback(bitmap)
            } else {
                // Check if it's the last file
                if (index == files.lastIndex) {
                    Log.d(
                        "TAG",
                        "None of the fileName exist in Internal Storage and it's the last file."
                    )
                }
            }
        }

    }

    fun updatePaperDB(fileName: String, listName: String, callback: (Boolean) -> Unit) {
        var listofFiles = Paper.book().read("$listName", ArrayList<String>())
        Log.d("TAG", "updatePaperDB: $fileName")
        if (listofFiles != null) {
            if (!listofFiles.contains(fileName)) {
                listofFiles.add(fileName)
                Paper.book().write("$listName", listofFiles)
                callback(true)
            } else {
                listofFiles.remove(fileName) // Remove the fileName if it already exists
                Paper.book().write("$listName", listofFiles)
                callback(false)
            }
        }
    }

    fun readListFromPaperDB(callback: (ArrayList<String>) -> Unit) {
        var listofFiles = Paper.book().read("listofFav", ArrayList<String>())
        listofFiles?.let { callback(it) }
    }

    fun setImageFromFilePath(filePath: String, callback: (Bitmap,path:String) -> Unit) {
        val file = File(filePath)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            callback(bitmap,file.absolutePath)

        } else {
            Log.e("TAG", "File does not exist at path: $filePath")
        }
    }

    fun getListOfImagePathsFromAssets(context: Context, callback: (ArrayList<String>) -> Unit) {
        val imagePathList: ArrayList<String> = ArrayList()
        val assetPath = context.getString(R.string.assetPath) // asset subfolder path
        try {
            val files = context.assets.list(assetPath)
            if (files != null) {
                for (file in files) {
                    if (file.endsWith(".png")) {
                        imagePathList.add(assetPath + File.separator + file)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        callback(imagePathList)
    }

    fun createGif(inputImage: Bitmap, fileName: String,callback: (Boolean,path:String?) -> Unit) {
        try {
            val outputGifPath =
                File(
                    mcontext!!.getExternalFilesDir(mcontext!!.getString(R.string.my_created_gifs_folderName)),
                    "$fileName.gif"
                )

            if (!outputGifPath.exists()) {
                val outputStream = FileOutputStream(outputGifPath.absolutePath)
                val numFrames = 20
                val frameDelay = 100 // in milliseconds

                val frames = ArrayList<Bitmap>()
                for (i in 0 until numFrames) {
                    val outputBitmap = Bitmap.createBitmap(
                        inputImage.width,
                        inputImage.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(outputBitmap)
                    canvas.drawColor(mcontext!!.getColor(R.color.white), PorterDuff.Mode.SRC)

                    val randomTranslateX =
                        (Math.random() * 50).toFloat() - 25
                    val randomTranslateY =
                        (Math.random() * 50).toFloat() - 25
                    val randomRotation =
                        (Math.random() * 20).toFloat() - 10

                    canvas.translate(randomTranslateX, randomTranslateY)
                    canvas.rotate(
                        randomRotation,
                        inputImage.width / 2.toFloat(),
                        inputImage.height / 2.toFloat()
                    ) // Apply random rotation

                    canvas.drawBitmap(inputImage, 0f, 0f, null)

                    frames.add(outputBitmap)

                    // Add delay between frames
                    Thread.sleep(frameDelay.toLong())
                }

                val gifEncoder =
                    AnimatedGifEncoder()
                gifEncoder.start(outputStream)
                gifEncoder.setDelay(frameDelay) // Set the frame delay
                gifEncoder.setRepeat(0) // 0 means loop forever

                frames.forEach { frame ->
                    gifEncoder.addFrame(frame)
                }

                Log.d("TAG", "GIF created successfully at: $outputGifPath")
                gifEncoder.finish()
                outputStream.close()
                callback(false,outputGifPath.absolutePath)
            }
            else{
                callback(true,outputGifPath.absolutePath)
            }
        } catch (e: Exception) {
            Log.d("TAG", "Gif Creation Exception: ${e.message}")
            callback(true,null)
        }

//        // Display the created GIF
//        val gifFile = File(outputGifPath)
//        val gifDrawable = GifDrawable(gifFile)
//        binding.createdEmojiId.setImageDrawable(gifDrawable)

    }

    fun getListOfFilesFromInternalStorage(folderName:String,callback: (List<File>) -> Unit){
        val directory = File(mcontext!!.getExternalFilesDir("$folderName"), "")
        callback(directory.listFiles()?.toList() ?: emptyList())
    }




//    fun zipEmojisFromFirebaseStorage(nameOfFolder:String,callback: (Boolean) -> Unit) {
//        val filesRef = storageReference!!.child("$nameOfFolder")
//        val directory = File(mcontext!!.getExternalFilesDir("zippedCollectionEmojis"), "$nameOfFolder")
//
//        if (!directory.exists()) {
//            directory.mkdir()
//        }
//        val mypath = File(directory, "${nameOfFolder}.zip")
//
//        val zipOutputStream = ZipOutputStream(FileOutputStream(mypath))
//
//        filesRef.listAll()
//            .addOnSuccessListener { listResult ->
//                val fileCount = listResult.items.size
//                var processedCount = 0
//                listResult.items.forEach { item ->
//                    val localFile = File.createTempFile("$nameOfFolder", "jpg")
//                    item.getFile(localFile)
//                        .addOnSuccessListener {
//                            // Add the file to the zip file
//                            val entry = ZipEntry(item.name)
//                            zipOutputStream.putNextEntry(entry)
//                            val fileBytes = localFile.readBytes()
//                            zipOutputStream.write(fileBytes)
//                            zipOutputStream.closeEntry()
//
//                            // Check if it's the last file and then close the zipOutputStream
//                            processedCount++
//                            if (processedCount == fileCount) {
//                                zipOutputStream.close()
//                                val targetDir =
//                                    File(mcontext!!.getExternalFilesDir("unzipped_Collection_Emojis"), "")
//                                unzipFile(mypath, targetDir) {
//                                    callback(it)
//                                }
//                                // Here, you can initiate the download of the zip file to the device
//                                // Use localZipFile for further operations
//                            }
//                        }
//                        .addOnFailureListener {
//                            // Handle any errors
//                        }
//                }
//            }
//            .addOnFailureListener {
//                // Handle any errors
//            }
//    }

//    fun unzipFile(zipFile: File, targetDirectory: File, callback: (Boolean) -> Unit) {
//        val buffer = ByteArray(1024)
//        try {
//            // Create target directory if it doesn't exist
//            if (!targetDirectory.exists()) {
//                targetDirectory.mkdirs()
//            }
//            // Create ZipInputStream to read the zip file
//            val zipInputStream = ZipInputStream(FileInputStream(zipFile))
//            var zipEntry: ZipEntry? = zipInputStream.nextEntry
//            // Iterate through each entry in the zip file
//            while (zipEntry != null) {
//                val newFile = File(targetDirectory, zipEntry.name)
//                // Create the file
//                if (zipEntry.isDirectory) {
//                    newFile.mkdirs()
//                } else {
//                    // Create any necessary directories
//                    newFile.parentFile?.mkdirs()
//                    // Write the file content
//                    val fileOutputStream = FileOutputStream(newFile)
//                    var len: Int
//                    while (zipInputStream.read(buffer).also { len = it } > 0) {
//                        fileOutputStream.write(buffer, 0, len)
//                    }
//                    fileOutputStream.close()
//                }
//                zipEntry = zipInputStream.nextEntry
//            }
//            zipInputStream.closeEntry()
//            zipInputStream.close()
//            callback(true)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            callback(false)
//        }
//    }

}