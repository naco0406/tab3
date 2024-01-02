import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    var isPagingEnabled = true

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (isPagingEnabled) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }
}
