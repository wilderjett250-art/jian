package com.example.jian2


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.jian2.ui.DiaryListFragment
import com.example.jian2.ui.CalendarFragment
import com.example.jian2.ui.ProfileFragment



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // 默认页：日记
        if (savedInstanceState == null) {
            switchFragment(DiaryListFragment())
            bottomNav.selectedItemId = R.id.nav_diary
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_diary -> switchFragment(DiaryListFragment())
                R.id.nav_calendar -> switchFragment(CalendarFragment())
                R.id.nav_profile -> switchFragment(ProfileFragment())
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
