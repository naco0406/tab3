package com.example.madcamp

import CustomDatePickerDialog
import android.app.DatePickerDialog
import android.graphics.Color
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

class EventDecorator(private val dates: HashSet<CalendarDay>) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(7f, Color.RED)) // 데코레이션 스타일 적용
    }
}
class Tab3 : Fragment(), CustomDatePickerDialog.DatePickerDialogListener, OnDateSelectedListener {

    private lateinit var onceSelectedDate: CalendarDay
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var cards: List<PhotoData>
    private val cardViewPairs = mutableListOf<Pair<CardView, ImageView>>()
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


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // CardView 참조를 저장할 리스트

        cards = JsonUtility(requireContext()).readPhotoData("data_image.json")
        val dateSet = HashSet<CalendarDay>()
        cards.forEach { card ->
            val time = card.timestamp
            val date = Date(time.time)
            dateSet.add(CalendarDay.from(date))
        }

        val todayButton: Button = view.findViewById(R.id.buttonDateToday)
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.setWeekDayFormatter(CustomWeekDayFormatter())
        calendarView.addDecorator(EventDecorator(dateSet))
        calendarView.setOnDateChangedListener(this)

        onceSelectedDate = CalendarDay.today()
        calendarView.setSelectedDate(onceSelectedDate)
        updateCardViewsForSelectedDate(onceSelectedDate)


        todayButton.setOnClickListener {
            calendarView.setSelectedDate(CalendarDay.today())
            updateCardViewsForSelectedDate(CalendarDay.today())
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

        val customScrollView: CustomScrollView = view.findViewById(R.id.customScrollView)
        val bottomSheetLayout: LinearLayout = view.findViewById(R.id.bottom_sheet)
        addCardsAndSpaceToLayout(cards, bottomSheetLayout)

//        val bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById<CustomScrollView>(R.id.customScrollView))
        bottomSheetBehavior = BottomSheetBehavior.from(customScrollView)

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
                        bottomSheetBehavior.setDraggable(false)
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
                // 슬라이드 상태 변경시 처리
//                calendarView.state().edit()
//                    .setCalendarDisplayMode(CalendarMode.MONTHS)
//                    .commit()
                customScrollView.setScrollingEnabled(false)
                if (slideOffset > 0.75f){
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
                val newWidth = 100.dp * (1 + slideOffset * 2)
                cardViewPairs.forEach { (cardView, imageView) ->
                    // 여기서 각 CardView의 크기를 조정하는 로직을 구현
                    val layoutParams = cardView.layoutParams
                    layoutParams.height = newWidth.toInt()
                    cardView.layoutParams = layoutParams
                    imageView.alpha = 1 - slideOffset
                }
            }
        })

    }

    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        // 날짜가 선택되었을 때의 로직
        onceSelectedDate = date
        if (selected) {
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
        val bottomSheetLayout: LinearLayout = view?.findViewById(R.id.bottom_sheet) ?: return
        addCardsAndSpaceToLayout(filteredCards, bottomSheetLayout)
    }

    private fun showAllCards() {
        val bottomSheetLayout: LinearLayout = view?.findViewById(R.id.bottom_sheet) ?: return
        addCardsAndSpaceToLayout(cards, bottomSheetLayout)
    }
//
//    private fun displayCards(cardsToShow: List<PhotoData>) {
//        // CardView들을 동적으로 추가하거나 업데이트하는 로직
//        // 예: cardsToShow에 있는 데이터를 사용하여 카드를 생성하고 화면에 표시
//    }
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
        textViewTimestamp.text = timestamp.toString()

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
//        barButton.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    // 버튼이 눌렸을 때의 로직
//                    bottomSheetBehavior.setDraggable(true)
//                    Log.d("Bar Button", "Clicked")
//                    true // 이벤트가 처리되었음을 나타냄
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    // 버튼에서 손가락이 떼어졌거나 취소되었을 때의 로직
//                    bottomSheetBehavior.setDraggable(false)
//                    Log.d("Bar Button", "Clicked Cancled")
//                    true // 이벤트가 처리되었음을 나타냄
//                }
//                else -> {
//                    Log.d("Bar Button", "Else")
//                    false
//                } // 이벤트가 처리되지 않았음을 나타냄
//            }
//        }

        linearLayout.addView(barButton)

        val transformation = MultiTransformation(CenterCrop(), RoundedCorners(16))
        // 모든 카드 데이터에 대해 반복하여 카드 뷰를 생성하고 LinearLayout에 추가
        cardDataList.forEach { cardData ->
            val cardViewD = createCardView(cardData) // createCardView 함수는 이전 답변에서 정의
            val imageViewD = cardViewD.findViewById<ImageView>(R.id.cardImage)
            when (cardData.type) {
                "internal" -> {
                    val imageId = resources.getIdentifier(cardData.uri, "drawable", requireContext().packageName)
                    Glide.with(this)
                        .load(imageId)
                        .apply(RequestOptions.bitmapTransform(transformation))
                        .into(imageViewD)
                }
                "external" -> {
                    val file = File(cardData.uri)
                    Glide.with(this)
                        .load(file)
                        .apply(RequestOptions.bitmapTransform(transformation))
                        .into(imageViewD)
                }
            }
            cardViewPairs.add(Pair(cardViewD, imageViewD))
            linearLayout.addView(cardViewD)
        }

        val heightPerCard = 32
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

        val bottomSheetLayout: LinearLayout = view?.findViewById(R.id.bottom_sheet) ?: return
        addCardsAndSpaceToLayout(cards, bottomSheetLayout)
        updateCardViewsForSelectedDate(onceSelectedDate)
    }
    override fun onPause() {
        super.onPause()
    }

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

}
