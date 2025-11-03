package com.example.flickrer.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flickrer.FlickrerViewModel
import com.example.flickrer.compose.components.ImageGrid
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.example.flickrer.R
import com.example.flickrer.compose.components.Overlay
import com.example.flickrer.compose.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)

// todo bug with focus on first two items
@Composable
fun FlickrerApp() {
    val viewModel = viewModel<FlickrerViewModel>()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }


    // Launched effect for error snackbar
    LaunchedEffect(Unit) {
        viewModel.error.collect {
            if(!it.isEmpty())
                snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(),
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Refresh button that clears current search query and gets latest
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_refresh_24),
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column (
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) {
            SearchBar(
                viewModel = viewModel,
                focusManager = focusManager
            )
            ImageGrid(
                viewModel = viewModel,
                focusManager = focusManager,
                viewModel.photos.collectAsState().value
            )
        }

        // API loading overlay
        if(viewModel.isLoading.collectAsState().value){
            Overlay()
        }
    }

}