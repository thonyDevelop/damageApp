package com.co.ao.damage_app

import android.R.attr.bitmap
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.co.ao.damage_app.adapter.DamageCaptureAdapter
import com.co.ao.damage_app.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import java.io.ByteArrayOutputStream
import android.location.Location
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.co.ao.damage_app.api.DamageDto
import com.co.ao.damage_app.api.DamageResponse
import com.co.ao.damage_app.api.LocationDto
import com.co.ao.damage_app.api.RetrofitInstance.apiService
import com.co.ao.damage_app.api.UserDto
import com.co.ao.damage_app.api.UserResponse
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var btnCamera: Button
    private lateinit var btnSend: Button
    private var damageCaptureList = ArrayList<DamageCapture>()
    private lateinit var damageDto:DamageDto
    private lateinit var preferencias: SharedPreferences
    private val LOGIN = "LOGIN"
    private val EMAIL = "EMAIL"
    private val ID = "ID"
    private var locationDto: LocationDto = LocationDto()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val alertDialog: AlertDialog = initAlertDialog()
        preferencias = getSharedPreferences(LOGIN, Context.MODE_PRIVATE)
        btnCamera = findViewById(R.id.btn_camera)
        btnSend = findViewById(R.id.btn_send)

        var email = preferencias.getString(EMAIL, "").toString()

        Utils().showMessage(this, "Hola $email")

        btnCamera.setOnClickListener {
            if (damageCaptureList.size >= 10){
                Toast.makeText(this,"No se puede subir mas de 10 imagenes", Toast.LENGTH_SHORT).show()
            } else {
                dispatchTakePictureIntent()
            }
        }
        btnSend.setOnClickListener{
            if (damageCaptureList.size <= 0){
                Toast.makeText(this,"Debe tener por lo menos 1 imagen para reportar", Toast.LENGTH_SHORT).show()
            } else {
                getLastLocation()
                alertDialog.show()
            }
        }
        initRecyclerView()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflar el menú definido en XML
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.damage_list -> {

                return true
            }
            R.id.option_register_damage -> {
                // Acción al hacer clic en el elemento del menú
                return true
            }
            R.id.option_logout -> {
                val editor: SharedPreferences.Editor = preferencias.edit()
                editor.clear()
                editor.apply()
                val i = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(i)
                finish()
                return true
            }
            // Agrega más casos según sea necesario
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            damageCaptureList.add(DamageCapture(getImageBase64(imageBitmap)))
        }
    }

    fun getImageBase64(bitmap: Bitmap):String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return  Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun initRecyclerView(){
        val manager = GridLayoutManager(this,2)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerImages)
        recyclerView.layoutManager = manager
        recyclerView.adapter = DamageCaptureAdapter(damageCaptureList)
    }

    fun initAlertDialog(): AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.dialog_layout, null)
        val dialogEditText = dialogView.findViewById<EditText>(R.id.dialogEditText)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setTitle("Observación")
        builder.setPositiveButton("Finalizar") { _, _ ->
            val observation = dialogEditText.text.toString()
            // Haz algo con la entrada del usuario, por ejemplo, mostrarla en un Toast
            damageDto = DamageDto(
                preferencias.getInt(ID, 0),
                observation,
                locationDto.latitude,
                locationDto.longitude,
                damageCaptureList
            )
            damageRegister(damageDto)
            Toast.makeText(this, "Enviando... ", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("Cancelar") { _, _ ->
            // Acción a realizar al presionar "Cancelar"
        }
        val alertDialog = builder.create()
        return alertDialog
    }



    fun getLastLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Aquí tienes la ubicación actual
                val latitude = location.latitude
                val longitude = location.longitude

                locationDto = LocationDto (latitude, longitude)
                Utils().showMessage(this@MainActivity, " location: $latitude, $longitude")
            }
        }
    }

    private fun damageRegister(damageDto: DamageDto){
        try {
            val call: Call<DamageResponse> = apiService.sendDamage(damageDto)

            call!!.enqueue(object : Callback<DamageResponse> {
                override fun onResponse(call: Call<DamageResponse>, response: Response<DamageResponse>) {


                    if (response.code() == 200) {
                        damageCaptureList.clear()
                        val recyclerView = findViewById<RecyclerView>(R.id.recyclerImages)
                        recyclerView.adapter = DamageCaptureAdapter(damageCaptureList)
                        val damageResponse = response.body()

                        Utils().showMessage(this@MainActivity,"Daño registrado")

                        Log.d("RESPONSE-RETRO", "Damage: $damageResponse ${response.code()}")
                    }
                    if (response.code() == 204){
                        Utils().showMessage(this@MainActivity,"Sin acceso")
                        Log.d("RESPONSE-RETRO", "DamageResponse: no se pudo registrar el o los daños ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<DamageResponse>, t: Throwable) {
                    Utils().showMessage(this@MainActivity,"Error a realizar el registro del daño, por favor intentelo mas tarde ")
                    Log.d("RESPONSE-RETRO", "DamageResponse: "+ t.message)
                }
            })

        } catch (e: Exception) {
            Log.d("RESPONSE-RETRO", "error: $e")
        }
    }
}



