package com.emojimerger.mixemojis.emojifun.modelClasses

class emojiDetails {
    var fileName: String = ""
    var fileUrl: String = ""
    var liked: Int = 0
    var disliked: Int = 0
    var shared: Int = 0
    var downloaded: Int = 0

    // Empty constructor required by Firebase for deserialization
    constructor()

    constructor(
        fileName: String,
        fileUrl: String,
        liked: Int,
        disliked: Int,
        shared: Int,
        downloaded: Int
    ) {
        this.fileName = fileName
        this.fileUrl = fileUrl
        this.liked = liked
        this.disliked = disliked
        this.shared = shared
        this.downloaded = downloaded
    }
}
