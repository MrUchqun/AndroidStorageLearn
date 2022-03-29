package com.example.androidstoragelearn

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotoAdapter(var context: Context, var list: List<Any>) :
    RecyclerView.Adapter<PhotoAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.setData(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class VH(inflate: View) : RecyclerView.ViewHolder(inflate) {
        private val ivPhoto: ImageView = inflate.findViewById(R.id.iv_photo)
        fun setData(item: Any) {
            Glide.with(context).load(item).into(ivPhoto)
        }
    }
}