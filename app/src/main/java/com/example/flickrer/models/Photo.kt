package com.example.flickrer.models

// Model class representing a photo from Flickr API
// Added 'tags' property to hold associated tags
// Added 'expanded' property to track expansion state in UI
data class Photo(
    val id: String,
    val owner: String,
    val secret: String,
    val server: String,
    val farm: Int,
    val title: String,
    val ispublic: Int,
    val isfriend: Int,
    val isfamily: Int,
    val tags: List<Tag>? = null,
    val expanded: Boolean = false
)