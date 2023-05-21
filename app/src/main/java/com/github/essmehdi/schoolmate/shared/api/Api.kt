package com.github.essmehdi.schoolmate.shared.api

import android.content.Context
import android.content.SharedPreferences
import com.github.essmehdi.schoolmate.R
import com.github.essmehdi.schoolmate.alerts.api.AlertService
import com.github.essmehdi.schoolmate.auth.api.AuthService
import com.github.essmehdi.schoolmate.complaints.api.ComplaintService
import com.github.essmehdi.schoolmate.complaints.models.BuildingComplaint
import com.github.essmehdi.schoolmate.complaints.models.Complaint
import com.github.essmehdi.schoolmate.complaints.models.FacilitiesComplaint
import com.github.essmehdi.schoolmate.complaints.models.RoomComplaint
import com.github.essmehdi.schoolmate.documents.api.DocumentsService
import com.github.essmehdi.schoolmate.schoolnavigation.api.SchoolZonesService
import com.github.essmehdi.schoolmate.shared.api.interceptors.CookieAuthenticator
import com.github.essmehdi.schoolmate.shared.api.interceptors.RequestLogger
import com.github.essmehdi.schoolmate.shared.api.interceptors.SessionInjector
import com.github.essmehdi.schoolmate.shared.api.interceptors.SessionInterceptor
import com.github.essmehdi.schoolmate.shared.utils.RuntimeTypeAdapterFactory
import com.github.essmehdi.schoolmate.users.api.UsersService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Api {

  const val BASE_URL = "http://192.168.1.107:9080/schoolmate/api/"
  private lateinit var retrofit: Retrofit

  fun setup(context: Context) {
    val sharedPrefs =
      context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    val runtimeTypeAdapterFactory: RuntimeTypeAdapterFactory<Complaint> = RuntimeTypeAdapterFactory
      .of(Complaint::class.java, "dtype")
      .registerSubtype(BuildingComplaint::class.java, "BuildingComplaint")
      .registerSubtype(RoomComplaint::class.java, "RoomComplaint")
      .registerSubtype(FacilitiesComplaint::class.java, "FacilitiesComplaint")

    val gson = GsonBuilder()
      .registerTypeAdapterFactory(runtimeTypeAdapterFactory)
      .create()

    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(SessionInterceptor(sharedPrefs))
      .addInterceptor(SessionInjector(sharedPrefs))
      .addInterceptor(RequestLogger())
      .authenticator(CookieAuthenticator(context))
      .build()

    retrofit = Retrofit.Builder()
      .addConverterFactory(GsonConverterFactory.create(gson))
      .client(okHttpClient)
      .baseUrl(BASE_URL)
      .build()
  }

  val schoolZonesService: SchoolZonesService by lazy {
    retrofit.create(SchoolZonesService::class.java)
  }

  val authService: AuthService by lazy {
    retrofit.create(AuthService::class.java)
  }

  val alertService: AlertService by lazy {
    retrofit.create(AlertService::class.java)
  }

  val documentsService: DocumentsService by lazy {
    retrofit.create(DocumentsService::class.java)
  }

  val usersService: UsersService by lazy {
    retrofit.create(UsersService::class.java)
  }

  val complaintService: ComplaintService by lazy {
    retrofit.create(ComplaintService::class.java)
  }
}