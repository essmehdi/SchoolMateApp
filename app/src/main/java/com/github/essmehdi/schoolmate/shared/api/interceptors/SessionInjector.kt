package com.github.essmehdi.schoolmate.shared.api.interceptors

import android.content.SharedPreferences
import com.github.essmehdi.schoolmate.shared.utils.PrefsManager
import okhttp3.Interceptor
import okhttp3.Response

class SessionInjector(private val sharedPreferences: SharedPreferences): Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val builder = chain.request().newBuilder()
    sharedPreferences.getString(PrefsManager.USER_COOKIE, null)?.let {
      builder.header("Cookie", it)
    }
    return chain.proceed(builder.build())
  }
}