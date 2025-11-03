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
import kotlin.text.get
import kotlin.text.set

class FlickrerViewModel(application: Application) : AndroidViewModel(application) {

    // region stateflows
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _photos = MutableStateFlow<MutableList<Photo>>(mutableListOf())
    val photos: StateFlow<MutableList<Photo>> = _photos.asStateFlow()

    private val _page = MutableStateFlow<Int>(0)
    val page: StateFlow<Int> = _page.asStateFlow()

    private val _searchText = MutableStateFlow<String?>(null)
    val searchText: StateFlow<String?> = _searchText.asStateFlow()

    private val _selectedTags = MutableStateFlow<MutableList<String>>(mutableListOf())
    val selectedTags: StateFlow<MutableList<String>> = _selectedTags.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    // endregion

    // region api calls

    //todo these methods could be streamlined into one to reduce duplicate code
    // or broken out into helper methods

    // Fetch photos from Flickr API
    // parameter: page - page number for pagination (defaults to 1 aka first result)
    fun fetchPhotos(page: Int = 0) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Build URL with parameters
                var parameters = ""

                when {
                    !searchText.value.isNullOrEmpty() -> parameters += "&text=${searchText.value}"
                    selectedTags.value.isNotEmpty() -> parameters += "&tags=${(selectedTags.value as Iterable<Any?>).joinToString(",")}"
                    page > 1 -> parameters += "&page=$page"
                    else -> ""
                }
                val url = getApplication<Application>().getString(R.string.get_photos_url).format(
                    if(!searchText.value.isNullOrEmpty() || selectedTags.value.isNotEmpty()) "search" else "getRecent",
                            parameters
                )

                // Open connection
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                }

                Log.d("FlickrerViewModel- - Flickr Request", "Request URL: $url")

                // Read response
                connection.inputStream.bufferedReader().use { reader ->
                    val body = reader.readText()
                    val stat = JSONObject(body).optString("stat")
                    if (connection.responseCode == HttpURLConnection.HTTP_OK && stat == "ok") {
                        Log.d("FlickrerViewModel - Flickr Response", "Response: $body")
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

                        // Simulate loading delay
                        sleep(1000)

                        // amend new photos to the end of the list
                        _photos.value = if(page > 1) _photos.value.plus(list).toMutableList() else list
                        // set current page, ensuring it does not exceed max pages. Uses parameter page
                        // instead of pageNum from response due to viewmodel handling math
                        _page.value = page.coerceAtMost(maxPages?.minus(1) ?: 1)
                        // clear error
                        _error.value = ""
                    } else {
                        // Handle error response
                        // get message from response body, guaranteed to exist if stat != ok
                        val failMessage = JSONObject(body).optString("message")
                        // set error message, prioritizing HTTP first
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

                Log.d("FlickrerViewModel - Flickr Request", "Request URL: $url")

                connection.inputStream.bufferedReader().use { reader ->
                    val body = reader.readText()
                    val stat = JSONObject(body).optString("stat")
                    if (connection.responseCode == HttpURLConnection.HTTP_OK && stat == "ok") {
                        Log.d("FlickrerViewModel - Flickr Response", "Response: $body")
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

                        // update photo with list of tags
                        _photos.update { photos ->
                            photos.map {
                                p -> if(p.id == photo.id) p.copy(tags = list) else p
                            }.toMutableList()
                        }
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

    // endregion

    // region Helper methods
    fun refresh(){
        _searchText.update { null }
        _selectedTags.update { mutableListOf() }
        fetchPhotos()
    }

    fun expandImage(inPhoto: Photo){
        //todo compose not recomposing on photo expand change?
        _photos.update { photos ->
            photos.map{
                photo -> if(photo.id == inPhoto.id) photo.copy(expanded = !inPhoto.expanded) else photo
            }.toMutableList()
        }
    }

    fun setText(query: String) {
        _searchText.update { query }
    }

    fun setSelectedTags(tags: MutableList<String>) {
        _selectedTags.update { _selectedTags.value.plus(tags).toMutableList() }
    }

    // endregion

}