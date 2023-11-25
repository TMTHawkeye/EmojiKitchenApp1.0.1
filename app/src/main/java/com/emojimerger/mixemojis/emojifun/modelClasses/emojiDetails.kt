package com.emojimerger.mixemojis.emojifun.modelClasses

import android.net.Uri

data class emojiDetails(
    val fileName:String="",
    val fileUrl: String="",
    val liked:Int=0,
    val disliked:Int=0,
    val shared:Int=0,
    val downloaded:Int=0
)
