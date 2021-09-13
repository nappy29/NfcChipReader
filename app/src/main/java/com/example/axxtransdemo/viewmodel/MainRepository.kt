package com.example.axxtransdemo.viewmodel

import com.example.axxtransdemo.data.api.ApiService
import com.google.gson.JsonObject

class MainRepository constructor(private val apiService: ApiService) {
    private val first_name = "FirstName"
    private val last_name = "LastName"

    suspend fun getUserByToken(tokenStr: String) = apiService.getUser(tokenStr)

    suspend fun getUserByName(searchStr: String) = apiService.getUserByName(first_name, last_name, searchStr)

    suspend fun updateUserEntreForToken(user_id: String, body: JsonObject) = apiService.updateUserEntreForToken(user_id, body)

    suspend fun getAccessPoints() = apiService.getAccessPoints()
}