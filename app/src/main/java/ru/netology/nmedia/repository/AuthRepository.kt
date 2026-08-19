package ru.netology.nmedia.repository

import ru.netology.nmedia.api.AuthResponse
import ru.netology.nmedia.api.AuthApi
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi
) {
    suspend fun authenticate(login: String, pass: String): AuthResponse =
        api.authenticate(login, pass)

    suspend fun registerUser(login: String, pass: String, name: String): AuthResponse =
        api.registerUser(login, pass, name)
}