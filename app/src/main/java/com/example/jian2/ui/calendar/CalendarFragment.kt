package com.example.jian2.ui.calendar

import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.example.jian2.ui.diary.detail.DiaryDetailFragment
import com.example.jian2.ui.diary.list.DiaryListAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private val viewModel: DiaryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val tvSelectedDate = view.findViewById<TextView>(R.id.tvSelectedDate)
        val rv = view.findViewById<RecyclerView>(R.id.rvDayDiary)
        val empty = view.findViewById<LinearLayout>(R.id.emptyStateDay)

        val adapter = DiaryListAdapter { entity ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DiaryDetailFragment.newInstance(entity.id))
                .addToBackStack("diary_detail")
                .commit()
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        fun setSelected(dayStart: Long) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            tvSelectedDate.text = "选择日期：${sdf.format(Date(dayStart))}"
        }

        val todayStart = startOfDayMillis(System.currentTimeMillis())
        setSelected(todayStart)
        viewModel.loadDiariesForDay(todayStart)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = cal.timeInMillis
            setSelected(dayStart)
            viewModel.loadDiariesForDay(dayStart)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dayDiaries.collect { list ->
                adapter.submitList(list)
                val isEmpty = list.isEmpty()
                empty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rv.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }

    private fun startOfDayMillis(anyMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = anyMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
