package com.github.essmehdi.schoolmate.alerts.api
import com.github.essmehdi.schoolmate.alerts.api.dto.AlertDto
import com.github.essmehdi.schoolmate.alerts.api.dto.EditAlertDto
import com.github.essmehdi.schoolmate.alerts.models.Alert
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import retrofit2.Call
import retrofit2.http.*

interface AlertService {

    @GET("alerts")
    fun getAllUserAlerts(
        @Query("page") page: Long = 0
    ): Call<PaginatedResponse<Alert>>


    @POST("alerts")
    @Headers("Content-Type: application/json")
    fun addUserAlert(@Body createAlertDto: AlertDto): Call<Alert>

    @GET("alerts/{id}")
    fun getAlertById(@Path("id") id: Long): Call<Alert>

    @PATCH("alerts/{id}")
    @Headers("Content-Type: application/json")
    fun editUserAlert(@Path("id") id: Long, @Body editAlertDto: EditAlertDto): Call<Alert>

    @PATCH("alerts/{id}/cancel")
    fun cancelUserAlert(@Path("id") id: Long): Call<Alert>

    @PATCH("alerts/{id}/confirm")
    fun confirmUserAlert(@Path("id") id: Long): Call<Alert>

    @DELETE("alerts/{id}")
    fun deleteUserAlert(@Path("id") id: Long): Call<MessageResponse>

}