package com.emojimerger.mixemojis.emojifun.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emojimerger.mixemojis.emojifun.Activities.CreatedEmojiActivity
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.modelClasses.fileDetails
import io.paperdb.Paper
import java.io.File
import java.io.IOException
import java.io.InputStream


class CollectionAdapter(
    val context: Context,
    val emojisList: ArrayList<String>,
    val intentFrom: String,
) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    inner class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emoji: ImageView = itemView.findViewById(R.id.img_collection)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_collection, parent, false)
        return CollectionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val currentEmojiPath = emojisList[position]
        Glide.with(context).asBitmap()
            .load(getImageFromAssets(currentEmojiPath))
            .centerCrop()
            .placeholder(R.drawable.progress_emoji)
            .into(holder.emoji)

        holder.itemView.setOnClickListener {
            holder.itemView.background=context.getDrawable(R.drawable.item_bg_selected)
            val delayMillis = 1000L
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                holder.itemView.background = context.getDrawable(R.drawable.item_bg)
            }, delayMillis)

            val fileDetails= fileDetails(
                "",currentEmojiPath,"Emoji ${position+1}"
                ,false,"","", intentFrom)
            Paper.book().write<fileDetails>("temp_list_file",fileDetails)
            context.startActivity(
                Intent(context, CreatedEmojiActivity::class.java)
            )
        }

    }

    private fun getImageFromAssets(filePath: String): Bitmap? {
        val assetManager = context.assets
        val istr: InputStream
        try {
            istr = assetManager.open(filePath)
            return BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun getItemCount(): Int {
        return emojisList.size
    }
}