package com.example.flickrer.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.flickrer.FlickrerViewModel

@Composable
fun SearchBar(
    viewModel: FlickrerViewModel,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
    hint: String = "Search",
) {

    val text = remember { mutableStateOf("") }

    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        TextField(
            value = text.value,
            onValueChange = { text.value = it },
            placeholder = { Text(hint) },
            singleLine = true,
            // todo
            /*leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }*/
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    updateText(viewModel, text.value, focusManager)
                },
                onDone = {
                    updateText(viewModel, text.value, focusManager)
                }
            ),
            modifier = Modifier
                // Not implementing since this could be "spammy" to the API and user experience
                // But if we wanted a search that updated as you typed, this is one way to go
                // about it.
                /*.onKeyEvent{
                    when{
                        it.type == KeyEventType.KeyUp -> {
                            updateText(viewModel, text.value, focusManager)
                            true
                        }
                        else -> false
                    }

                }*/
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .onFocusChanged { state ->
                    if (!state.isFocused) {
                        updateText(viewModel, text.value, focusManager)
                    }
                }
        )
    }
}

// Helper method to update text in viewmodel and fetch photos, reducing duplicate code
fun updateText(viewModel: FlickrerViewModel, value: String, focusManager: FocusManager){
    viewModel.setText(value)
    viewModel.fetchPhotos()
    focusManager.clearFocus()
}

// todo preview