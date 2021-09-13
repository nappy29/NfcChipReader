package com.example.axxtransdemo.data.model

data class AccessPointRecord(
    val records: List<AccessPoint>
)
data class AccessPoint (
    val id: String,
    val fields: AccessPointFields
    )


data class AccessPointFields(
    var Name: String,
    var AccessPointRight: List<String>
    )