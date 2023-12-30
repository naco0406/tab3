package com.example.madcamp

import android.Manifest
import android.app.Activity
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
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Type
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 100)
        }
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()){success ->
            if (success) {
                Log.d("Photo captured", "Photo captured")
                val photoUri = Uri.parse(currentPhotoPath)
                addImageModal(photoUri)
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoAllData: MutableList<PhotoData> = mutableListOf()

        val context = context ?: return
        val jsonUtility = JsonUtility(context)
        try {
            val jsonData = jsonUtility.readJson("data_image.json")
            val photoType: Type = object : TypeToken<List<PhotoData>>() {}.type
            val photos = jsonUtility.parseJson<List<PhotoData>>(jsonData, photoType)
            photos.forEach {
                photoAllData.add(it)
            }
//            Log.d("Tab3", "Photo Place: ${photoAllData[0][0].place}, Timestamp: ${photoAllData[0][0].timestamp}")
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
                        Log.d("PlusButton", "Button Clicked")
//                        addImageModal()
                        dispatchTakePictureIntent()
                    } else {
                        val place = photoAllData[index].place
                        val timestamp = photoAllData[index].timestamp
                        val star = photoAllData[index].star
//                        Log.d("ShowImageModal", "resId: $resId, place: $place, timestamp: $timestamp, star: $star")
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
//        Log.d("MyFragment5", "adjustSquareImage run")
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
//                    Log.d("MyFragment2", "Original cell size: $originalSize")
                    val params = GridLayout.LayoutParams() as GridLayout.LayoutParams
                    params.width = cellSize
                    params.height = cellSize
                    params.setMargins(8, 8, 8, 8)
//                    Log.d("MyFragment2", "Calculated cell size: $cellSize")
                    child.layoutParams = params
                    child.requestLayout()
//                    Log.d("MyFragment", "New size for child $i: ${params.width} x ${params.height}")
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
//        Log.d("Modal", "Modal Text: $modalText")
        modalTextView.setText(modalText)
        dialog.show()
    }

    lateinit var currentPhotoPath: String
//    private fun dispatchTakePictureIntent() {
//        Log.d("PictureIntent", "Take Picture")
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        activity?.let { activity ->
//            takePictureIntent.resolveActivity(activity.packageManager)?.also {
//                val photoFile: File? = try {
//                    createImageFile()
//                } catch (ex: IOException) {
//                    // 에러 처리
//                    null
//                }
//                photoFile?.also {
//                    val photoURI: Uri = FileProvider.getUriForFile(
//                        activity,
//                        "${activity.packageName}.provider",
//                        it
//                    )
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                    ActivityCompat.startActivityForResult(activity, takePictureIntent, 1, null)
//                }
//            }
//        }
//    }
    private fun dispatchTakePictureIntent() {
        Log.d("PictureIntent", "Take Picture")
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e("Tab2", "사진 파일 생성 실패", ex)
            null
        }
        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireActivity().packageName}.provider",
                it
            )
            takePictureLauncher.launch(photoURI) // 수정된 부분
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("ImageResulting", "requestCode: $requestCode, resultCode: $resultCode")
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // 촬영한 사진 처리
            // 예: ImageView에 사진 표시, 메타데이터와 함께 JSON 파일에 저장 등

        }
    }


    private fun addImageModal(photoUri: Uri){
        Log.d("addImageModal", "addImageModal")
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image) // 별도의 레이아웃 파일
        val modalImageView = dialog.findViewById<ImageView>(R.id.dialogImageView)
        val modalTextView = dialog.findViewById<TextView>(R.id.dialogTextView)
        val file = File(currentPhotoPath)
        Log.d("addImageModal", "photoUri : $photoUri")
        Glide.with(this)
            .load(file)
            .into(modalImageView)


        val place = "test"
        val timestamp = Timestamp.valueOf("2023-11-02 11:28:12")
        val star = 5
        val modalText = modalDataToText(place, timestamp, star)
//        Log.d("Modal", "Modal Text: $modalText")
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