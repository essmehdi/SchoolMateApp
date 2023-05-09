package com.github.essmehdi.schoolmate

import android.app.Application
import android.graphics.Bitmap
import com.github.essmehdi.schoolmate.shared.api.Api
import com.onesignal.OneSignal

const val ONESIGNAL_APP_ID = "b2b8442c-96ee-49dc-9769-e4538d9ff8d8"
class SchoolMateApplication: Application() {
  override fun onCreate() {
    super.onCreate()

    OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

    // OneSignal Initialization
    OneSignal.initWithContext(this)
    OneSignal.setAppId(ONESIGNAL_APP_ID)

    OneSignal.promptForPushNotifications();

    // Initialize the API
    Api.setup(applicationContext)
  }
}