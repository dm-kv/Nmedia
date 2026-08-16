package ru.netology.nmedia.viewmodel

import ru.netology.nmedia.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.viewModelScope
import ru.netology.nmedia.auth.AppAuth
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.repository.PostRepository


class RegisterViewModel(
    private val repository: AuthRepository,
    appAuth: AppAuth,

    ) : ViewModel() {

    private val dependencyContainer = DependencyContainer.getInstance()

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(login: String, pass: String, name: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val response = repository.registerUser(login, pass, name)
                dependencyContainer.appAuth.setAuth(response.id, response.token)
                _uiState.value = RegisterUiState.Success
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "Ошибка регистрации")
            }
        }
    }
}


class RegisterViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}