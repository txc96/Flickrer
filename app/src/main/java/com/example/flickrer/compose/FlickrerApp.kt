package com.example.flickrer.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flickrer.FlickrerViewModel
import com.example.flickrer.ui.ImageGrid
import androidx.compose.runtime.collectAsState

@Composable
fun FlickrerApp() {
    val viewModel = viewModel<FlickrerViewModel>()
    Column {
        SearchBar(viewModel = viewModel)
        ImageGrid(viewModel.photos.collectAsState().value)
    }
}