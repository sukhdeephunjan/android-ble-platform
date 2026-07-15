package com.example.datalib.user

import com.example.datalib.resultHandling.Resource

interface IUserRepository {
    suspend fun login(username: String, password: String): Resource<UserEntity>
    suspend fun saveUser(email: String, password: String): Resource<Int>
}

