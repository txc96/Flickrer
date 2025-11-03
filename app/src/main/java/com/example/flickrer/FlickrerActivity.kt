package com.example.flickrer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.flickrer.compose.FlickrerApp

class FlickrerActivity : AppCompatActivity() {
    private val vm: FlickrerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        vm.fetchPhotos()
        setContent {
            FlickrerApp()
        }
    }
}