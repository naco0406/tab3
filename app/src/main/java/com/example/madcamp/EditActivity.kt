package com.example.madcamp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONException
import java.io.File

class EditActivity : AppCompatActivity() {

    private var profilePosition: Int = -1
    private lateinit var profileAdapter:ProfileAdapter

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        supportActionBar?.hide()

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
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

        // Intent에서 전달된 데이터 받기
        val id = intent.getLongExtra("id", -1)
        Log.d("Intent_id", id.toString())
        val name = intent.getStringExtra("name")
        val phone = intent.getStringExtra("phone")
        val imageUrl = intent.getStringExtra("image")

        val editTextViewName = findViewById<EditText>(R.id.editUserName)
        val editTextViewPhone = findViewById<EditText>(R.id.editPhone)
        val editImageView = findViewById<ImageView>(R.id.editUserImage)

        val saveButton = findViewById<Button>(R.id.saveButton)
        val deleteButton = findViewById<ImageButton>(R.id.deleteButton)

        // 이전 화면에서 선택된 프로필의 위치
        profilePosition = intent.getLongExtra("id", -1).toInt()

        editTextViewName.text = Editable.Factory.getInstance().newEditable(name)
        editTextViewPhone.text = Editable.Factory.getInstance().newEditable(phone)

        Glide.with(this)
            .load(imageUrl)
            .circleCrop()
            .placeholder(R.drawable.outline_image_24)
            .error(R.drawable.outline_broken_image_24)
            .into(editImageView)
        Glide.with(this)
            .load(imageUrl)
            .into(backgroundImage)

        saveButton.setOnClickListener {
            val updatedName = editTextViewName.text.toString()
            val updatedPhone = editTextViewPhone.text.toString()
            Log.d("updateProfile", "$updatedName")
            // 프로필 정보 업데이트
            if (imageUrl != null) {
                Log.d("updateProfile", "Attempt to update Profile")
                updateProfile(id, updatedName, updatedPhone, imageUrl)
            }

        }

        deleteButton.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    private fun updateProfile(id: Long, name: String, phone: String, imageUrl: String) {
        // JSON 파일 읽기
        val jsonUtility = JsonUtility(this)
        val profiles = jsonUtility.readProfileData("data_user.json").toMutableList()
        Log.d("updateProfile", "$profiles")
        // 프로필 정보 업데이트
        val index = profiles.indexOfFirst { it.id == id }
        if (index != -1) {
            profiles[index] = Profile(id, imageUrl, name, phone)
            jsonUtility.updateProfileDataJson("data_user.json", profiles[index])
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("profileId", id)
            putExtra("image", imageUrl)
            putExtra("name", name)
            putExtra("phone", phone)
        }
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    private fun showDeleteConfirmDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("삭제 확인")
            .setMessage("이 전화번호를 삭제하시겠습니까?")
            .setPositiveButton("확인") {_, _ ->
                deleteProfile()
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    @SuppressLint("LongLogTag")
    private fun deleteProfile() {
        Log.d("deleteProfile", "deleteProfile")
        if (profilePosition != -1) {
            Log.d("deleteProfile_if", "if")

            val profilesJsonString = readProfilesJsonFromStorage()
            val updatedProfilesJson = deleteProfile(profilesJsonString, profilePosition.toLong())

            // 업데이트된 JSON을 파일에 저장
            saveProfilesJsonToStorage(updatedProfilesJson)
            val updatedProfiles = parseJson(updatedProfilesJson)
            Log.d("Delete Completed, ", updatedProfilesJson)
            // 어댑터 업데이트

            // 삭제된 정보를 Tab1화면으로 전달하고, 액티비티 시작
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            finish()

        }
    }

    private fun parseJson(jsonString: String): List<Profile> {
        val profileList = mutableListOf<Profile>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val id = jsonObject.getInt("id").toLong()
                val name = jsonObject.getString("name")
                val phone = jsonObject.getString("phone")
                val image = jsonObject.getString("image")

                val profile = Profile(id, name, phone, image)
                profileList.add(profile)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return profileList
    }


    private fun readProfilesJsonFromStorage(): String {
        try {
            val fileName = "data_user.json"
            val file = File(filesDir, fileName)

            if (file.exists()) {
                return file.readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    private fun deleteProfile(profilesJsonString: String, position: Long): String {
        try {
            val profilesArray = JSONArray(profilesJsonString)

            for (i in profilesArray.length() -1 downTo 0) {
                val jsonObject = profilesArray.getJSONObject(i)
                if (jsonObject.getLong("id") == position) {
                    profilesArray.remove(i)
                    Log.d("deleteProfile", "deletedProfile: $profilesArray")
                    return profilesArray.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun saveProfilesJsonToStorage(updatedProfilesJson: String) {
        try {
            val fileName = "data_user.json"
            val file = File(filesDir, fileName)
            file.writeText(updatedProfilesJson)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}

