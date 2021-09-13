package com.example.axxtransdemo.data.api

import com.example.axxtransdemo.data.model.AccessPointRecord
import com.example.axxtransdemo.data.model.Records
import com.example.axxtransdemo.data.model.ResponseObject
import com.example.axxtransdemo.data.model.User
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

//    @GET("appBZSQAVVCxLezwH/User")
//    suspend fun getUser(@Query("fields%5B%5D") first_name: String, @Query("fields%5B%5D") last_name: String,
//    @Query("filterByFormula") tokenString: String): Response<Records>

    @GET("appBZSQAVVCxLezwH/User")
    suspend fun getUser(@Query("filterByFormula") tokenString: String): Response<Records>

    @POST("appBZSQAVVCxLezwH/User")
    suspend fun addUser(): Response<User>

    @GET("appBZSQAVVCxLezwH/User")
    suspend fun getUserByName(@Query("fields%5B%5D") first_name: String, @Query("fields%5B%5D") last_name: String,
                        @Query("filterByFormula") searchStr: String): Response<Records>

    @PATCH("appBZSQAVVCxLezwH/User/{user_id}")
    suspend fun updateUserEntreForToken(@Path("user_id") user_id: String, @Body body: JsonObject): Response<User>

    @GET("appBZSQAVVCxLezwH/AccessPoint")
    suspend fun getAccessPoints(): Response<AccessPointRecord>
}