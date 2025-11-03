package com.example.flickrer.models;

data class Tag (
    val id: String,
    val author: String,
    val authorName: String,
    val raw: String,
    val content: String,
    val machineTag: Boolean
)
