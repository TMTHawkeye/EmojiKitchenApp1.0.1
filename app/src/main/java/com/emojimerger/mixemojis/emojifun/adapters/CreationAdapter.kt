package com.emojimerger.mixemojis.emojifun.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emojimerger.mixemojis.emojifun.Activities.CreatedEmojiActivity
import com.emojimerger.mixemojis.emojifun.R
import com.emojimerger.mixemojis.emojifun.modelClasses.fileDetails
import io.paperdb.Paper
import pl.droidsonroids.gif.GifImageView
import java.io.File

class CreationAdapter(
    var context: Context,
    var listofMatchedFiles: List<File>,
    var intentFrom: String
) :
    RecyclerView.Adapter<CreationAdapter.viewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        if (intentFrom.equals(context.getString(R.string.mygifs))) {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_gif, parent, false)
            return viewHolder(itemView)
        } else {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_collection, parent, false)
            return viewHolder(itemView)
        }
    }

    override fun getItemCount(): Int {
        return listofMatchedFiles.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val fileName = listofMatchedFiles[position]
        if (intentFrom.equals(context.getString(R.string.mygifs))) {
            Glide.with(context).asGif()
                .placeholder(R.drawable.progress_emoji)
                .load(fileName.absolutePath).into(holder.gif)
        } else {
            val bitmap = BitmapFactory.decodeFile(fileName.absolutePath)
//            holder.imgCollection.setImageBitmap(bitmap)
            Glide.with(context).asBitmap()
                .placeholder(R.drawable.progress_emoji)
                .load(bitmap).into(holder.imgCollection)

        }
        holder.itemView.setOnClickListener {
            holder.itemView.background=context.getDrawable(R.drawable.item_bg_selected)
            val delayMillis = 1000L
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                holder.itemView.background = context.getDrawable(R.drawable.item_bg)
            }, delayMillis)
            val fileDetails = fileDetails(
                "", fileName.absolutePath, "Emoji ${position + 1}", false, "", "", intentFrom
            )
            Paper.book().write<fileDetails>("temp_list_file", fileDetails)
            context.startActivity(
                Intent(context, CreatedEmojiActivity::class.java)
            )

        }
    }

    inner class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCollection: ImageView =
            itemView.findViewById(R.id.img_collection) ?: ImageView(context)
        val gif: GifImageView = itemView.findViewById(R.id.gif_item) ?: GifImageView(context)
    }

    fun notifyChange(){
        notifyDataSetChanged()
    }



}