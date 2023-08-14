package com.co.ao.damage_app.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
    @POST("/api/User")
    fun getUser(@Body userDto: UserDto): Call<UserResponse>

    @POST("api/Damage")
    fun sendDamage(@Body damageDto: DamageDto): Call<DamageResponse>
}