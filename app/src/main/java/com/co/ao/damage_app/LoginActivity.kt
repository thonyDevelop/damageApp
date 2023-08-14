package com.co.ao.damage_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import com.co.ao.damage_app.api.RetrofitInstance
import com.co.ao.damage_app.api.UserDto
import com.co.ao.damage_app.api.UserResponse
import com.co.ao.damage_app.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.Manifest


class LoginActivity : AppCompatActivity() {
    private val apiService = RetrofitInstance.apiService
    private lateinit var etEmail: EditText
    private lateinit var btnLogin: Button
    private lateinit var preferencias: SharedPreferences
    private val EMAIL = "EMAIL"
    private val ID = "ID"
    private val LOGIN = "LOGIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        etEmail = findViewById(R.id.et_Email)
        btnLogin = findViewById(R.id.btn_Login)
        preferencias = getSharedPreferences(LOGIN, MODE_PRIVATE)
        val getUsername = preferencias.getString(EMAIL, "")
        val getId = preferencias.getInt(ID, 0)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE)
            return
        }

        if (getUsername != "" && getId != 0){
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }


        btnLogin.setOnClickListener{
            var email = etEmail.text.toString().trim()
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            if (email.isEmpty() || !email.matches(emailPattern.toRegex())){
                Utils().showMessage(this,"Por favor digitar un correo electrónico válido")
                return@setOnClickListener
            }
            val userDto = UserDto(email)
            sendEmail(userDto)
        }


    }

    private fun sendEmail(userDto: UserDto){
        try {
            val call: Call<UserResponse> = apiService.getUser(userDto)

            call!!.enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {


                    if (response.code() == 200) {
                        etEmail.setText("")
                        val user = response.body()
                        preferencias = getSharedPreferences(LOGIN, Context.MODE_PRIVATE)
                        val editor: SharedPreferences.Editor = preferencias.edit()
                        editor.putString(EMAIL, user!!.email)
                        editor.putInt(ID, user!!.id)
                        editor.apply()
                        Utils().showMessage(this@LoginActivity,"Bienvenido ${user!!.email}")
                        val i = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(i)
                        finish()
                        Log.d("RESPONSE-RETRO", "sendEmail1: $user ${response.code()}")
                    }
                    if (response.code() == 204){
                        Utils().showMessage(this@LoginActivity,"Sin acceso")
                        Log.d("RESPONSE-RETRO", "sendEmail1: no existe usuario ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Utils().showMessage(this@LoginActivity,"Error a realizar el login ${t.message}")
                    Log.d("RESPONSE-RETRO", "sendEmail3: "+ t.message)
                }
            })

        } catch (e: Exception) {
            Log.d("RESPONSE-RETRO", "error: $e")
        }
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }



}