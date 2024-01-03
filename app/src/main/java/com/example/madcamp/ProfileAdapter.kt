package com.example.madcamp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ProfileAdapter(private var profileListAll: MutableList<Profile>):
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>(), Filterable {

    var profileList: MutableList<Profile> = ArrayList(profileListAll)
    private var onItemClickListener: OnItemClickListener? = null

    init {
        sortByName()
    }

    fun sortByName() {
        profileList.sortBy { it.name }
        notifyDataSetChanged()
    }

    // 뷰가 만들어질때 호출되는 메서드. 뷰홀더를 생성해 리턴
    // recyclerview 만들어질때만 호출
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_item, parent, false)
        return ProfileViewHolder(view)
    }


    // 뷰와 내용 연결
    // recyclerview -> 재사용. 스크롤 내리거나 올릴때 호출
    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profileList[position]

        Glide.with(holder.itemView.context)
            .load(profile.image)
            .override(100, 100)
            .placeholder(R.drawable.outline_image_24)
            .error(R.drawable.outline_broken_image_24)
            .into(holder.rv_image)

        holder.rv_name.text = profile.name
        holder.rv_phone.text = profile.phone
    }

    override fun getItemCount(): Int {
       // Log.d("size", profileList.size.toString())
        return profileList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun updateData(newProfileList: List<Profile>) {
        this.profileListAll = newProfileList.toMutableList()
        this.profileList = ArrayList(newProfileList)
        sortByName()
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrBlank()) {
                    profileListAll
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    profileListAll.filter {
                        it.name.lowercase().contains(filterPattern) ||
                                it.phone.replace("-", "").contains(filterPattern)
                    }.toMutableList()
                }

                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                profileList.clear()
                profileList.addAll(results?.values as List<Profile>)
                notifyDataSetChanged()
            }
        }
    }

    // 해당 뷰와 연결
    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rv_image : ImageView = itemView.findViewById(R.id.tv_rv_photo)
        val rv_name = itemView.findViewById<TextView>(R.id.tv_rv_name)
        val rv_phone = itemView.findViewById<TextView>(R.id.tv_rv_phone)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(position)
                }
            }
        }
    }

    private val profileFilter = object: Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            // 검색어에 해당하는 프로필을 찾아냄
            val filteredList = mutableListOf<Profile>()

            if (constraint.isNullOrBlank()) {
                // 검색어가 비어있거나 null이면 전체 프로필 반환
                filteredList.addAll(profileListAll)
            } else{
                val filterPattern = constraint.toString().trim().replace("-", "")
                for (profile in profileListAll) {
                    val profileNameLower = profile.name.lowercase()
                    val profilephone = profile.phone.replace("-", "")

                    if(profileNameLower.contains(filterPattern) || profilephone.contains(filterPattern)) {
                        filteredList.add(profile)
                    }
                }
            }

            // 결과를 FilterResults 객체에 담아 반환
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        // 필터링된 결과 어댑터에 적용하고, RecyclerView 갱신
        // DiffUtil을 이용해 변경사항 계산, 어댑터에 반영
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

            val newProfileList = results?.values as List<Profile>

            val diffCallBack =  ProfileDiffCallBack(profileList, newProfileList)
            val diffResult = DiffUtil.calculateDiff(diffCallBack)

            profileList.clear()
            profileList.addAll(newProfileList)
            sortByName()
            notifyDataSetChanged()
            diffResult.dispatchUpdatesTo(this@ProfileAdapter)
        }
    }

    // 이전 목록과 새로운 목록 간의 차이를 계산
    private class ProfileDiffCallBack(
        private val oldList: List<Profile>,
        private val newList: List<Profile>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

}