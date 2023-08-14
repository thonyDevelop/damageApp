package com.co.ao.damage_app.api

import com.google.gson.annotations.SerializedName

data class UserResponse (
    @SerializedName("id")
    var id: Int,
    @SerializedName("email")
    var email: String,
    @SerializedName("status")
    var status: String,
    @SerializedName("insert_date")
    var insertDate: String
)

data class UserDto(
    var email: String
)