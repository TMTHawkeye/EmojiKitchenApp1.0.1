package com.emojimerger.mixemojis.emojifun.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emojimerger.mixemojis.emojifun.Activities.MixEmojiActivity
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.interfaces.selectedImageCallBack

import pl.droidsonroids.gif.GifImageView
import java.io.IOException
import java.io.InputStream


class EmojiAdapter(
    val context: MixEmojiActivity,
    val emojisList: ArrayList<String>,
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {
    lateinit var callBackInstance : selectedImageCallBack
    private var selectedItems = SparseBooleanArray()
    private var selectedCount = 0
    private val maxSelectionCount = 2


    inner class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emoji: GifImageView = itemView.findViewById(R.id.gif_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_gif, parent, false)
        return EmojiViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val currentEmojiPath = emojisList[position]
        // Set the background color based on the selected state
        if (selectedItems.get(position, false)) {
            holder.itemView.background=context.getDrawable(R.drawable.item_bg_selected)
        } else {
            holder.itemView.background=context.getDrawable(R.drawable.item_bg)
        }
        Glide.with(context).asBitmap()
            .load(getImageFromAssets(currentEmojiPath))
            .centerCrop()
            .placeholder(R.drawable.progress_emoji)
            .into(holder.emoji)

        holder.emoji.setOnClickListener {
            Log.d("TAG", "onBindViewHolder: ${currentEmojiPath}")

            // Toggle the selected state if not exceeding the maximum count
            if (selectedCount < maxSelectionCount || selectedItems.get(position, false)) {
                val isSelected = !selectedItems.get(position, false)
                selectedItems.put(position, isSelected)

                // Update the background color
                if(isSelected){
                    holder.itemView.background=context.getDrawable(R.drawable.item_bg_selected)
                }
                else{
                    holder.itemView.background=context.getDrawable(R.drawable.item_bg)
                }

                // Update the selected count
                selectedCount += if (isSelected) 1 else -1

//                // Update the selected state
//                callBackInstance = context
//                //callback to the mainActivity
//                callBackInstance.setImage(currentEmojiPath)
            }
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


    fun getSelectedEmojiPaths(): List<String> {
        val selectedPaths = mutableListOf<String>()
        for (i in 0 until emojisList.size) {
            if (selectedItems.get(i, false)) {
                selectedPaths.add(emojisList[i])
            }
        }
        return selectedPaths
    }

    fun clearSelectedItems() {
        selectedItems.clear()
        selectedCount = 0
        notifyDataSetChanged()
    }

}