package com.github.essmehdi.schoolmate.shared.api.interceptors

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.github.essmehdi.schoolmate.shared.utils.PrefsManager
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class SessionInterceptor(private val sharedPreferences: SharedPreferences): Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())

    if (response.headers("Set-Cookie").isNotEmpty()) {
      for (cookie in response.headers("Set-Cookie")) {
        Log.d("Cookie: ", cookie);
        val strippedCookie = cookie.split(";")[0]
        if (strippedCookie.contains("JSESSIONID")) {
          sharedPreferences.edit {
            putString(PrefsManager.USER_COOKIE, strippedCookie)
            apply()
          }
        }
      }
    }
    return response
  }
}