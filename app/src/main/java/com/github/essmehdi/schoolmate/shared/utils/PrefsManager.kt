package com.github.essmehdi.schoolmate.shared.utils

import android.content.Context
import android.content.SharedPreferences
import com.github.essmehdi.schoolmate.R

object PrefsManager {

  const val USER_COOKIE = "user_cookie"

  fun saveString(context: Context, key: String, value: String) {
    context
      .getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
      .edit().apply {
        putString(key, value)
        apply()
      }
  }

  fun getString(context: Context, key: String, fallbackValue: String? = null): String? {
    return context
      .getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
      .getString(key, fallbackValue)
  }

  fun clearString(context: Context, key: String) {
    context
      .getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
      .edit()
      .apply {
        clearString(context, key)
        apply()
    }
  }

  fun clearData(context: Context){
    context
      .getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
      .edit().apply {
        clear()
        apply()
      }
  }
}