package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun get(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getById(id: Long): PostEntity?

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Int

    @Query("UPDATE PostEntity SET isVisible = 1")
    suspend fun revealAll()

    @Query("SELECT * FROM PostEntity WHERE isVisible = 1 ORDER BY published DESC")
    fun getVisible(): Flow<List<PostEntity>>

    @Query("SELECT MAX(id) FROM PostEntity")
    suspend fun getMaxId(): Long?

    @Query("SELECT COUNT(*) FROM PostEntity WHERE isVisible = 0")
    suspend fun countInvisible(): Int
}
