package com.example.madcamp

import android.Manifest
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
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    val title: String,
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
    private lateinit var profileList: List<Profile>
    private var selectedProfiles: List<Profile> = listOf()
    private var placeList: MutableList<String>? = mutableListOf()
    private var selectedPlace: String = "모두"

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var currentLocation: Location? = null
    private var currentCity: String? = null
    private var currentCountry: String? = null
    private var locationLoaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JsonUtility(requireContext()).copyFileToInternalStorage("data_sample_image.json", "data_image.json")
        val photos = JsonUtility(requireContext()).readPhotoData("data_image.json")
        photos.forEach {
            appendPlace(it.place)
        }
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 100)
        }
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
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

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // LocationListener 구현
        locationListener = LocationListener { location ->
            val latitude = location.latitude
            val longitude = location.longitude
//            Log.d("GPS Location2", "Latitude: $latitude, Longitude: $longitude")
            // 여기에 위치 정보를 사용한 추가 작업을 수행합니다.
            val geocoder = Geocoder(context, Locale.KOREA) // 한국어로 설정
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val city = address.locality // 도시
                    val country = address.countryName // 국가
//                    Log.d("Geocoder", "City: $city, Country: $country")
                    currentCity = city
                    currentCountry = country

                    // 여기에 위치 정보를 사용한 추가 작업을 수행합니다.
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        requestLocationUpdates(locationListener)
    }

    private fun requestLocationUpdates(locationListener: LocationListener) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfileData()
        setupSpinner()

        val gpsButton: Button = view.findViewById(R.id.gpsButton)
        gpsButton.setOnClickListener {
            //here
            requestLocationUpdates(locationListener)
            currentLocation?.let {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestSingleUpdate(
                        LocationManager.GPS_PROVIDER,
                        LocationListener { location ->
                            // 현재 위치 정보 사용
                            val latitude = location.latitude
                            val longitude = location.longitude
                            Log.d("GPS Location", "Latitude: $latitude, Longitude: $longitude")
                        },
                        null
                    )
                }
            }
        }

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            dispatchTakePictureIntent()
        }

        val photoAllData: MutableList<PhotoData> = mutableListOf()
        val photos = JsonUtility(requireContext()).readPhotoData("data_image.json")
        photos.forEach {
            appendPlace(it.place)
            photoAllData.add(it)
        }
        Log.d("Initial JSON Data", "$photos")
        val transformation = MultiTransformation(CenterCrop(), RoundedCorners(16))

        val gridLayout: GridLayout = view.findViewById(R.id.gridview)
        photoAllData.forEachIndexed { index, photoData ->
            appendPlace(photoData.place)
            val title = photoData.title
            val place = photoData.place
            val timestamp = photoData.timestamp
            val star = photoData.star
            val people = photoData.people
            val type = photoData.type
            val uri = photoData.uri
            val imageView = ImageView(context).apply {
                setOnClickListener {
                    showImageModal(uri, type, title, place, timestamp, star, people)
                }
//                background = ContextCompat.getDrawable(context, R.drawable.profile_image_rounded_corner)

                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // 초기 너비 0으로 설정
                    height = 0 // 초기 높이 0으로 설정
                    setMargins(8, 8, 8, 8)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    tag = index
//                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }
            if (type == "internal"){
                val imageId = context?.resources?.getIdentifier(uri, "drawable", requireContext().packageName)
                Log.d("Image Loader", "Image Uri from JSON: $imageId")
                Glide.with(this)
                    .load(imageId)
                    .apply(RequestOptions.bitmapTransform(transformation))
                    .into(imageView)
            } else if (type == "external"){
                val imageUri = Uri.parse(uri)
                Glide.with(this)
                    .load(imageUri)
                    .apply(RequestOptions.bitmapTransform(transformation))
                    .into(imageView)
            } else {
                error("Invalid Image Type")
            }
            gridLayout.addView(imageView)
        }
        gridLayout.requestLayout()
        adjustSquareImage(gridLayout, photoAllData.size)
    }

    private fun loadProfileData(){
        val context = context ?: return
        val jsonUtility = JsonUtility(context)

        try {
            val jsonData = jsonUtility.readJson("data_sample_user.json")
            val profileType: Type = object: TypeToken<List<Profile>>() {}.type
            val profiles = jsonUtility.parseJson<List<Profile>>(jsonData, profileType)

            profileList = profiles

        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                    val child = gridLayout.getChildAt(i)
                    if (child is ImageView){
                        val params = GridLayout.LayoutParams() as GridLayout.LayoutParams
                        params.width = cellSize
                        params.height = cellSize
                        params.setMargins(8, 8, 8, 8)
                        child.layoutParams = params
                        child.requestLayout()
                    }
                }
            }
        })
    }

    private fun showImageModal(uri: String, type: String, title: String, place: String, timestamp: Timestamp, star: Int, people: List<String>){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image) // 별도의 레이아웃 파일
        val modalImageView = dialog.findViewById<ImageView>(R.id.dialogImageView)
        val modalTextView = dialog.findViewById<TextView>(R.id.dialogTextView)
        val modalTitleTextView = dialog.findViewById<TextView>(R.id.dialogTitleTextView)
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

        modalTitleTextView.setText(title)
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
        dialog.setContentView(R.layout.image_input) // 별도의 레이아웃 파일
        val modalImageView = dialog.findViewById<ImageView>(R.id.capturedImageView)
//        val modalTextView = dialog.findViewById<TextView>(R.id.inputEditText)
        val inputEditText = dialog.findViewById<EditText>(R.id.inputEditText)
        val inputRatingBar = dialog.findViewById<RatingBar>(R.id.ratingBar)
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)
        val file = File(currentPhotoPath)
        Log.d("addImageModal", "photoUri : $photoUri")
        Glide.with(this)
            .load(file)
            .into(modalImageView)

        val peopleButton: Button = dialog.findViewById(R.id.peopleButton)
        peopleButton.setOnClickListener {
            showProfileDialog()
        }
        saveButton.setOnClickListener {
            val inputText = inputEditText.text.toString()
            val inputRating = inputRatingBar.rating.toInt()
            val peopleNames = selectedProfiles.map { it.name }
            Log.d("InputText", "Entered text: $inputText")

            val newPhotoData = PhotoData(
                title = inputText,
                place = "$currentCity, $currentCountry",
                timestamp = Timestamp(System.currentTimeMillis()),
                star = inputRating,
                people = peopleNames,
                type = "external",
                uri = photoUri.toString()
            )
            JsonUtility(requireContext()).appendPhotoJson("data_image.json", newPhotoData)
            refreshGridLayout()
            Log.d("refreshGridLayout", "refreshGridLayout")

            val photoDataList = JsonUtility(requireContext()).readPhotoData("data_image.json")
            Log.d("Updated JSON Data", photoDataList.toString())

            setupSpinner()
            dialog.dismiss() // Dialog 닫기
        }
        dialog.show()

    }
    private fun showProfileDialog() {
        val checkedItems = BooleanArray(profileList.size) { false }
        val selectedProfile: MutableList<Profile?> = MutableList(profileList.size) { null }
        val adapter = itemProfileAdapter(requireContext(), profileList, checkedItems, selectedProfile)

        AlertDialog.Builder(requireContext())
            .setTitle("Select People")
            .setAdapter(adapter, null)  // 여기서 커스텀 어댑터를 설정
            .setPositiveButton("OK") { dialog, _ ->
                selectedProfiles = selectedProfile.filterNotNull()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun refreshGridLayout() {
        val gridLayout: GridLayout = view?.findViewById(R.id.gridview) ?: return
        gridLayout.removeAllViews() // 기존의 모든 뷰 제거
        val transformation = MultiTransformation(CenterCrop(), RoundedCorners(16))
        val newPhotoDataList = JsonUtility(requireContext()).readPhotoData("data_image.json")
        newPhotoDataList.forEach { photoData ->
            appendPlace(photoData.place)

            val imageView = ImageView(context).apply {
                setOnClickListener {
                    showImageModal(photoData.uri, photoData.type, photoData.title, photoData.place, photoData.timestamp, photoData.star, photoData.people)
                }
//                background = ContextCompat.getDrawable(context, R.drawable.profile_image_rounded_corner)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = GridLayout.LayoutParams.WRAP_CONTENT
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    setMargins(8, 8, 8, 8)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
//                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }

            when (photoData.type) {
                "internal" -> {
                    val imageId = resources.getIdentifier(photoData.uri, "drawable", requireContext().packageName)
                    Glide.with(this)
                        .load(imageId)
                        .apply(RequestOptions.bitmapTransform(transformation))
                        .into(imageView)
                }
                "external" -> {
                    val file = File(photoData.uri)
                    Glide.with(this)
                        .load(file)
                        .apply(RequestOptions.bitmapTransform(transformation))
                        .into(imageView)
                }
            }
            if (selectedPlace == "모두" || selectedPlace == photoData.place){
                gridLayout.addView(imageView)
            }
        }
        gridLayout.requestLayout()
        adjustSquareImage(gridLayout, newPhotoDataList.size)
    }

    fun modalDataToText(place: String, timestamp: Timestamp, star: Int, people: List<String>): String {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분")
        val formattedDate = dateFormat.format(timestamp)
        val stars = "⭐".repeat(star)
        val peopleText = people.joinToString(", ")

        return "장소: $place\n\n시간: $formattedDate\n\n인물: $peopleText\n\n별점: $stars"
    }

    fun appendPlace(newPlace: String){
        if (placeList.isNullOrEmpty()) {
            placeList = mutableListOf(newPlace)
        } else if (newPlace !in placeList!!) {
            placeList!!.add(newPlace)
        }
        Log.d("appendPlace", "appendPlace: $placeList")
    }
    private fun setupSpinner() {
        val defaultItem = "모두"
        val placePlaceholder = listOf(defaultItem) + (placeList ?: return)

        val spinner: Spinner = requireView().findViewById(R.id.placeSpinner)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, placePlaceholder)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    selectedPlace = "모두"
                } else {
                    selectedPlace = (placeList ?: return)[position - 1]
                }
                refreshGridLayout()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때의 처리
                Log.d("is it", "is it")
            }
        }
    }

}

class itemProfileAdapter(
    context: Context,
    private val profiles: List<Profile>,
    private val checkedItems: BooleanArray,
    private val selectedProfile: MutableList<Profile?>
) : ArrayAdapter<Profile>(context, 0, profiles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.profile_image)
        val textView = view.findViewById<TextView>(R.id.profile_name)

        val profile = getItem(position)
        textView.text = profile?.name

        // 이미지 로딩 라이브러리 (예: Glide)를 사용하여 이미지 로드
        Glide.with(context)
            .load(profile?.image) // profile.image는 이미지 URL
            .into(imageView)

        val checkBox = view.findViewById<CheckBox>(R.id.profile_checkbox)
        checkBox.isChecked = checkedItems[position]

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkedItems[position] = isChecked
//            selectedProfile.add(profiles[position])
            selectedProfile[position] = if (isChecked) profiles[position] else null
        }
        return view
    }
}
