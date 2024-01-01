import android.app.Dialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import com.example.madcamp.R

class CustomDatePickerDialog : DialogFragment() {
    // 클래스 구현
    private var listener: DatePickerDialogListener? = null

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }

    interface DatePickerDialogListener {
        fun onDateSelected(year: Int, month: Int)
    }

    fun setDatePickerDialogListener(listener: DatePickerDialogListener) {
        this.listener = listener
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_date_picker, null)

        val numberPickerYear = view.findViewById<NumberPicker>(R.id.numberPickerYear)
        val numberPickerMonth = view.findViewById<NumberPicker>(R.id.numberPickerMonth)

        // NumberPicker 설정
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        numberPickerYear.minValue = 2000
        numberPickerYear.maxValue = currentYear
        numberPickerYear.value = currentYear

        numberPickerMonth.minValue = 1
        numberPickerMonth.maxValue = 12
        numberPickerMonth.value = Calendar.getInstance().get(Calendar.MONTH) + 1


        builder.setView(view)
            .setPositiveButton("확인") { dialog, id ->
                listener?.onDateSelected(numberPickerYear.value, numberPickerMonth.value)
            }
            .setNegativeButton("취소") { dialog, id ->
                getDialog()?.cancel()
            }

        return builder.create()
    }
}
