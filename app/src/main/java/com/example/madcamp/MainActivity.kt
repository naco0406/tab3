package com.example.madcamp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position){
                0 -> {
                    tab.text = "Contact"
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.baseline_contact_page_24)
                }
                1 -> {
                    tab.text = "Gallery"
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.baseline_photo_library_24)
                }
                2 -> {
                    tab.text = "404"
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.baseline_question_mark_24)
                }
            }
//            tab.text = "Tab ${position + 1}"
        }.attach()
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