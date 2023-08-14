package com.co.ao.damage_app.api

import com.co.ao.damage_app.DamageCapture
import com.google.gson.annotations.SerializedName

data class DamageDto(
    val user_id: Int,
    val observation: String,
    val latitude: Double,
    val longitude: Double,
    val images: ArrayList<DamageCapture>
)

data class DamageResponse (
    @SerializedName("message")
    var message: String,
    @SerializedName("status")
    var status: String
)

