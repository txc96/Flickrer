package com.example.flickrer.compose.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flickrer.FlickrerViewModel
import com.example.flickrer.models.Photo
import com.example.flickrer.models.Tag

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    collapsedImageHeight: Dp = 80.dp,
    expandedImageHeight: Dp = 240.dp,
    collapsedElevation: Dp = 2.dp,
    expandedElevation: Dp = 8.dp,
    title: String = "Title",
    detail: String  = "Detailed text shown when expanded",
    photo: Photo,
    tags: List<Tag>?,
    viewModel: FlickrerViewModel,
    body: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Animate the image height and the elevation when toggling
    //todo for flow row replace with weight modifier
    val imageHeight by animateDpAsState(
        targetValue = if (expanded) expandedImageHeight else collapsedImageHeight,
        animationSpec = tween(durationMillis = 300)
    )
    val elevation by animateDpAsState(
        targetValue = if (expanded) expandedElevation else collapsedElevation,
        animationSpec = tween(durationMillis = 300)
    )

    Surface(
        modifier = modifier
            .clickable {
                if (!expanded) {
                    viewModel.fetchTags(photo.id)
                }
                expanded = !expanded
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
            // Top area (image placeholder here) resizes between collapsed/expanded heights
            Box(
                modifier = Modifier
                    .width(imageHeight)
                    //todo height should determine from the image
                    .height(imageHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFCCCCCC)),
                contentAlignment = Alignment.Center
            ) { body() }

            // Extra text appears only when expanded (animated)
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)) { it / 4 },
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)) { it / 4 }
            ) {
                Column(modifier = Modifier
                    .padding(top = 12.dp)
                ) {
                    if(tags != null) {
                        FlowRow() {
                            tags.map {
                                Text(
                                    text = it.content,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(Color(0xFF6200EE), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .padding(end = 4.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}