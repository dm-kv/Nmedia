package ru.netology.nmedia.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY id DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<PostEntity>

    @Query("SELECT * FROM posts WHERE id < :key ORDER BY id DESC LIMIT :limit")
    suspend fun getBefore(key: Long, limit: Int): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE posts SET likes = likes + 1, likedByMe = 1 WHERE id = :id")
    suspend fun likeById(id: Long)

    @Query("UPDATE posts SET likes = likes - 1, likedByMe = 0 WHERE id = :id")
    suspend fun unlikeById(id: Long)
}