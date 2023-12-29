package com.example.madcamp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(val profileList: ArrayList<Profile>):
        RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    // 뷰가 만들어질때 호출되는 메서드. 뷰홀더를 생성해 리턴
    // recyclerview 만들어질때만 호출
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_item, parent, false)
        return ProfileViewHolder(view)
    }


    // 뷰와 내용 연결
    // recyclerview -> 재사용. 스크롤 내리거나 올릴때 호출
    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.rv_image.text = profileList[position].image
        holder.rv_name.text = profileList[position].name
        holder.rv_phone.text = profileList[position].phone

    }
    override fun getItemCount(): Int {
        Log.d("size", profileList.size.toString())
        return profileList.count()
    }


    // 해당 뷰와 연결
    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rv_image = itemView.findViewById<TextView>(R.id.tv_rv_photo)
        val rv_name = itemView.findViewById<TextView>(R.id.tv_rv_name)
        val rv_phone = itemView.findViewById<TextView>(R.id.tv_rv_phone)
    }


}