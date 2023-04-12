package com.github.essmehdi.schoolmate.shared.api.interceptors

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class SessionInterceptor(private val sharedPreferences: SharedPreferences): Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())

    chain.request().body()?.let {
      try {
        val requestBuffer = Buffer()
        it.writeTo(requestBuffer)
        Log.d("Request body", requestBuffer.readUtf8())
        Log.d("Request method", chain.request().method())
        Log.d("Request URL", chain.request().url().toString())
        Log.d("Request", chain.request().headers().names().toString())
      } catch (e: Exception) {
        Log.e("Request body log", "Failed to get request body")
      }
    }

    Log.d("Response body", response.peekBody(Long.MAX_VALUE).string())

    if (response.headers("Set-Cookie").isNotEmpty()) {
      for (cookie in response.headers("Set-Cookie")) {
        Log.d("Cookie: ", cookie);
        val strippedCookie = cookie.split(";")[0]
        if (strippedCookie.contains("JSESSIONID")) {
          sharedPreferences.edit {
            putString("user_cookie", strippedCookie)
            apply()
          }
        }
      }
    }
    return response
  }
}