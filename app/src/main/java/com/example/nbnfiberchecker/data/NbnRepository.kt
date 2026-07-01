package com.example.nbnfiberchecker.data

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NbnRepository {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchNbnDetails(locationId: String): Result<AddressDetail> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://places.nbnco.net.au/places/v2/details/$locationId")
            .header("Referer", "https://www.nbnco.com.au/")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP error ${response.code}"))

                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val nbnResponse = json.decodeFromString<NbnResponse>(body)
                val detail = nbnResponse.addressDetail ?: return@withContext Result.failure(Exception("No address detail"))
                Result.success(detail)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
