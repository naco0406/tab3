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
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import java.util.SimpleTimeZone

data class PhotoData(
    val place: String,
    val timestamp: Timestamp,
    val star: Int,
    val people: List<String>,
    val type: String,
    val uri: String
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
        JsonUtility(requireContext()).copyFileToInternalStorage("data_sample_image.json", "data_image.json")
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
            } else {
                Log.d("Photo capture", "Canceled or failed")
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

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            dispatchTakePictureIntent()
        }

        val photoAllData: MutableList<PhotoData> = mutableListOf()
        val photos = JsonUtility(requireContext()).readPhotoData("data_image.json")
        photos.forEach {
            photoAllData.add(it)
        }
        Log.d("Initial JSON Data", "$photos")

        val gridLayout: GridLayout = view.findViewById(R.id.gridview)
        photoAllData.forEachIndexed { index, photoData ->
            val place = photoData.place
            val timestamp = photoData.timestamp
            val star = photoData.star
            val people = photoData.people
            val type = photoData.type
            val uri = photoData.uri
            val imageView = ImageView(context).apply {
                setOnClickListener {
                    showImageModal(uri, type, place, timestamp, star, people)
                }
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // 초기 너비 0으로 설정
                    height = 0 // 초기 높이 0으로 설정
                    setMargins(8, 8, 8, 8)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    tag = index
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                elevation = 10f
                translationZ = 10f
            }
            if (type == "internal"){
                val imageId = context?.resources?.getIdentifier(uri, "drawable", requireContext().packageName)
                Log.d("Image Loader", "Image Uri from JSON: $imageId")
                Glide.with(this)
                    .load(imageId)
                    .into(imageView)
            } else if (type == "external"){
                val imageUri = Uri.parse(uri)
                Glide.with(this)
                    .load(imageUri)
                    .into(imageView)
            } else {
                error("Invalid Image Type")
            }
            gridLayout.addView(imageView)
        }
        gridLayout.requestLayout()
        adjustSquareImage(gridLayout, photoAllData.size)
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

    private fun showImageModal(uri: String, type: String, place: String, timestamp: Timestamp, star: Int, people: List<String>){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image) // 별도의 레이아웃 파일
        val modalImageView = dialog.findViewById<ImageView>(R.id.dialogImageView)
        val modalTextView = dialog.findViewById<TextView>(R.id.dialogTextView)
        if (type == "internal"){
            val imageId = context?.resources?.getIdentifier(uri, "drawable", requireContext().packageName)
            Log.d("Image Loader", "Image Uri from JSON: $imageId")
            Glide.with(this)
                .load(imageId)
                .into(modalImageView)
        } else if (type == "external"){
            val imageUri = Uri.parse(uri)
            Glide.with(this)
                .load(imageUri)
                .into(modalImageView)
            Log.d("External image", "External Iamge Loaded")
        } else {
            error("Invalid Image Type")
        }

        val modalText = modalDataToText(place, timestamp, star, people)
//        Log.d("Modal", "Modal Text: $modalText")
        modalTextView.setText(modalText)
        dialog.show()
    }

    lateinit var currentPhotoPath: String
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
        val people = listOf("하니", "민지")
        val modalText = modalDataToText(place, timestamp, star, people)
//        Log.d("Modal", "Modal Text: $modalText")
        modalTextView.setText(modalText)
        dialog.show()
        val newPhotoData = PhotoData(
            place = "카이스트",
            timestamp = Timestamp(System.currentTimeMillis()),
            star = 5,
            people = listOf("하니", "민지"),
            type = "external",
            uri = photoUri.toString()
        )
        JsonUtility(requireContext()).appendPhotoJson("data_image.json", newPhotoData)
        refreshGridLayout()
        Log.d("refreshGridLayout", "refreshGridLayout")

        val photoDataList = JsonUtility(requireContext()).readPhotoData("data_image.json")
        Log.d("Updated JSON Data", photoDataList.toString())

    }

    private fun refreshGridLayout() {
        val gridLayout: GridLayout = view?.findViewById(R.id.gridview) ?: return
        gridLayout.removeAllViews() // 기존의 모든 뷰 제거

        val newPhotoDataList = JsonUtility(requireContext()).readPhotoData("data_image.json")
        newPhotoDataList.forEach { photoData ->
            val imageView = ImageView(context).apply {
                setOnClickListener {
                    showImageModal(photoData.uri, photoData.type, photoData.place, photoData.timestamp, photoData.star, photoData.people)
                }
                layoutParams = GridLayout.LayoutParams().apply {
                    width = GridLayout.LayoutParams.WRAP_CONTENT
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    setMargins(8, 8, 8, 8)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                elevation = 10f
                translationZ = 10f
            }

            when (photoData.type) {
                "internal" -> {
                    val imageId = resources.getIdentifier(photoData.uri, "drawable", requireContext().packageName)
                    Glide.with(this).load(imageId).into(imageView)
                }
                "external" -> {
                    val file = File(photoData.uri)
                    Glide.with(this).load(file).into(imageView)
                }
            }

            gridLayout.addView(imageView)

        }
        gridLayout.requestLayout()
        adjustSquareImage(gridLayout, newPhotoDataList.size)
    }

    fun modalDataToText(place: String, timestamp: Timestamp, star: Int, people: List<String>): String {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
        val formattedDate = dateFormat.format(timestamp)
        val stars = "⭐".repeat(star)
        val peopleText = people.joinToString(", ")

        return "장소: $place\n시간: $formattedDate\n별점: $stars\n인물: $peopleText"
    }

}
