package com.github.essmehdi.schoolmate

import android.app.Application
import com.github.essmehdi.schoolmate.shared.api.Api

class SchoolMateApplication: Application() {
  override fun onCreate() {
    super.onCreate()
    Api.setup(applicationContext)
  }
}