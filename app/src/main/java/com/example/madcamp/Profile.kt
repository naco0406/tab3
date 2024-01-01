package com.example.madcamp

data class Profile(
    val id: Long,  // DiffUtil에서 item 구별을 위한 속성 필요
    val image: String,
    val name : String,
    val phone : String
)
