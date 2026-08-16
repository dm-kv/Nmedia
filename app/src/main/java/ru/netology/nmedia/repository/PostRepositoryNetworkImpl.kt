package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.MediaUpload
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.AppError


class PostRepositoryNetworkImpl(
    private val dao: PostDao,
    private val apiService: PostsApiService,
    ) : PostRepository {
    override val data = dao.getVisible()
        .map { it.map(PostEntity::toDto) }
        .flowOn(Dispatchers.IO)

    override suspend fun get() {
        try {
            val response = apiService.get()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.map { PostEntity.fromDto(it).copy(isVisible = true) })
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            try {
                val response = apiService.getNewer(getMaxId())
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }

                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(body.map { PostEntity.fromDto(it).copy(isVisible = false) })

                emit(dao.countInvisible())
            } catch (e: Exception) {
                e.printStackTrace()
                emit(-1)
            }
        }
    }
        .flowOn(Dispatchers.Default)


    override suspend fun acceptNewPosts() {
        dao.revealAll()
    }

    override suspend fun getMaxId(): Long = dao.getMaxId() ?: 0L


    override suspend fun save(post: Post) {
        try {
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun likeById(id: Long) {
        val current = dao.getById(id) ?: throw IllegalStateException("Post not found locally")
        val isLiked = current.likedByMe
        val updated = current.copy(
            likedByMe = !isLiked,
            likes = if (isLiked) (current.likes ?: 0) - 1 else (current.likes ?: 0) + 1
        )

        dao.insert(updated)

        try {
            val response = if (isLiked) {
                apiService.dislikeById(id)
            } else {
                apiService.likeById(id)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            dao.insert(current)
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(current)
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        val current = dao.getById(id)
            ?: throw IllegalStateException("Post not found locally")

        val updated = current.copy(

            shares = (current.shares ?: 0) + 1
        )
        dao.insert(updated)

        try {
            val response = apiService.getById(id)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            dao.insert(current)
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(current)
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            // TODO: add support for other types
            val postWithAttachment = post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: java.io.IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: java.io.IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        val removedPost = dao.getById(id)
            ?: throw IllegalStateException("Post not found locally")
        dao.removeById(id)
        try {
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            dao.insert(removedPost)
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(removedPost)
            throw UnknownError
        }
    }
}
