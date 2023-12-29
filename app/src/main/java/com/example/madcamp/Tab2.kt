package com.example.madcamp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception
import java.lang.reflect.Type
import java.sql.Timestamp
import java.text.SimpleDateFormat

data class PhotoData(
    val place: String,
    val timestamp: Timestamp,
    val star: Int
)

class Tab2 : Fragment() {

    override fun onResume() {
        super.onResume()
        val rootView = view
        val gridLayout = rootView?.findViewById<GridLayout>(R.id.gridview)
        gridLayout?.requestLayout()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoAllData: MutableList<List<PhotoData>> = mutableListOf()

        val context = context ?: return
        val jsonUtility = JsonUtility(context)
        try {
            val jsonData = jsonUtility.readJson("data.json")
            val photoType: Type = object : TypeToken<List<PhotoData>>() {}.type
            val photos = jsonUtility.parseJson<List<PhotoData>>(jsonData, photoType)
            photoAllData.add(photos)
            Log.d("Tab3", "Photo Place: ${photoAllData[0][0].place}, Timestamp: ${photoAllData[0][0].timestamp}")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val gridLayout: GridLayout = view.findViewById(R.id.gridview)
        val imageResIds = listOf(
            R.drawable.image_choonsik1,
            R.drawable.image_choonsik2,
            R.drawable.image_choonsik3,
            R.drawable.image_choonsik4,
            R.drawable.image_choonsik5,
            R.drawable.image_cat1,
            R.drawable.image_cat2,
            R.drawable.image_cat3,
            R.drawable.image_cat4,
            R.drawable.image_cat5,
            R.drawable.image_choonsik1,
            R.drawable.image_choonsik2,
            R.drawable.image_choonsik3,
            R.drawable.image_choonsik4,
            R.drawable.image_choonsik5,
            R.drawable.image_cat1,
            R.drawable.image_cat2,
            R.drawable.image_cat3,
            R.drawable.image_cat4,
            R.drawable.image_cat5,
        )
        val plusAddedIds = imageResIds + R.drawable.baseline_add_24
        val sizeIds = plusAddedIds.size
//        Log.d("MyFragment0", "Ids: $sizeIds")
        plusAddedIds.forEachIndexed() { index, resId ->
            val imageView = ImageView(context).apply {
//                Log.d("MyFragment3", "Original cell size: $resId")
                setImageResource(resId)
                setOnClickListener{
                    if (index == plusAddedIds.size - 1){

                    } else {
                        val place = photoAllData[0][index].place
                        val timestamp = photoAllData[0][index].timestamp
                        val star = photoAllData[0][index].star
                        Log.d("ShowImageModal", "resId: $resId, place: $place, timestamp: $timestamp, star: $star")
                        showImageModal(resId, place, timestamp, star)
                    }
                }

                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // 초기 너비 0으로 설정
                    height = 0 // 초기 높이 0으로 설정
                    setMargins(8, 8, 8, 8)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    tag = index
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    if (index == plusAddedIds.size - 1){
                        scaleType = ImageView.ScaleType.CENTER
                    }
                }
                elevation = 10f
                translationZ = 10f
            }
            gridLayout.addView(imageView)
        }
        gridLayout.requestLayout()

        adjustSquareImage(gridLayout, plusAddedIds.size)
        Log.d("MyFragment5", "adjustSquareImage run")
    }

    private fun adjustSquareImage(gridLayout: GridLayout, childImageNumber: Int) {
        gridLayout.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                gridLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val totalWidth = gridLayout.width - gridLayout.paddingLeft - gridLayout.paddingRight
                val expectedMargin = 12
                val totalMargin = (gridLayout.columnCount - 1) * expectedMargin * 2 // 마진의 2배를 고려

                val cellSize = (totalWidth - totalMargin) / gridLayout.columnCount
                for (i in 0 until childImageNumber) {
                    val child = gridLayout.getChildAt(i) as ImageView
                    val originalSize = child.layoutParams.width
                    Log.d("MyFragment2", "Original cell size: $originalSize")
                    val params = GridLayout.LayoutParams() as GridLayout.LayoutParams
                    params.width = cellSize
                    params.height = cellSize
                    params.setMargins(8, 8, 8, 8)
                    Log.d("MyFragment2", "Calculated cell size: $cellSize")
                    child.layoutParams = params
                    child.requestLayout()
                    Log.d("MyFragment", "New size for child $i: ${params.width} x ${params.height}")

                }
            }
        })
    }

    private fun showImageModal(imageResId: Int, place: String, timestamp: Timestamp, star: Int){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image) // 별도의 레이아웃 파일
        val modalImageView = dialog.findViewById<ImageView>(R.id.dialogImageView)
        val modalTextView = dialog.findViewById<TextView>(R.id.dialogTextView)
        modalImageView.setImageResource(imageResId) // 큰 이미지로 변경
        val modalText = modalDataToText(place, timestamp, star)
        Log.d("Modal", "Modal Text: $modalText")
        modalTextView.setText(modalText)
        dialog.show()
    }

    fun modalDataToText(place: String, timestamp: Timestamp, star: Int): String {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
        val formattedDate = dateFormat.format(timestamp)
        val stars = "⭐".repeat(star)

        return "장소: $place\n시간: $formattedDate\n별점: $stars"
    }

}

class JsonUtility(private val context: Context){
    fun readJson(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
    fun <T> parseJson(jsonData: String, clazz: Type): T{
        val gson = Gson()
        return gson.fromJson(jsonData, clazz)
    }
}