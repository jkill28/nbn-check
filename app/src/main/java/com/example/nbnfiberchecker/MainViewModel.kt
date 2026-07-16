package com.example.nbnfiberchecker

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nbnfiberchecker.data.AddressDetail
import com.example.nbnfiberchecker.data.AddressSuggestion
import com.example.nbnfiberchecker.data.NbnRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NbnRepository()
    private val sharedPrefs = application.getSharedPreferences("nbn_checker_prefs", Context.MODE_PRIVATE)

    var results by mutableStateOf<List<AddressDetail>>(emptyList())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    private var _locationIds = mutableStateListOf<String>()
    val locationIds: List<String> get() = _locationIds

    var searchResults by mutableStateOf<List<AddressSuggestion>>(emptyList())
        private set

    init {
        val savedIds = sharedPrefs.getStringSet("location_ids", emptySet()) ?: emptySet()
        _locationIds.addAll(savedIds)
        refresh()
    }

    private fun saveIds() {
        sharedPrefs.edit().putStringSet("location_ids", _locationIds.toSet()).apply()
    }

    fun refresh() {
        if (_locationIds.isEmpty()) {
            results = emptyList()
            return
        }
        viewModelScope.launch {
            isRefreshing = true
            val deferred = _locationIds.map { id ->
                async { repository.fetchNbnDetails(id) }
            }
            results = deferred.awaitAll().mapNotNull { it.getOrNull() }
            isRefreshing = false
        }
    }

    fun addLocation(id: String) {
        if (!_locationIds.contains(id)) {
            _locationIds.add(id)
            saveIds()
            refresh()
        }
    }

    fun removeLocation(id: String) {
        // We need the ID from the result or we store a map.
        // Let's find the ID by matching the result's address if needed, or just remove from the list.
        // But the UI shows cards from 'results'.
        // Let's assume we remove by index or something.
        // Or better, NbnDetail doesn't have the ID. Let's modify AddressDetail to include it if possible,
        // or just match by formattedAddress.
    }

    fun removeLocationById(id: String) {
        if (_locationIds.remove(id)) {
            saveIds()
            refresh()
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }
        viewModelScope.launch {
            repository.searchAddress(query).onSuccess {
                searchResults = it
            }
        }
    }
}
