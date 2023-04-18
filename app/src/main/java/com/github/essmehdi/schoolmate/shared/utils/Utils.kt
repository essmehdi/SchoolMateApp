package com.github.essmehdi.schoolmate.shared.utils

import android.annotation.SuppressLint
import android.content.Context
import com.github.essmehdi.schoolmate.R
import java.text.DateFormat
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*

object Utils {

  /**
   * Author: Mehdi ESSALEHI
   * https://github.com/essmehdi/DuckDuckGoSearchApp/blob/develop/app/src/main/java/io/duckduckgosearch/app/HistoryAdapter.java
   */
  @SuppressLint("SimpleDateFormat")
  fun calculatePastTime(date: Date, context: Context): String? {
    val format: DateFormat = SimpleDateFormat("EEE. MMM d, yyyy")
    val formatDay: DateFormat = SimpleDateFormat("EEEE")
    val formatNoYear: DateFormat = SimpleDateFormat("EEE. MMM d")
    val currentCalendar = Calendar.getInstance()
    currentCalendar.time = Calendar.getInstance().time
    val day = currentCalendar[Calendar.DAY_OF_YEAR]
    val year = currentCalendar[Calendar.YEAR]
    val calendarToCompare = Calendar.getInstance()
    calendarToCompare.time = date
    val dayToCompare = calendarToCompare[Calendar.DAY_OF_YEAR]
    val yearToCompare = calendarToCompare[Calendar.YEAR]
    val diff = currentCalendar.timeInMillis - calendarToCompare.timeInMillis
    val minutes: Long
    val hours: Long
    val days: Long
    val seconds: Long = diff / 1000
    return if (seconds >= 60) {
      minutes = seconds / 60
      if (minutes >= 60) {
        hours = minutes / 60
        if (hours >= 24) {
          days = hours / 24
          if (days < 2 && day - dayToCompare < 2 && day - dayToCompare > 0) {
            context.resources.getString(R.string.time_calculation_yesterday)
          } else if (days < 7 && day - dayToCompare < 7 && day - dayToCompare > 0) {
            formatDay.format(date)
          } else {
            if (yearToCompare == year) {
              formatNoYear.format(date)
            } else {
              format.format(date)
            }
          }
        } else {
          context.resources.getQuantityString(R.plurals.time_calculation_hours, hours.toInt(), hours.toInt())
        }
      } else {
        context.resources.getQuantityString(R.plurals.time_calculation_minutes, minutes.toInt(), minutes.toInt())
      }
    } else {
      context.resources.getString(R.string.time_calculation_few_moments)
    }
  }

  fun slugify(word: String, replacement: String = "-") = Normalizer
    .normalize(word, Normalizer.Form.NFD)
    .replace("[^\\p{ASCII}]".toRegex(), "")
    .replace("[^a-zA-Z0-9\\s]+".toRegex(), "").trim()
    .replace("\\s+".toRegex(), replacement)
    .toLowerCase()
}