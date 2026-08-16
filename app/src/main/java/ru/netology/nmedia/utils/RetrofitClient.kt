package ru.netology.nmedia.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nmedia.api.AuthApi

object RetrofitClient {
    private const val AUTH_BASE_URL = "http://10.0.2.2:9999/"

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}