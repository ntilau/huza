package com.huza.bridge

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object NtfySender {

    private const val TAG = "NtfySender"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun send(
        server: String,
        topic: String,
        title: String,
        text: String,
        app: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = if (text.isNotBlank()) text else title

            val request = Request.Builder()
                .url("$server/$topic")
                .post(body.toRequestBody("text/plain; charset=utf-8".toMediaType()))
                .header("Title", "$app — $title")
                .header("Priority", "default")
                .header("Tags", "incoming_envelope")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Sent to ntfy: $app — $title")
                Result.success(Unit)
            } else {
                Log.w(TAG, "ntfy returned ${response.code}")
                Result.failure(Exception("Server returned ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "ntfy send failed", e)
            Result.failure(e)
        }
    }
}
