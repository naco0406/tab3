package com.example.madcamp

import CustomDatePickerDialog
import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.io.File
import java.util.Date

class CustomWeekDayFormatter : WeekDayFormatter {
    private val weekDayLabels = arrayOf("일", "월", "화", "수", "목", "금", "토")

    override fun format(dayOfWeek: Int): CharSequence {
        return weekDayLabels[dayOfWeek - 1]
    }
}

class EventDecorator(private val dates: HashSet<CalendarDay>, private val color: Int) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(7f, color)) // 데코레이션 스타일 적용
    }
}
class Tab3 : Fragment(), CustomDatePickerDialog.DatePickerDialogListener, OnDateSelectedListener {

    private lateinit var onceSelectedDate: CalendarDay
    private var selectedDatePhotoNum: Int = 0
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var cards: List<PhotoData>
    private val cardViewPairs = mutableListOf<Pair<CardView, ImageView>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
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
        return inflater.inflate(R.layout.fragment_tab3, container, false)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val customScrollView: CustomScrollView = view.findViewById(R.id.customScrollView)
        bottomSheetBehavior = BottomSheetBehavior.from(customScrollView)

        cards = JsonUtility(requireContext()).readPhotoData("data_image.json")
        val dateSet = HashSet<CalendarDay>()
        cards.forEach { card ->
            val cardViewD = createCardView(card) // createCardView 함수는 이전 답변에서 정의
            val imageViewD = cardViewD.findViewById<ImageView>(R.id.cardImage)
            val time = card.timestamp
            val date = Date(time.time)
            dateSet.add(CalendarDay.from(date))
            cardViewPairs.add(Pair(cardViewD, imageViewD))
        }

        val todayButton: Button = view.findViewById(R.id.buttonDateToday)
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.setWeekDayFormatter(CustomWeekDayFormatter())
        calendarView.addDecorator(EventDecorator(dateSet, ContextCompat.getColor(requireContext(), R.color.key)))
        calendarView.setOnDateChangedListener(this)

        onceSelectedDate = CalendarDay.today()
        calendarView.setSelectedDate(onceSelectedDate)
        updateCardViewsForSelectedDate(onceSelectedDate)


        todayButton.setOnClickListener {
            val today = CalendarDay.today()
            calendarView.setCurrentDate(today)
            calendarView.setSelectedDate(today)
            updateCardViewsForSelectedDate(today)
        }

        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH) + 1
        val tvDate = view.findViewById<TextView>(R.id.calendarMonth)
        tvDate.text = "${month}월"
        val tvYear = view?.findViewById<TextView>(R.id.calendarYear)
        tvYear?.text = "${year}"

        val buttonOpenDatePicker = view.findViewById<ImageButton>(R.id.buttonDatePicker)
        buttonOpenDatePicker.setOnClickListener {
            val datePickerDialogFragment = CustomDatePickerDialog()
            datePickerDialogFragment.setDatePickerDialogListener(this)
            datePickerDialogFragment.show(parentFragmentManager, "datePicker")
        }
//
//        val bottomSheet = view.findViewById<LinearLayout>(R.id.bottom_sheet)
//        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        val bottomSheetLayout: LinearLayout = view.findViewById(R.id.bottom_sheet)
        addCardsAndSpaceToLayout(cards, bottomSheetLayout)



        // BottomSheetBehavior 설정
        bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height_month)
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.halfExpandedRatio = 0.75f
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        // 필요한 경우 바텀 시트 보여주기
        showBottomSheet()

        customScrollView.setOnScrollChangeListener(object : CustomScrollView.OnScrollChangeListener {
            override fun onScrollTopReached() {
                // 스크롤이 맨 위에 도달했을 때 수행할 작업
                bottomSheetBehavior.setDraggable(true)
            }
        })

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        Log.d("bottomSheet", "Half Expanded")
                        calendarView.state().edit()
                            .setCalendarDisplayMode(CalendarMode.WEEKS)
                            .commit()
                        toolbar.visibility = View.VISIBLE
                        customScrollView.setScrollingEnabled(true)
                        if (selectedDatePhotoNum > 1){
                            bottomSheetBehavior.setDraggable(false)
                        }
                        cardViewPairs.forEach { (cardView, imageView) ->
                            val originalHeight = 0
                            val width = imageView.width
                            val newHeight = originalHeight + width
                            val layoutParams = imageView.layoutParams
                            layoutParams.height = newHeight
                            imageView.layoutParams = layoutParams
                        }
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("bottomSheet", "Expanded")
//                        toolbar.visibility = View.GONE
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Log.d("bottomSheet", "Collapsed")
                        calendarView.state().edit()
                            .setCalendarDisplayMode(CalendarMode.MONTHS)
                            .commit()
                        toolbar.visibility = View.VISIBLE
                        customScrollView.setScrollingEnabled(true)
                    }
                    // 기타 필요한 상태 처리
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d("onSlide", "slideOffset: $slideOffset")
                if (slideOffset > 0.75f) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }

                cardViewPairs.forEach { (cardView, imageView) ->
                    val originalHeight = 0
                    val width = imageView.width

                    if (slideOffset in 0.0..0.6) {
                        val newHeight = (originalHeight + width * slideOffset / 0.6).toInt()
                        val layoutParams = imageView.layoutParams
                        layoutParams.height = newHeight
                        imageView.layoutParams = layoutParams
                    }
                }
            }

        })

    }

    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        // 날짜가 선택되었을 때의 로직
        onceSelectedDate = date
        if (selected) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            updateCardViewsForSelectedDate(date)
        } else {
            // 선택이 해제되었을 때의 로직 (예: 모든 카드 표시)
            onceSelectedDate = CalendarDay.today()
            showAllCards()
        }
    }

    private fun updateCardViewsForSelectedDate(selectedDate: CalendarDay) {
        val filteredCards = cards.filter { card ->
            val time = card.timestamp
            val date = Date(time.time)
            CalendarDay.from(date) == selectedDate
        }
        selectedDatePhotoNum = filteredCards.size
        val bottomSheetLayout: LinearLayout = view?.findViewById(R.id.bottom_sheet) ?: return
        addCardsAndSpaceToLayout(filteredCards, bottomSheetLayout)
    }

    private fun showAllCards() {
        val bottomSheetLayout: LinearLayout = view?.findViewById(R.id.bottom_sheet) ?: return
        selectedDatePhotoNum = cards.size
        addCardsAndSpaceToLayout(cards, bottomSheetLayout)
    }

    private fun createCardView(cardData: PhotoData): CardView {
        val cardView = CardView(requireContext())

        val cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
        cardView.radius = cornerRadius
        // 레이아웃 인플레이터를 사용하여 XML 레이아웃 로드
        val inflater = LayoutInflater.from(context)
        val cardContent = inflater.inflate(R.layout.photo_card, cardView, false)

        // XML 레이아웃에서 뷰 참조 가져오기
        val textViewTitle: TextView = cardContent.findViewById(R.id.textViewTitle)
        val textViewPlace: TextView = cardContent.findViewById(R.id.textViewPlace)
        val textViewTimestamp: TextView = cardContent.findViewById(R.id.textViewTimestamp)
        val imageView: ImageView = cardContent.findViewById(R.id.cardImage)

        // 데이터로 뷰 채우기
        val title = cardData.title
        val place = cardData.place
        val timestamp = cardData.timestamp
        val image = cardData.uri
        Log.d("Card Data Loaded", "title: $title, place: $place, timestamp: $timestamp")
        textViewTitle.text = title
        textViewPlace.text = place
        val timestampString = timestamp.toString().split(" ", ".")[1]
        textViewTimestamp.text = timestampString

        // 완성된 컨텐츠 뷰를 CardView에 추가
        cardView.addView(cardContent)

        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.MarginLayoutParams.MATCH_PARENT,
            ViewGroup.MarginLayoutParams.WRAP_CONTENT
        )
        val margin = resources.getDimension(R.dimen.card_margin).toInt() // 예: res/values/dimens.xml에 정의된 값
        layoutParams.setMargins(margin, margin, margin, margin)
        cardView.layoutParams = layoutParams

        return cardView
    }

    private fun addCardsAndSpaceToLayout(cardDataList: List<PhotoData>, linearLayout: LinearLayout) {
        linearLayout.removeAllViews()
        val barButton = ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                4.dp // 높이를 dp 단위로 설정

            ).also {
                it.setMargins(120.dp, 0.dp, 120.dp, 16.dp)
            }
            background = ContextCompat.getDrawable(context, R.drawable.black_bar)
        }
//        barButton.setOnClickListener(){
//            bottomSheetBehavior.setDraggable(true)
//        }
//        barButton.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    // 버튼이 눌렸을 때의 로직
//                    bottomSheetBehavior.setDraggable(true)
//                    true // 이벤트가 처리되었음을 나타냄
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    // 버튼에서 손가락이 떼어졌거나 취소되었을 때의 로직
//                    true // 이벤트가 처리되었음을 나타냄
//                }
//                else -> false // 이벤트가 처리되지 않았음을 나타냄
//            }
//        }

        linearLayout.addView(barButton)

        val transformation = MultiTransformation(RoundedCorners(16))
        // 모든 카드 데이터에 대해 반복하여 카드 뷰를 생성하고 LinearLayout에 추가
        cardDataList.forEach { cardData ->
            val cardView = createCardView(cardData)
            val imageView = cardView.findViewById<ImageView>(R.id.cardImage)
            val layoutParams = imageView.layoutParams

            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                // 이미지 뷰의 높이를 가로 길이와 동일하게 설정
                val width = imageView.width
                layoutParams.height = width
            } else {
                layoutParams.height = 0 // 또는 다른 높이 설정
            }
            imageView.layoutParams = layoutParams

            when (cardData.type) {
                "internal" -> {
                    val imageId = resources.getIdentifier(cardData.uri, "drawable", requireContext().packageName)
                    Glide.with(this)
                        .load(imageId)
                        .apply(RequestOptions.bitmapTransform(transformation))
                        .into(imageView)
                }
                "external" -> {
                    val file = File(cardData.uri)
                    Glide.with(this)
                        .load(file)
                        .apply(RequestOptions.bitmapTransform(transformation))
                        .into(imageView)
                }
            }
            cardViewPairs.add(Pair(cardView, imageView))
            linearLayout.addView(cardView)
        }

        val heightPerCard = 140
        val spaceHeight = cardDataList.size * heightPerCard.dp

        // 모든 카드가 추가된 후, 마지막에 Space 뷰 추가
        val space = Space(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeight // 높이를 dp 단위로 설정
            )
        }
        linearLayout.addView(space)
    }
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    override fun onToolbarDateSelected(year: Int, month: Int) {
        val calendarView = view?.findViewById<MaterialCalendarView>(R.id.calendarView)
        val calendarDate = CalendarDay.from(year, month - 1, 1)
        calendarView?.setCurrentDate(calendarDate)
        calendarView?.selectedDate = calendarDate

        val tvMonth = view?.findViewById<TextView>(R.id.calendarMonth)
        tvMonth?.text = "${month}월"
        val tvYear = view?.findViewById<TextView>(R.id.calendarYear)
        tvYear?.text = "${year}"
    }


    override fun onResume() {
        super.onResume()
        cards = JsonUtility(requireContext()).readPhotoData("data_image.json")
        cards.forEach {
            val cardViewD = createCardView(it) // createCardView 함수는 이전 답변에서 정의
            val imageViewD = cardViewD.findViewById<ImageView>(R.id.cardImage)
            cardViewPairs.add(Pair(cardViewD, imageViewD))
        }
        val bottomSheetLayout: LinearLayout = view?.findViewById(R.id.bottom_sheet) ?: return
        addCardsAndSpaceToLayout(cards, bottomSheetLayout)
        updateCardViewsForSelectedDate(onceSelectedDate)
    }
    override fun onPause() {
        super.onPause()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

}
