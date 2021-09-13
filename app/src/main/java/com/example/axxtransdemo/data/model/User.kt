package com.example.axxtransdemo.data.model

data class User(
    val id: String,
    val fields: Fields
)

data class Fields(
    var FirstName: String,
    var LastName: String,
    var Token: String,
    var AccessPointRight: List<String>
)

data class AccessPointRight(
    var ids: List<String>
)