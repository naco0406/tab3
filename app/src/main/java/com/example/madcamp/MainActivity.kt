package com.example.madcamp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        viewPager.adapter = ViewPagerAdapter(this)

//        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
//            when (position){
//                0 -> {
//                    tab.text = "연락처"
//                    tab.icon = ContextCompat.getDrawable(this, R.drawable.baseline_contact_page_24)
//                }
//                1 -> {
//                    tab.text = "갤러리"
//                    tab.icon = ContextCompat.getDrawable(this, R.drawable.baseline_photo_library_24)
//                }
//                2 -> {
//                    tab.text = "404"
//                    tab.icon = ContextCompat.getDrawable(this, R.drawable.baseline_question_mark_24)
//                }
//            }
////            tab.text = "Tab ${position + 1}"
//        }.attach()
        // 커스텀 레이아웃을 사용하여 탭 설정
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // 커스텀 뷰를 인플레이트
            val tabView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null)
            val tabIcon = tabView.findViewById<ImageView>(R.id.tab_icon)
            val tabText = tabView.findViewById<TextView>(R.id.tab_text)

            when (position) {
                0 -> {
                    tabIcon.setImageResource(R.drawable.outline_contact_page_24)
                    tabText.text = "연락처"
                    tabText.setTextColor(ContextCompat.getColor(this, R.color.key))
                    tabIcon.setColorFilter(ContextCompat.getColor(this, R.color.key))
                    tabText.textSize = 16f
                    tabText.typeface = ResourcesCompat.getFont(this, R.font.gothic_a1_bold)

                }
                1 -> {
                    tabIcon.setImageResource(R.drawable.outline_photo_library_24)
                    tabText.text = "갤러리"
                    tabText.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
                    tabIcon.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray))
                }
                2 -> {
                    tabIcon.setImageResource(R.drawable.outline_edit_24)
                    tabText.text = "일기"
                    tabText.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
                    tabIcon.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray))
                }
            }
            // 탭에 커스텀 뷰를 설정
            tab.customView = tabView
        }.attach()

// 선택된 탭의 폰트 크기를 변경
        val typefaceMedium = ResourcesCompat.getFont(this, R.font.gothic_a1_medium)
        val typefaceBold = ResourcesCompat.getFont(this, R.font.gothic_a1_bold)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabText = tab.customView?.findViewById<TextView>(R.id.tab_text)
                val tabIcon = tab.customView?.findViewById<ImageView>(R.id.tab_icon)
                tabText?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.key))
                tabText?.typeface = typefaceBold
                tabIcon?.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.key))
                val animator = ValueAnimator.ofFloat(12f, 16f).apply {
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
                    }
                    duration = 200
                }
                animator.start()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabText = tab.customView?.findViewById<TextView>(R.id.tab_text)
                val tabIcon = tab.customView?.findViewById<ImageView>(R.id.tab_icon)
//                tabText?.textSize = 16f // 작은 폰트 크기
                tabText?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.dark_gray))
                tabText?.typeface = typefaceMedium
                tabIcon?.setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.dark_gray))
                val animator = ValueAnimator.ofFloat(16f, 12f).apply {
                    addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Float
                        tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
                    }
                    duration = 200
                }
                animator.start()
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // 재선택될 때 필요한 동작이 있으면 여기에 구현
            }
        })

    }
}
class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity){
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    val fragments = listOf<Fragment>(Tab1(), Tab2(), Tab3())

    override fun getItemCount(): Int {
        return fragments.size
    }
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}