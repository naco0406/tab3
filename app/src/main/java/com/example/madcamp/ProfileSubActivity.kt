package com.example.madcamp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import kotlin.properties.Delegates

class ProfileSubActivity : AppCompatActivity() {
    private var profileId by Delegates.notNull<Long>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_sub)
        supportActionBar?.hide()

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val data = Intent()
            data.putExtra("updateNeeded", true)
            setResult(Activity.RESULT_OK, data)
            onBackPressed()
        }

        val backgroundImage = findViewById<ImageView>(R.id.backgroundUserImage)
        val backgroundGradient = findViewById<View>(R.id.gradientView)
        backgroundImage.post {
            val width = backgroundImage.width // 가로 길이 가져오기
            val params = backgroundImage.layoutParams
            params.height = width // 세로 길이를 가로 길이와 동일하게 설정
            backgroundImage.layoutParams = params // 레이아웃 파라미터 업데이트
        }
        backgroundGradient.post {
            val width = backgroundGradient.width // 가로 길이 가져오기
            val params = backgroundGradient.layoutParams
            params.height = width // 세로 길이를 가로 길이와 동일하게 설정
            backgroundGradient.layoutParams = params // 레이아웃 파라미터 업데이트
        }

        val imageView = findViewById<ImageView>(R.id.userImage)
        val textViewId = findViewById<TextView>(R.id.userId)
        val textViewName = findViewById<TextView>(R.id.textViewName)
        val textViewPhone = findViewById<TextView>(R.id.textViewPhone)

        val btnCall = findViewById<ImageButton>(R.id.call_phone)
        val btnMessage = findViewById<ImageButton>(R.id.call_message)
        val btnEdit = findViewById<Button>(R.id.edit)

        // Intent에서 전달된 데이터 받기
        profileId = intent.getLongExtra("profileId", -1)
        Log.d("ProfileSubActivity_id", profileId.toString())
        val name = intent.getStringExtra("name")
        val phone = intent.getStringExtra("phone")
        val imageUrl = intent.getStringExtra("image")

        // 받은 데이터로 View 업데이트
        textViewName.text = name
        textViewPhone.text = phone

        Glide.with(this)
            .load(imageUrl)
            .circleCrop()
            .placeholder(R.drawable.outline_image_24)
            .error(R.drawable.outline_broken_image_24)
            .into(imageView)
        Glide.with(this)
            .load(imageUrl)
            .into(backgroundImage)


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
                val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null))
                startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.SEND_SMS),
                    SEND_SMS_PERMISSION_REQUEST_CODE
                )
            }
        }

        btnEdit.setOnClickListener {

            val editIntent = Intent(this, EditActivity::class.java)

            // 현재 사용자 정보를 Intent에 추가
            editIntent.putExtra("id", profileId)
            editIntent.putExtra("name", name)
            editIntent.putExtra("phone", phone)
            editIntent.putExtra("image", imageUrl)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(editIntent)
            overridePendingTransition(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        updateSubActivity()
    }
    private fun updateSubActivity() {

        // JSON 파일 다시 읽기
        val context = this
        val jsonUtility = JsonUtility(context)
        val profiles = jsonUtility.readProfileData("data_user.json").toMutableList()

        val textViewName = findViewById<TextView>(R.id.textViewName)
        val textViewPhone = findViewById<TextView>(R.id.textViewPhone)

        profiles.forEach{
            if (it.id == profileId){
                textViewName.setText(it.name)
                textViewPhone.setText(it.phone)
            }
        }
    }


    companion object {
        // 권한 요청 결과 식별을 위한 코드?
        private const val CALL_PHONE_PERMISSION_REQUEST_CODE = 1001
        private const val SEND_SMS_PERMISSION_REQUEST_CODE = 1002
    }



}