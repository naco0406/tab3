package com.example.madcamp

import CustomDatePickerDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class CustomWeekDayFormatter : WeekDayFormatter {
    private val weekDayLabels = arrayOf("일", "월", "화", "수", "목", "금", "토")

    override fun format(dayOfWeek: Int): CharSequence {
        return weekDayLabels[dayOfWeek - 1]
    }
}

class Tab3 : Fragment(), CustomDatePickerDialog.DatePickerDialogListener {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
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

        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.setWeekDayFormatter(CustomWeekDayFormatter())
        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean {
                // 여기에 날짜를 확인하는 로직을 추가
                // 예시: 매월 1일에 데코레이터를 적용
                return day.day == 1
            }

            override fun decorate(view: DayViewFacade) {
                // 여기에 데코레이션 스타일을 추가
                // 예시: 날짜 아래에 점 추가
                view.addSpan(DotSpan(7f, Color.RED))
            }
        })

        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH) + 1
        val tvDate = view.findViewById<TextView>(R.id.calendarMonth)
        tvDate.text = "${month}월"
        val tvYear = view?.findViewById<TextView>(R.id.calendarYear)
        tvYear?.text = "${year}"

//        val spinnerDate = view.findViewById<Spinner>(R.id.spinnerDate)
//        val years = (Calendar.getInstance().get(Calendar.YEAR) downTo 2000).toList()
//        val months = (1..12).toList()
//        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years.map { year ->
//            months.map { month ->
//                "${year}년 ${month}월"
//            }
//        }.flatten())
//        spinnerDate.adapter = spinnerAdapter
//
//        spinnerDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                val selectedDate = parent.getItemAtPosition(position) as String
//                val year = selectedDate.substringBefore("년").toInt()
//                val month = selectedDate.substringAfter("년 ").substringBefore("월").toInt() - 1
//                val calendarDate = CalendarDay.from(year, month, 1)
//                calendarView.setCurrentDate(calendarDate)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }

        val buttonOpenDatePicker = view.findViewById<ImageButton>(R.id.buttonDatePicker)
        buttonOpenDatePicker.setOnClickListener {
            val datePickerDialogFragment = CustomDatePickerDialog()
            datePickerDialogFragment.setDatePickerDialogListener(this)
            datePickerDialogFragment.show(parentFragmentManager, "datePicker")
        }

        val bottomSheet = view.findViewById<LinearLayout>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // BottomSheetBehavior 설정
        bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height_month)
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.halfExpandedRatio = 0.75f
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // 필요한 경우 바텀 시트 보여주기
        showBottomSheet()

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        Log.d("bottomSheet", "Half Expanded")
                        calendarView.state().edit()
                            .setCalendarDisplayMode(CalendarMode.WEEKS)
                            .commit()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("bottomSheet", "Expanded")
                        calendarView.state().edit()
                            .setCalendarDisplayMode(CalendarMode.MONTHS)
                            .commit()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> Log.d("bottomSheet", "Collapsed")
                    // 기타 필요한 상태 처리
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 상태 변경시 처리
                calendarView.state().edit()
                    .setCalendarDisplayMode(CalendarMode.MONTHS)
                    .commit()
            }
        })
    }

    override fun onDateSelected(year: Int, month: Int) {
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