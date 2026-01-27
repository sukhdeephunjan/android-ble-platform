package com.example.datalib.user

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.datalib.resultHandling.AppError
import com.example.datalib.resultHandling.Resource
import javax.inject.Inject

class UserRepositoryImpl @Inject  constructor(private val userDao: UserDao) : IUserRepository {

    override suspend fun login(username: String, password: String): Resource<UserEntity> {
        return try {
            val entity = userDao.getUserByCredentials(username, password)
            // Return the appropriate result based on whether the user is found
            if (entity != null) {
                Resource.Success(entity)  // Successfully logged in
            } else {
              // Invalid credentials
                Resource.Failure(AppError.InvalidCredentials)
            }
        } catch (e: Exception) {
            // Log error and return Error state
            Log.e("UserRepository", "Error during login: ${e.message}")
            Resource.Failure(AppError.DatabaseError)  // Handle any other errors (e.g., database issues, network issues)
        }
    }

    override suspend fun getUserById(id: Int): Resource<UserEntity>? {
        return try {
            val user = userDao.getUserById(id)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Failure(AppError.UserNotFound)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "getUserById error", e)
            Resource.Failure(AppError.DatabaseError)
        }
    }

    override suspend fun saveUser(username: String, password: String): Resource<Int> {
        return try {
            val existingUser = userDao.getUserByUsername(username)

            if (existingUser != null) {
                return Resource.Failure(AppError.UserAlreadyExists)
            }
            val userId = userDao.insertUser(
                UserEntity(
                    username = username,
                    password = password
                )
            )
            Resource.Success(userId.toInt())
        } catch (e: SQLiteConstraintException) {
            // Safety net: unique constraint violation (race condition)
            Resource.Failure(AppError.UserAlreadyExists)

        } catch (e: Exception) {
            Resource.Failure(AppError.Unknown("Something wrong happened"))
        }
    }

    // Extension function to convert Entity -> Domain
    private fun UserEntity.toDomain() = UserEntity(
        id = this.id,
        username = this.username,
        password = this.password
    )
}