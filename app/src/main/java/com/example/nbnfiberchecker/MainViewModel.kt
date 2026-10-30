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

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

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

    var themeMode by mutableStateOf(loadThemeMode())
        private set

    val eligibleCount: Int
        get() = results.count { detail ->
            detail.techChangeStatus?.contains("ELIGIBLE", ignoreCase = true) == true ||
            detail.techFlip?.contains("Eligible", ignoreCase = true) == true ||
            detail.techType?.equals("FTTP", ignoreCase = true) == true
        }

    init {
        val savedIds = sharedPrefs.getStringSet("location_ids", emptySet()) ?: emptySet()
        _locationIds.addAll(savedIds)
        refresh()
    }

    private fun loadThemeMode(): ThemeMode {
        val modeStr = sharedPrefs.getString("app_theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        sharedPrefs.edit().putString("app_theme_mode", mode.name).apply()
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
