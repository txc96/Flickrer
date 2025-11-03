package com.example.flickrer.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flickrer.FlickrerViewModel
import com.example.flickrer.R
import com.example.flickrer.models.Photo

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    collapsedImageWidth: Dp = 80.dp,
    collapsedElevation: Dp = 2.dp,
    expandedElevation: Dp = 8.dp,
    photo: Photo,
    viewModel: FlickrerViewModel,
    focusManager: FocusManager,
    body: @Composable () -> Unit
) {

    // Animate the image height and the elevation when toggling
    val imageWidth by animateDpAsState(
        targetValue = if (photo.expanded) GetScreenWidthDp() else collapsedImageWidth,
        animationSpec = tween(durationMillis = 300)
    )
    val elevation by animateDpAsState(
        targetValue = if (photo.expanded) expandedElevation else collapsedElevation,
        animationSpec = tween(durationMillis = 300)
    )

    Surface(
        modifier = modifier
            .clickable {
                if (!photo.expanded) {
                    viewModel.fetchTags(photo)
                }
                viewModel.expandImage(photo)
            }
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Column(
            modifier = Modifier
                // animateContentSize ensures smooth size transition when the inner content changes
                .animateContentSize(animationSpec = tween(durationMillis = 300))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Box that holds image/content
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFCCCCCC))
                    .then(
                        if(photo.expanded)
                            //todo fix height
                            Modifier.wrapContentHeight().fillMaxWidth()
                        else
                            Modifier.height(collapsedImageWidth).width(collapsedImageWidth)
                    ),
                contentAlignment = Alignment.Center
            ) { body() }

            // Extra text appears only when expanded (animated)
            AnimatedVisibility(
                visible = photo.expanded,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)) { it / 4 },
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)) { it / 4 }
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(top = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.photo_author).format(photo.owner.ifEmpty { "No owner" }),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp),
                    )
                    Text(
                        text = stringResource(R.string.photo_title).format(photo.title.ifEmpty { "No Title" }),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                    //Tags list
                    if(!photo.tags.isNullOrEmpty()) {
                        FlowRow(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            photo.tags.map {
                                Text(
                                    text = it.content,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(Color(0xFF6200EE), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                        .clickable{
                                            // on tag click, update search query and clear focus
                                            viewModel.setSelectedTags(mutableListOf(it.content))
                                            viewModel.fetchPhotos()
                                            focusManager.clearFocus()
                                        }
                                )
                            }
                        }
                    }
                    else{
                        Text(
                            text = "No tags...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GetScreenWidthDp(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    return screenWidthDp
}
