package com.example.flickrer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flickrer.models.Photo
import com.example.flickrer.models.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL

class FlickrerViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private val _searchText = MutableStateFlow<String?>(null)
    val searchText: StateFlow<String?> = _searchText.asStateFlow()

    private val _selectedTags = MutableStateFlow<List<Tag>?>(null)
    val selectedTags: StateFlow<List<Tag>?> = _selectedTags.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    fun fetchPhotos() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var parameters = ""

                when {
                    !searchText.value.isNullOrEmpty() -> parameters += "&text=${searchText.value}"
                    selectedTags.value != null && !selectedTags.value.isNullOrEmpty() -> parameters += "tags=${(selectedTags.value as Iterable<Any?>).joinToString(",")}"
                    else -> ""
                }
                val url = getApplication<Application>().getString(R.string.get_photos_url).format(
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
                    val stat = JSONObject(body).optString("stat")
                    if (connection.responseCode == HttpURLConnection.HTTP_OK && stat == "ok") {
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

                        sleep(1000) // Simulate loading delay

                        _photos.value = list
                        _error.value = ""
                    } else {
                        val failMessage = JSONObject(body).optString("message")
                        _error.value = if(connection.responseCode == HttpURLConnection.HTTP_OK) "Error: $failMessage"
                        else "HTTP ${connection.responseCode}"

                    }
                    _isLoading.value = false
                }
                connection.disconnect()
            } catch (t: Throwable) {
                Log.d("FlickrerViewModel", "Error: ${t.message}")
                _error.value = t.message ?: "Unknown error"
                _isLoading.value = true
            }
        }
    }

    fun fetchTags(photo: Photo) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val url = getApplication<Application>().getString(R.string.get_tags_url).format(photo.id)

                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                }

                Log.d("FlickrerViewModel", "Request URL: $url")

                connection.inputStream.bufferedReader().use { reader ->
                    val body = reader.readText()
                    val stat = JSONObject(body).optString("stat")
                    if (connection.responseCode == HttpURLConnection.HTTP_OK && stat == "ok") {
                        Log.d("FlickrerViewModel", "Response: $body")
                        val res = JSONObject(body).optJSONObject("photo")
                        val tagList = res?.optJSONObject("tags")?.optJSONArray("tag")
                        val list = mutableListOf<Tag>()
                        tagList?.length()?.let {
                            if(it > 0){
                                for (i in 0 until tagList.length()) {
                                    val obj = tagList.getJSONObject(i)
                                    list += Tag(
                                        id = obj.optString("id"),
                                        author = obj.optString("author"),
                                        authorName = obj.optString("authorName"),
                                        raw = obj.optString("raw"),
                                        content = obj.optString("_content"),
                                        machineTag = obj.optBoolean("machine_tag")
                                    )
                                }
                            }
                        }

                        photo.copy(tags = list)
                        _photos.emit(_photos.value)
                        _error.value = ""
                    } else {
                        val failMessage = JSONObject(body).optString("message")
                        _error.value = if(connection.responseCode == HttpURLConnection.HTTP_OK) "Error: $failMessage"
                        else "HTTP ${connection.responseCode}"
                    }
                    _isLoading.value = false
                }
                connection.disconnect()
            } catch (t: Throwable) {
                Log.d("FlickrerViewModel", "Error: ${t.message}")
                _error.value = t.message ?: "Unknown error"
                _isLoading.value = false
            }
        }
    }

    fun refresh(){
        _searchText.update { null }
        _selectedTags.update { null }
        fetchPhotos()
    }

    fun setText(query: String) {
        _searchText.update { query }
    }

    fun setSelectedTags(tags: List<Tag>) {
        _selectedTags.update { tags }
    }

}