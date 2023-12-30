package com.example.madcamp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ActivityCompat

class ProfileSubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_sub)

        val imageView = findViewById<ImageView>(R.id.userImage)
        val textViewName = findViewById<TextView>(R.id.textViewName)
        val textViewPhone = findViewById<TextView>(R.id.textViewPhone)

        val btnCall = findViewById<ImageButton>(R.id.call_phone)
        val btnMessage = findViewById<ImageButton>(R.id.call_message)

        // Intent에서 전달된 데이터 받기
        val name = intent.getStringExtra("name")
        val phone = intent.getStringExtra("phone")
        val imageUrl = intent.getStringExtra("image")

        // 받은 데이터로 View 업데이트
        textViewName.text = name
        textViewPhone.text = phone

        Glide.with(this)
            .load(imageUrl)
            .override(100, 100)
            .placeholder(R.drawable.ic_home)
            .error(R.drawable.baseline_question_mark_24)
            .into(imageView)

        // 전화걸기
        btnCall.setOnClickListener {
            val phoneNumber = textViewPhone.text.toString().trim()

            // 권한 확인
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val dialIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
                startActivity(dialIntent)
            } else {
                // 권한없으면 다시 권한 요청
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CALL_PHONE),
                    CALL_PHONE_PERMISSION_REQUEST_CODE
                )
            }
        }

        // 메시지 보내기
        btnMessage.setOnClickListener {
            val phoneNumber = textViewPhone.text.toString().trim()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
            ) {
//                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${phoneNumber}"))
                val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null))
                startActivity(intent)
            } else {
//                Toast.makeText(this, "권한이 필요합니다", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.SEND_SMS),
                    SEND_SMS_PERMISSION_REQUEST_CODE
                )
            }
        }
    }


    companion object {
        // 권한 요청 결과 식별을 위한 코드?
        private const val CALL_PHONE_PERMISSION_REQUEST_CODE = 1001
        private const val SEND_SMS_PERMISSION_REQUEST_CODE = 1002
    }



}