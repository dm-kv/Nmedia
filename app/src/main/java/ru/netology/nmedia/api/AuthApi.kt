package ru.netology.nmedia.api

import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Field

interface AuthApi {
    @FormUrlEncoded
    @POST("api/users/authentication")
    suspend fun authenticate(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): AuthResponse

    @FormUrlEncoded
    @POST("api/users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): AuthResponse
}

data class AuthResponse(val id: Long, val token: String)