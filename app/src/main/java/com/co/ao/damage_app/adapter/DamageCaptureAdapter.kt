package com.co.ao.damage_app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.co.ao.damage_app.DamageCapture
import com.co.ao.damage_app.R

class DamageCaptureAdapter(private val damageCaptureList:List<DamageCapture>): RecyclerView.Adapter<DamageCaptureViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DamageCaptureViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DamageCaptureViewHolder(layoutInflater.inflate(R.layout.item_damage_capture, parent, false))
    }

    override fun getItemCount(): Int = damageCaptureList.size


    override fun onBindViewHolder(holder: DamageCaptureViewHolder, position: Int) {
        val item = damageCaptureList[position]
        holder.render(item)
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_slide_in_from_bottom)
        holder.itemView.startAnimation(animation)
    }


}