// kotlin
// Requires: implementation("io.coil-kt:coil-compose:<latest-version>")

package com.example.flickrer.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.example.flickrer.FlickrerViewModel
import com.example.flickrer.models.Photo
import com.example.flickrer.R
import com.example.flickrer.compose.components.ExpandableCard
import com.example.flickrer.models.Tag

@Composable
fun ImageGrid(
    viewModel: FlickrerViewModel,
    images: List<Photo>,
    tags: List<Tag>?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    spacing: Dp = 8.dp
) {
    val scrollState = ScrollState(0)
    //todo need to add scrolling
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        FlowRow(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            maxItemsInEachRow = 3
        ) {
            images.map {
                ExpandableCard(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                    photo = it,
                    tags = tags,
                    viewModel = viewModel
                ) {
                    val url = stringResource(R.string.url_format).format(it.server, it.id, it.secret)
                    AsyncImage(
                        modifier = Modifier.fillMaxWidth(),
                        model = url,
                        contentDescription = null
                    )
                }
            }
        }
    }

    /*LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(images) { _, photo ->
            ExpandableCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
            ) {
                val url = stringResource(R.string.url_format).format(photo.server, photo.id, photo.secret)
                AsyncImage(
                    model = url,
                    contentDescription = null
                )
            }
        }
    }*/
}

@Preview(showBackground = true)
@Composable
private fun ImageGridPreview() {
    /*val sample = List(12) { "https://picsum.photos/300?random=$it" }
    ImageGrid(images = sample)*/
}