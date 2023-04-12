package com.github.essmehdi.schoolmate.shared.api.interceptors

import android.content.Context
import android.content.Intent
import android.os.Handler
import com.github.essmehdi.schoolmate.auth.api.AuthService
import com.github.essmehdi.schoolmate.auth.ui.LoginActivity
import com.github.essmehdi.schoolmate.shared.api.Api
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CookieAuthenticator(val context: Context): Authenticator {
  override fun authenticate(route: Route?, response: Response): Request? {
    return null
  }

  suspend fun checkAuth() {
    val retrofit = Retrofit.Builder()
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl(Api.BASE_URL)
      .build()

    val service = retrofit.create(AuthService::class.java)
    val response = service.me().execute()
    if (!response.isSuccessful) {
      val mainHandler = Handler(context.mainLooper)
      mainHandler.post {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
      }
    }
  }

}