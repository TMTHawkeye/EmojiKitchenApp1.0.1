package com.emojimerger.mixemojis.emojifun.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.ViewModel
import com.emojimerger.mixemojis.emojifun.modelClasses.emojiDetails
import com.emojimerger.mixemojis.emojifun.repositories.emojisRepository
import java.io.File
import java.io.Serializable

class MainViewModel(private val repository: emojisRepository) : ViewModel() ,Serializable{

    fun checkImageInDatabase(databaseKey1: String, databaseKey2: String, emoji1: String?, emoji2: String?, date: String, callback: (String,String,Boolean) -> Unit) {
        repository.checkImageInDatabase(databaseKey1, databaseKey2, emoji1, emoji2, date, callback)
    }

    fun saveToFile(bitmapImage: BitmapDrawable, nameOfFile: String,state:Boolean,callback: (Boolean,String)-> Unit) {
        repository.saveToFile(bitmapImage, nameOfFile,state,callback)
    }

    fun updatePaperDB(fileName: String, listName:String, callback: (Boolean) -> Unit) {
        repository.updatePaperDB(fileName,listName,callback)
    }

    fun readListFromPaperDB(callback: (ArrayList<String>)->Unit){
        repository.readListFromPaperDB(callback)
    }

    fun setImageFromFilePath(filePath:String,callback: (Bitmap,String) -> Unit){
        repository.setImageFromFilePath(filePath,callback)
    }

    fun getListOfImagePathsFromAssets(context: Context,callback: (ArrayList<String>) -> Unit){
        repository.getListOfImagePathsFromAssets(context,callback)
    }

    fun createGif(bitmap:Bitmap,fileName:String,callback: (Boolean,path:String?) -> Unit){
        repository.createGif(bitmap,fileName,callback)
    }

    fun getListOfFilesFromInternalStorage(folderName:String,callback: (List<File>) -> Unit){
        repository.getListOfFilesFromInternalStorage(folderName,callback)
    }

    fun updateDatabaseField(fileName: String,liked:Boolean,disliked:Boolean,shared:Boolean,downloaded:Boolean,callback: (emojiDetails)->Unit){
        repository.updateDatabaseField(fileName,liked,disliked,shared,downloaded,callback)
    }
    fun readFieldsFromDatabase(fileName: String,callback: (emojiDetails)->Unit){
        repository.readFieldsFromDatabase(fileName,callback)
    }
    fun checkFileInInternalStorage(fileName1:String,fileName2:String,callback: (Bitmap) -> Unit){
        repository.checkFileInInternalStorage(fileName1,fileName2,callback)
    }


    //    fun zipEmojisFromFirebaseStorage(nameOfFolder:String,callback: (Boolean) -> Unit){
//        repository.zipEmojisFromFirebaseStorage(nameOfFolder,callback)
//    }







}