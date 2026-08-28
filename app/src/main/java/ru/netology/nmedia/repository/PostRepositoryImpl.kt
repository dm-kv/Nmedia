package ru.netology.nmedia.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.flowOf
import ru.netology.nmedia.entity.PostEntity

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
) : PostRepository {


    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 5, enablePlaceholders = false),
        pagingSourceFactory = { PostPagingSource(postDao) },
    ).flow

    override suspend fun getAll() {
        postDao.getLatest(Int.MAX_VALUE)
    }

    override fun getNewerCount(id: Long): Flow<Int> = flowOf(0)

    fun Post.toEntity(): PostEntity = PostEntity.fromDto(this)

    override suspend fun save(post: Post, upload: MediaUpload?) {
        postDao.insert(post.toEntity())
        data.collectLatest{}
    }

    override suspend fun removeById(id: Long) {
        postDao.removeById(id)
    }

    override suspend fun likeById(id: Long) {
        postDao.likeById(id)
    }

    override suspend fun upload(upload: MediaUpload): Media {
        throw UnsupportedOperationException("No media")
    }
}
