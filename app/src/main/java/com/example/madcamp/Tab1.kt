package com.example.madcamp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
class Tab1 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val jsonData = jsonUtility.readJson("test.json")
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab1.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab1().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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

}