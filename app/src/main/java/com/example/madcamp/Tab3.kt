package com.example.madcamp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.sql.Timestamp
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception
import java.lang.reflect.Type


data class Photo(
    val place: String,
    val timestamp: Timestamp,
    val star: Int
)
class Tab3 : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        val jsonUtil = JsonUtil(context)
        try {
            val jsonData = jsonUtil.readJson("data_image.json")
            val photoType: Type = object : TypeToken<List<Photo>>() {}.type
            val photo = jsonUtil.parseJson<List<Photo>>(jsonData, photoType)

            android.util.Log.d("Tab3", "Photo Place: ${photo[0].place}, Timestamp: ${photo[0].timestamp}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

class JsonUtil(private val context: Context){
    fun readJson(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
    fun <T> parseJson(jsonData: String, clazz: Type): T{
        val gson = Gson()
        return gson.fromJson(jsonData, clazz)
    }
}