package com.example.flickrer

import android.app.Application
import android.util.Log
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flickrer.models.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class FlickrerViewModel(application: Application) : AndroidViewModel(application) {
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private val _searchText = MutableStateFlow<String?>(null)
    val searchText: StateFlow<String?> = _searchText.asStateFlow()

    private val _tags = MutableStateFlow<List<String>?>(null)
    val tags: StateFlow<List<String>?> = _tags.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var parameters = ""

                when {
                    !searchText.value.isNullOrEmpty() -> parameters += "&text=${searchText.value}"
                    tags.value != null && !tags.value.isNullOrEmpty() -> parameters += "tags=${(tags.value as Iterable<Any?>).joinToString(",")}"
                    else -> ""
                }
                val url = getApplication<Application>().getString(R.string.flickr_url).format(
                    if(!searchText.value.isNullOrEmpty()) "search" else "getRecent",
                            parameters
                )

                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                }

                Log.d("FlickrerViewModel", "Request URL: $url")

                connection.inputStream.bufferedReader().use { reader ->
                    val body = reader.readText()
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("FlickrerViewModel", "Response: $body")
                        val res = JSONObject(body).optJSONObject("photos")
                        val pageNum = res?.optInt("page")
                        val maxPages = res?.optInt("pages")
                        val photoList = res?.optJSONArray("photo")
                        val list = mutableListOf<Photo>()
                        photoList?.length()?.let {
                            if(it > 0){
                                for (i in 0 until photoList.length()) {
                                    val obj = photoList.getJSONObject(i)
                                    list += Photo(
                                        id = obj.optString("id"),
                                        owner = obj.optString("owner"),
                                        secret = obj.optString("secret"),
                                        server = obj.optString("server"),
                                        farm = obj.optInt("farm"),
                                        title = obj.optString("title"),
                                        ispublic = obj.optInt("ispublic"),
                                        isfriend = obj.optInt("isfriend"),
                                        isfamily = obj.optInt("isfamily")
                                    )
                                }
                            }
                        }

                        _photos.value = list
                        _error.value = null
                    } else {
                        _error.value = "HTTP ${connection.responseCode}"
                    }
                }
                connection.disconnect()
            } catch (t: Throwable) {
                Log.d("FlickrerViewModel", "Error: ${t.message}")
                _error.value = t.message ?: "Unknown error"
            }
        }
    }

    fun setText(query: String) {
        _searchText.update { query }
    }

    fun setTags(tags: List<String>) {
        _tags.update { tags }
    }

}