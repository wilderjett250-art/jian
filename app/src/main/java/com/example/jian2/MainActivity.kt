package com.example.jian2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.jian2.ui.calendar.CalendarFragment
import com.example.jian2.ui.diary.list.DiaryListFragment
import com.example.jian2.ui.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            switchFragment(DiaryListFragment())
            bottomNav.selectedItemId = R.id.menu_diary
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_diary -> switchFragment(DiaryListFragment())
                R.id.menu_calendar -> switchFragment(CalendarFragment())
                R.id.menu_profile -> switchFragment(ProfileFragment())
            }
            true
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
