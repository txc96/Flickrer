// Requires: implementation("io.coil-kt:coil-compose:<latest-version>")

package com.example.flickrer.compose.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.flickrer.FlickrerViewModel
import com.example.flickrer.R
import com.example.flickrer.models.Photo

@Composable
fun ImageGrid(
    viewModel: FlickrerViewModel,
    focusManager: FocusManager,
    images: List<Photo>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    spacing: Dp = 8.dp,
    maxColumns: Int = 3
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(maxColumns),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier.fillMaxSize(),
        content = {
            items(
                images,
                // set span if photo is expanded
                span = { item ->
                    if(item.expanded) GridItemSpan(currentLineSpan = maxColumns)
                        else GridItemSpan(currentLineSpan = 1)
                }
            ) { photo ->
                ExpandableCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp)),
                    photo = photo,
                    viewModel = viewModel,
                    focusManager = focusManager
                ) {
                    // construct photo url for coil from Flickr response
                    val url = stringResource(R.string.url_format).format(photo.server, photo.id, photo.secret)
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = url,
                        contentDescription = null
                    )
                }
            }
            // append item to the end of the grid, which if rendered, triggers fetching next page
            item{
                LaunchedEffect(Unit) {
                    viewModel.fetchPhotos(viewModel.page.value+1)
                }
            }
        }
    )
}

// todo preview
@Preview(showBackground = true)
@Composable
private fun ImageGridPreview() {
    /*val sample = List(12) { "https://picsum.photos/300?random=$it" }
    ImageGrid(images = sample)*/
}