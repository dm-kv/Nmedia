package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import kotlin.collections.emptyList

class PostPagingSource (
    private val dao: PostDao,
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null


    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        return try {
            val key = params.key ?: 0L
            val loadSize = params.loadSize

            val result = when (params) {
                is LoadParams.Refresh -> dao.getLatest(loadSize)
                is LoadParams.Append -> dao.getBefore(key, loadSize)
                is LoadParams.Prepend -> emptyList()
            }

            if (result.isEmpty()) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }

            val nextKey = if (result.size == loadSize) result.last().id else null

            LoadResult.Page(
                data = result.map { it.toDto() },
                prevKey = params.key,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}