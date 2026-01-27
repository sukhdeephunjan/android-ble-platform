package com.example.datalib.user

import com.example.datalib.resultHandling.Resource

interface IUserRepository {
    // Returns null if credentials don't match
    suspend fun login(username: String, password: String): Resource<UserEntity>

    // Gets details by ID
    suspend fun getUserById(id: Int): Resource<UserEntity>?

    suspend fun saveUser(userName: String, password:String) : Resource<Int>
}