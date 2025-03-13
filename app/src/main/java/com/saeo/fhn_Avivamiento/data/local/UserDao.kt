package com.saeo.fhn_Avivamiento.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.Date

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhone(phoneNumber: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    @Query("SELECT * FROM users WHERE synced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    @Query("UPDATE users SET synced = 1 WHERE phoneNumber = :phoneNumber")
    suspend fun markUserAsSynced(phoneNumber: String)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET lastModified = :lastModified, synced = 0 WHERE phoneNumber = :phoneNumber")
    suspend fun markUserAsModified(phoneNumber: String, lastModified: Date)

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND password = :password LIMIT 1")
    suspend fun verifyCredentials(phoneNumber: String, password: String): UserEntity?
}