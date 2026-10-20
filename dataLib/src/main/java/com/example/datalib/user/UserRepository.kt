package com.example.datalib.user

import com.example.datalib.db.UserDao
import com.example.datalib.db.UserRoomEntity
import com.example.datalib.resultHandling.AppError
import com.example.datalib.resultHandling.Resource
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) : IUserRepository {

    override suspend fun login(username: String, password: String): Resource<UserEntity> {
        return try {
            val user = userDao.findByCredentials(username, password)
            if (user != null) {
                Resource.Success(UserEntity(id = user.id, username = user.username))
            } else {
                Resource.Failure(AppError.InvalidCredentials)
            }
        } catch (e: Exception) {
            Resource.Failure(AppError.DatabaseError)
        }
    }

    override suspend fun saveUser(email: String, password: String): Resource<Int> {
        return try {
            val existing = userDao.findByUsername(email)
            if (existing != null) {
                return Resource.Failure(AppError.UserAlreadyExists)
            }
            val newId = userDao.insert(UserRoomEntity(username = email, password = password))
            Resource.Success(newId.toInt())
        } catch (e: Exception) {
            Resource.Failure(AppError.DatabaseError)
        }
    }
}

