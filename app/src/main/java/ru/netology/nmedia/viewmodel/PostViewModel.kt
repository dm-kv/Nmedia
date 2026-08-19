package ru.netology.nmedia.viewmodel


import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.utils.SingleLiveEvent
import ru.netology.nmedia.model.FeedModelState
import kotlinx.coroutines.flow.catch
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.AuthRepository
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject
import kotlin.Boolean
import kotlin.Long


private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    published = 0,
    content = "",
    likes = 0,
    shares = 0,
    likedByMe = false,
    video = null,
    isVisible = false,
    ownedByMe = false,
)

private val noPhoto = PhotoModel()

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    authRepository: AuthRepository,
    appAuth: AppAuth,
): ViewModel() {

    private val authViewModel: AuthViewModel = AuthViewModel(appAuth)
    val authenticated: Boolean
        get() = authViewModel.authenticated


    val data: LiveData<FeedModel> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.data
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState> get() = _dataState

    private val _newerCount = MutableLiveData<Int>(0)
    val newerCount: LiveData<Int> get() = _newerCount
    val hasNewPosts: LiveData<Boolean> = _newerCount.map { it > 0 }

    private val _event = MutableLiveData<PostUiEvent>()
    val event: LiveData<PostUiEvent>
        get() = _event

    @Volatile
    private var justAcceptedNewPosts = false

    fun shouldConsumeAndResetJustAccepted(): Boolean {
        val was = justAcceptedNewPosts
        justAcceptedNewPosts = false
        return was
    }

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
        viewModelScope.launch {
            repository.getNewerCount()
                .catch { e ->
                    Log.e("Feed", "getNewerCount error", e)
                    _newerCount.value = -1
                }
                .collect { count ->
                    if (count >= 0) {
                        _newerCount.value = count
                    }
                }
        }
    }

    fun onNewPostsClicked() = viewModelScope.launch {
        justAcceptedNewPosts = true
        try {
            repository.acceptNewPosts()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            justAcceptedNewPosts = false
        }
    }


    fun loadPosts() = viewModelScope.launch {
        _dataState.value = FeedModelState(loading = true)
        try {
            repository.get()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        _dataState.value = FeedModelState(refreshing = true)
        try {
            repository.get()
            _newerCount.value = 0
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit> get() = _postCreated

    fun save() {
        if (!authViewModel.authenticated) {
            _event.value = PostUiEvent.RequestAuth
            return
        }

        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> repository.save(it)
                        else -> _photo.value?.uri?.let { uri ->
                            repository.saveWithAttachment(it, MediaUpload(uri.toFile()))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }


    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    fun likeById(id: Long) {
        if (!authViewModel.authenticated) {
            _event.value = PostUiEvent.RequestAuth
            return
        }

        viewModelScope.launch {
            _dataState.value = FeedModelState(loading = true)
            try {
                repository.likeById(id)
                loadPosts()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            } finally {
                _dataState.value = FeedModelState()
            }
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        _dataState.value = FeedModelState(loading = true)
        try {
            repository.removeById(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

}

sealed class PostUiEvent {
    object RequestAuth : PostUiEvent()
}

