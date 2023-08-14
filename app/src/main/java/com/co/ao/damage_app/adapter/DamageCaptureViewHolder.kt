package com.co.ao.damage_app.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.co.ao.damage_app.DamageCapture
import com.co.ao.damage_app.R

class DamageCaptureViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val photo: ImageView = view.findViewById(R.id.photo)
    fun render (damageCapture: DamageCapture){
        photo.setImageBitmap(decodeBase64ToBitmap(damageCapture.image))
    }
    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}