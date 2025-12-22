package com.example.jian2.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class CalendarFragment : Fragment() {

    private val viewModel: DiaryViewModel by activityViewModels()

    private lateinit var calendarView: CalendarView
    private lateinit var tvSelectedDate: TextView
    private lateinit var rvDayDiary: RecyclerView
    private lateinit var emptyState: LinearLayout

    private val adapter = DiaryListAdapter { entity ->
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DiaryDetailFragment.newInstance(entity.id))
            .addToBackStack("diary_detail")
            .commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        rvDayDiary = view.findViewById(R.id.rvDayDiary)
        emptyState = view.findViewById(R.id.emptyStateDay)

        rvDayDiary.layoutManager = LinearLayoutManager(requireContext())
        rvDayDiary.adapter = adapter

        // 默认：今天
        val todayStart = startOfDayMillis(System.currentTimeMillis())
        setSelectedDateText(todayStart)
        viewModel.loadDiariesForDay(todayStart)

        // 监听日期点击
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month) // month: 0-11
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = cal.timeInMillis
            setSelectedDateText(dayStart)
            viewModel.loadDiariesForDay(dayStart)
        }

        // 订阅当天列表
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dayDiaries.collect { list ->
                adapter.submitList(list)
                val isEmpty = list.isEmpty()
                emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvDayDiary.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }

    private fun setSelectedDateText(dayStartMillis: Long) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvSelectedDate.text = "选择日期：${sdf.format(Date(dayStartMillis))}"
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
