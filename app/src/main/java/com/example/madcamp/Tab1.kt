package com.example.madcamp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Tab1.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab1 : Fragment(), ProfileAdapter.OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var imageView: ImageView
    private lateinit var profileAdapter: ProfileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JsonUtility(requireContext()).copyFileToInternalStorage("data_sample_user.json", "data_user.json")
//        val users = JsonUtility(requireContext()).readPhotoData("data_user.json")
//        users.forEach {
//
//        }

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_tab1, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv_profile = view.findViewById<RecyclerView>(R.id.rv_profile)
        val profileAllData : MutableList<Profile> = mutableListOf()

        val context = context ?:return
        val jsonUtility = JsonUtility(context)
        try {
//            val jsonData = jsonUtility.readJson("data_sample_user.json")
            val jsonData = jsonUtility.readJson("data_sample_user.json").toString()
            val profileType: Type = object: TypeToken<List<Profile>>() {}.type
            val profiles = jsonUtility.parseJson<List<Profile>>(jsonData, profileType)

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

//        val deleteButton = view.findViewById<Button>(R.id.deleteButton)
//        deleteButton.setOnClickListener {
//
//        }

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

        // RecyclerView 아이템 클릭 리스너 설정
        profileAdapter.setOnItemClickListener(this)
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

//    fun readProfileData(fileName: String): List<Profile> {
//        val file = File(context.filesDir, fileName)
//        return if (file.exists()) {
//            val jsonData = file.readText()
//            val profileType: Type = object : TypeToken<List<Profile>>() {}.type
//            Gson().fromJson(jsonData, profileType)
//        } else {
//            Log.d("readProfileData", "No such file")
//            emptyList()
//        }
//    }

    fun updateProfileDataJson(fileName: String, updateData: Profile) {
        try {
            val file = File(context.filesDir, fileName)
            val data: MutableList<Profile>

            if (file.exists()) {
                val jsonData = file.readText();
                // gson으로 json데이터를 List<Profile>로 파싱
                val profileType: Type = object : TypeToken<List<Profile>>() {}.type
                data = Gson().fromJson(jsonData, profileType)
            } else {
                data = mutableListOf()
            }
            // updateData 추가하고 파일에 쓰기
            data.add(updateData)
            file.writeText(Gson().toJson(data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}