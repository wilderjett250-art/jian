package com.example.jian2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.commit
import com.example.jian2.ui.calendar.CalendarFragment
import com.example.jian2.ui.diary.list.DiaryListFragment
import com.example.jian2.ui.profile.ProfileFragment
import com.example.jian2.worker.ReminderScheduler
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPrefs()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 若提醒开启，确保已排程
        ReminderScheduler.ensureScheduledIfEnabled(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, DiaryListFragment())
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_diary -> supportFragmentManager.commit {
                    replace(R.id.fragment_container, DiaryListFragment())
                }
                R.id.menu_calendar -> supportFragmentManager.commit {
                    replace(R.id.fragment_container, CalendarFragment())
                }
                R.id.menu_profile -> supportFragmentManager.commit {
                    replace(R.id.fragment_container, ProfileFragment())
                }
            }
            true
        }
    }

    private fun applyThemeFromPrefs() {
        val sp = getSharedPreferences("mojian_prefs", MODE_PRIVATE)
        val dark = sp.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
