package com.co.ao.damage_app.utils

import android.content.Context
import android.widget.Toast

class Utils {
    fun showMessage(context: Context, message: String){
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show();
    }
}