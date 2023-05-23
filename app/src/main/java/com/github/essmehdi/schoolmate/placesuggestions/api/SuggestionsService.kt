package com.github.essmehdi.schoolmate.placesuggestions.api

import com.github.essmehdi.schoolmate.placesuggestions.api.dto.CreateSuggestionDto
import com.github.essmehdi.schoolmate.placesuggestions.api.dto.EditSuggestionDto
import com.github.essmehdi.schoolmate.placesuggestions.models.PlaceSuggestions
import com.github.essmehdi.schoolmate.shared.api.dto.MessageResponse
import com.github.essmehdi.schoolmate.shared.api.dto.PaginatedResponse
import retrofit2.Call
import retrofit2.http.*

interface SuggestionsService {
    @GET("placesuggestions")
    fun getAllSuggestions(
        @Query("page") page: Long = 0,
        @Query("sort") sort: String = "date,desc",
    ): Call<PaginatedResponse<PlaceSuggestions>>

    @POST("placesuggestions")
    fun suggestPlace(
        @Body createSuggestionDto: CreateSuggestionDto
    ): Call<PlaceSuggestions>

    @GET("placesuggestions/user/me")
    fun getCurrentUserSuggestions(
        @Query("page") page: Long = 0,
        @Query("sort") sort: String = "date,desc",
    ): Call<PaginatedResponse<PlaceSuggestions>>

    @GET("placesuggestions/user/{id}")
    fun getUserSuggestions(
        @Query("page") page: Long = 0,
        @Query("sort") sort: String = "date,desc",
        @Path("id") id: Long
    ): Call<PaginatedResponse<PlaceSuggestions>>

    @GET("placesuggestions/{id}")
    fun getSuggestion(
        @Path("id") id: Long,
    ): Call<PlaceSuggestions>

    @PATCH("placesuggestions/{id}")
    fun editSuggestion(
        @Body editSuggestionDto: EditSuggestionDto,
        @Path("id") id: Long,
    ): Call<PlaceSuggestions>

    @DELETE("placesuggestions/{id}")
    fun deleteSuggestion(
        @Path("id") id: Long,
    ): Call<MessageResponse>
}