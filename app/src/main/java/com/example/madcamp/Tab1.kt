package com.example.madcamp

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.Contacts.Photo
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type


class Tab1 : Fragment(), ProfileAdapter.OnItemClickListener {

    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var dialogView: View
    private lateinit var userImage: ImageButton

    private var profileAllData: MutableList<Profile> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JsonUtility(requireContext()).copyFileToInternalStorage("data_sample_user.json", "data_user.json")

        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        // 권한 요청
        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, 100)
        }
    }
    private fun hasPermissions(permissions: Array<String>): Boolean {
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_tab1, container, false)

        dialogView = layoutInflater.inflate(R.layout.profile_add_dialog, null)
        userImage = dialogView.findViewById(R.id.addUserImage)
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == 1) {
            val imageUri: Uri? = data?.data

            // Uri를 String으로 변환
            val imagePath: String = imageUri?.toString() ?: ""

            Log.d("onActivityResultFile", imagePath)

            // Glide를 사용하여 이미지 로드
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.outline_image_24)
                .error(R.drawable.outline_broken_image_24)
                .into(userImage)

            // setTag로 View에 추가정보를 저장
            // 이미지의 uri를 객체에 연결된 추가정보로 저장.
            userImage.setTag(R.id.addUserImage, imageUri)  // 추가
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv_profile = view.findViewById<RecyclerView>(R.id.rv_profile)

        val context = context ?:return
        val jsonUtility = JsonUtility(context)
        try {
//            val jsonData = jsonUtility.readJson("data_sample_user.json").toString()
//            val profileType: Type = object: TypeToken<List<Profile>>() {}.type
//            val profiles = jsonUtility.parseJson<List<Profile>>(jsonData, profileType)
            val profiles = jsonUtility.readProfileData("data_user.json")

            profiles.forEach{
                profileAllData.add(it)
            }
//            Log.d("ProfileList: ", "image: ${profileAllData[0][0].image}, name: ${profileAllData[0][0].name}, phone: ${profileAllData[0][0].phone}")

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ArrayList로 어댑터 만들고, 어댑터와 리사이클러뷰 갱신
        val profileAdapter = ProfileAdapter(profileAllData)

        rv_profile.adapter = profileAdapter
        context?.let {
            rv_profile.layoutManager = LinearLayoutManager(it)
            rv_profile.addItemDecoration(VerticalItemDecorator(10))
//            profileAdapter.addItemDecoration(it, rv_profile)
        }
        profileAdapter.notifyDataSetChanged()

        val fabProfile: FloatingActionButton = view.findViewById(R.id.fab_profile)

        dialogView = layoutInflater.inflate(R.layout.profile_add_dialog, null)
        userImage = dialogView.findViewById<ImageButton>(R.id.addUserImage)
        val userName = dialogView.findViewById<EditText>(R.id.addUserName).text
        val userPhone = dialogView.findViewById<EditText>(R.id.addUserPhone).text

        val transformation = MultiTransformation(CenterCrop(), RoundedCorners(16))

        // Profile Add Button
        val transformation = MultiTransformation(RoundedCorners(16))
        fabProfile.setOnClickListener {
            userName.clear()
            userPhone.clear()
            Log.d("fabProfile", "clicked")
            Glide.with(this)
                .load(R.drawable.image_cat1)  // 기본이미지
                .apply(RequestOptions.bitmapTransform(transformation))
                .placeholder(R.drawable.outline_image_24)
                .apply((RequestOptions.bitmapTransform(transformation)))
                .error(R.drawable.outline_broken_image_24)
                .into(userImage)

            val alertDialog = android.app.AlertDialog.Builder(context)
            val parentView = dialogView.parent as? ViewGroup
            parentView?.removeView(dialogView)

            alertDialog.setTitle("연락처 추가")
                .setView(dialogView)
                .setPositiveButton("저장",
                    DialogInterface.OnClickListener { dialog, which ->
                        Log.d("saveButton", "clicked")

                        val ProfileList = JsonUtility(requireContext()).readProfileData("data_user.json")

                        val newProfile = if (ProfileList.isNotEmpty()) {
                            // 프로필이 하나 이상 있는 경우
                            val lastProfileId = ProfileList.last().id
                            Profile(
                                id = lastProfileId + 1,
                                // ImageButton에 연결된 추가정보를 불러와 문자열로 변환
                                // 선택한 이미지의 uri를 Profile 객체에 설정
                                image = (userImage.getTag(R.id.addUserImage) as? Uri)?.toString() ?: "",
                                name = userName.toString(),
                                phone = userPhone.toString()
                            )
                        } else {
                            // 프로필이 없는 경우
                            Profile(
                                id = 1,
                                image = (userImage.getTag(R.id.addUserImage) as? Uri)?.toString() ?: "", // 이미지 경로 가져오기
                                name = userName.toString(),
                                phone = userPhone.toString()
                            )
                        }
                        Log.d("AddDialog: ", newProfile.toString())
                        JsonUtility(requireContext()).appendProfileJson("data_user.json", newProfile)

                        updateRecyclerView()  // 리사이클러뷰 업데이트
                        dialog.dismiss()

                    })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, which ->
                        Log.d("NegativeButton", "clicked")
                        dialog.dismiss()
                    })
                .show()
            userImage.setOnClickListener{
                openGalleryForImage(userImage)
            }
        }

        // SearchView에 포커스 설정
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        searchView.setOnQueryTextFocusChangeListener{ searchView, hasFocus ->
            if (hasFocus) {
                showKeyboard(searchView)
            } else{
                hideKeyboard(searchView)
            }
        }

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                profileAdapter.filter.filter(newText)
                return false
            }
        })
        searchView.clearFocus()

        this@Tab1.profileAdapter = ProfileAdapter(profileAllData)
        rv_profile.adapter = profileAdapter

        // RecyclerView 아이템 클릭 리스너 설정
        profileAdapter.setOnItemClickListener(this)
    }

    // 갤러리에서 이미지 선택
    private fun openGalleryForImage(userImage: ImageButton) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }
    private fun updateRecyclerView() {

        profileAllData.clear()

        // JSON 파일 다시 읽기
        val context = context ?:return
        val jsonUtility = JsonUtility(context)
        val updatedProfiles = jsonUtility.readProfileData("data_user.json").toMutableList()

        updatedProfiles.forEach{
            profileAllData.add(it)
        }
        Log.d("updateRecyclerView ", profileAllData.toString())
        profileAdapter.updateData(profileAllData)
//        val profileAdapter = ProfileAdapter(profileAllData)
//        val rv_profile = view?.findViewById<RecyclerView>(R.id.rv_profile)
//        rv_profile?.adapter = profileAdapter
//        profileAdapter.sortByName()
        this@Tab1.profileAdapter = ProfileAdapter(profileAllData)
//
//        // RecyclerView 아이템 클릭 리스너 설정
//        profileAdapter.setOnItemClickListener(this)
//
//        profileAdapter.notifyDataSetChanged()
//        Log.d("notifyDataSetChanged", "")
        profileAdapter.sortByName()
        profileAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        updateRecyclerView()
    }

    override fun onItemClick(position: Int) {

        val selectedProfile = profileAdapter.profileList[position]
        Log.d("Tab1", "selectedProfile: $selectedProfile")

        // ProfileSubActivity로 이동하는 Intent 생성
        val tabIntent = Intent(requireContext(), ProfileSubActivity::class.java).apply {
            putExtra("profileId", selectedProfile.id)
            putExtra("image", selectedProfile.image)
            putExtra("name", selectedProfile.name)
            putExtra("phone", selectedProfile.phone)
        }
        startActivity(tabIntent)
    }

    // 키보드 보여주기
    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    // 키보드 숨기기
    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}

class JsonUtility(private val context: Context) {

    fun readJson(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use{it.readText()}
    }

    // gson - json 파싱과정을 간단하게
    fun <T> parseJson(jsonData: String, clazz: Type): T {
        val gson = Gson()
        return gson.fromJson(jsonData, clazz)
    }

    fun readProfileData(fileName: String): List<Profile> {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val jsonData = file.readText()
            val profileType: Type = object : TypeToken<List<Profile>>() {}.type

            return Gson().fromJson(jsonData, profileType) ?: emptyList()
        } else {
            return emptyList()
        }
    }

    fun appendProfileJson(fileName: String, newData: Profile) {
        try {
            val file = File(context.filesDir, fileName)
            val data: MutableList<Profile>
            if (file.exists()) {
                val jsonData = file.readText()
                val profileType: Type = object :TypeToken<List<Profile>>() {}.type
                data = Gson().fromJson(jsonData, profileType)
            } else {
                data = mutableListOf()
            }
            data.add(newData)
            file.writeText(Gson().toJson(data))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun appendPhotoJson(fileName: String, newData: PhotoData){
        try {
            val file = File(context.filesDir, fileName)
            val data: MutableList<PhotoData>
            if (file.exists()) {
                val jsonData = file.readText()
                val photoType: Type = object : TypeToken<List<PhotoData>>() {}.type
                data = Gson().fromJson(jsonData, photoType)
            } else {
                data = mutableListOf()
            }
            // 데이터 추가하는 부분
            data.add(newData)
            file.writeText(Gson().toJson(data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyFileToInternalStorage(fileName: String, toName: String) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open(fileName)
            val outputFile = File(context.filesDir, toName)
            if (!outputFile.exists()) {
                inputStream.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // 로그에 오류 출력
            // 오류 처리 (예: 사용자에게 알림)
        }
    }
    fun readPhotoData(fileName: String): List<PhotoData> {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            val jsonData = file.readText()
            val photoType: Type = object : TypeToken<List<PhotoData>>() {}.type
            Gson().fromJson(jsonData, photoType)
        } else {
            Log.d("readPhotoData", "No such file")
            emptyList()
        }
    }

    fun updatePhotoDataJson(fileName: String, updatedPhoto: PhotoData) {
        try {
            val file = File(context.filesDir, fileName)
            val photos: MutableList<PhotoData>
            Log.d("updateProfile","$file")

            if (file.exists()) {
                val jsonData = file.readText()
                val photoType = object : TypeToken<List<PhotoData>>() {}.type
                photos = Gson().fromJson(jsonData, photoType)
                Log.d("updateProfile","$photos")
            } else {
                photos = mutableListOf()
            }

            // 특정 id를 가진 프로필 찾아서 업데이트
            val index = photos.indexOfFirst { it.uri == updatedPhoto.uri }
            if (index != -1) {
                photos[index] = updatedPhoto
                Log.d("updatePhoto","$updatedPhoto")
            } else {
                // 프로필이 존재하지 않는 경우 새로 추가
                photos.add(updatedPhoto)
            }

            // 수정된 프로필 리스트를 JSON으로 변환하여 파일에 저장
            Log.d("updatePhoto","$photos")
            file.writeText(Gson().toJson(photos))
            Log.d("updatePhoto","Gson")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateProfileDataJson(fileName: String, updatedProfile: Profile) {
        try {
            val file = File(context.filesDir, fileName)
            val profiles: MutableList<Profile>
            Log.d("updateProfile","$file")

            if (file.exists()) {
                val jsonData = file.readText()
                val profileType = object : TypeToken<List<Profile>>() {}.type
                profiles = Gson().fromJson(jsonData, profileType)
                Log.d("updateProfile","$profiles")
            } else {
                profiles = mutableListOf()
            }

            // 특정 id를 가진 프로필 찾아서 업데이트
            val index = profiles.indexOfFirst { it.id == updatedProfile.id }
            if (index != -1) {
                profiles[index] = updatedProfile
                Log.d("updateProfile","$updatedProfile")
            } else {
                // 프로필이 존재하지 않는 경우 새로 추가
                profiles.add(updatedProfile)
            }

            // 수정된 프로필 리스트를 JSON으로 변환하여 파일에 저장
            Log.d("updateProfile","$profiles")
            file.writeText(Gson().toJson(profiles))
            Log.d("updateProfile","Gson")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}