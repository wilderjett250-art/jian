package com.example.jian2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
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
            supportFragmentManager.commit {
                replace(R.id.fragment_container, DiaryListFragment())
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_diary -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, DiaryListFragment())
                    }
                    true
                }
                R.id.menu_calendar -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, CalendarFragment())
                    }
                    true
                }
                R.id.menu_profile -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, ProfileFragment())
                    }
                    true
                }
                else -> false
            }
        }
    }
}
