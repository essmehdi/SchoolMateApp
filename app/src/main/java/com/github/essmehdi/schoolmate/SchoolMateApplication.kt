package com.github.essmehdi.schoolmate

import android.app.Application
import android.graphics.Bitmap
import com.github.essmehdi.schoolmate.shared.api.Api

class SchoolMateApplication: Application() {
  override fun onCreate() {
    super.onCreate()

    // Initialize the API
    Api.setup(applicationContext)
  }
}