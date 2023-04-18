package com.github.essmehdi.schoolmate.shared.api.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class RequestLogger: Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())

    chain.request().body()?.let {
      try {
        val requestBuffer = Buffer()
        it.writeTo(requestBuffer)
        Log.d("Request body", requestBuffer.readUtf8())
      } catch (e: Exception) {
        Log.e("Request body log", "Failed to get request body")
      }
    }

    Log.d("Request method", chain.request().method())
    Log.d("Request URL", chain.request().url().toString())
    Log.d("Request headers", chain.request().headers().names().toString())
    Log.d("Response body", response.peekBody(Long.MAX_VALUE).string())

    return response
  }
}