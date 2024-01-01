package com.example.madcamp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONException
import java.io.File

class EditActivity : AppCompatActivity() {

    private var profilePosition: Int = -1
    private lateinit var profileAdapter:ProfileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // Intent에서 전달된 데이터 받기
        val id = intent.getLongExtra("id", -1)
        Log.d("Intent_id", id.toString())
        val name = intent.getStringExtra("name")
        val phone = intent.getStringExtra("phone")
        val imageUrl = intent.getStringExtra("image")

        val textViewId = findViewById<TextView>(R.id.userId)
        val editTextViewName = findViewById<EditText>(R.id.editUserName)
        val editTextViewPhone = findViewById<EditText>(R.id.editPhone)
        val editImageView = findViewById<ImageView>(R.id.editUserImage)

        val saveButton = findViewById<Button>(R.id.saveButton)
        val deleteButton = findViewById<Button>(R.id.deleteButton)

//        textViewId.text = id.toString()
        // 이전 화면에서 선택된 프로필의 위치
        profilePosition = intent.getLongExtra("id", -1).toInt()

        editTextViewName.text = Editable.Factory.getInstance().newEditable(name)
        editTextViewPhone.text = Editable.Factory.getInstance().newEditable(phone)

        Glide.with(this)
            .load(imageUrl)
            .override(100, 100)
            .placeholder(R.drawable.ic_home)
            .error(R.drawable.baseline_question_mark_24)
            .into(editImageView)

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

        // 액티비티 종료
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
//            profileAdapter.updateData(updatedProfiles)
//            Log.d("profileAdapter.updateData", updatedProfilesJson)

            // 삭제된 정보를 Tab1화면으로 전달하고, 액티비티 시작
            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("deletedPosition", profilePosition)
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
//    private fun showEditDialog(editTextViewName: EditText) {
//        val editText = EditText(this)
//        editText.setText(editTextViewName.text)
//
//        val dialog = AlertDialog.Builder(this)
//            .setTitle("이름 수정")
//            .setView(editText)
//            .setPositiveButton("확인") { _, _ ->
//                // 수정된 텍스트를 저장하거나 처리하는 로직 추가
//                editTextViewName.text = editText.text
//            }
//            .setNegativeButton("취소", null)
//            .create()
//
//        // 키보드 표시
//        editText.requestFocus()
//        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
//
//        dialog.show()
//    }


}

