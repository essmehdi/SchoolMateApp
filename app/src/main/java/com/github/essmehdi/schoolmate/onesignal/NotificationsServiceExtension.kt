package com.github.essmehdi.schoolmate.onesignal

import android.content.Context
import android.util.Log
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal.OSRemoteNotificationReceivedHandler

@Suppress("UNUSED")
class NotificationsServiceExtension: OSRemoteNotificationReceivedHandler {
  override fun remoteNotificationReceived(context: Context?, notificationReceivedEvent: OSNotificationReceivedEvent?) {
    val notification = notificationReceivedEvent?.notification
    notification?.additionalData?.let {
      it.keys().forEach { key ->
        Log.d("OneSignalNotification", key)
      }
    }
    if (notification?.additionalData?.has("alertId") == true) {
      val id = notification.additionalData!!.get("alertId")
      // TODO: Handle alertId
    } else if (notification?.additionalData?.has("complaintId") == true) {
      val id = notification.additionalData!!.get("complaintId")
      // TODO: Handle complaintId
    }
    notificationReceivedEvent?.complete(notification)
  }
}