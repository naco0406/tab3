package com.example.madcamp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class CustomScrollView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {
    private var isScrollable: Boolean = false
    interface OnScrollChangeListener {
        fun onScrollTopReached()
        // 필요하다면 여기에 다른 메서드 추가
    }

    private var onScrollChangeListener: OnScrollChangeListener? = null

    fun setOnScrollChangeListener(listener: OnScrollChangeListener) {
        this.onScrollChangeListener = listener
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        // 스크롤이 맨 위에 도달했는지 확인
        if (t == 0) {
            onScrollChangeListener?.onScrollTopReached()
        }
    }

    fun setScrollingEnabled(enabled: Boolean) {
        isScrollable = enabled
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (isScrollable) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if (isScrollable) {
            super.onTouchEvent(ev)
        } else {
            false
        }
    }
}
