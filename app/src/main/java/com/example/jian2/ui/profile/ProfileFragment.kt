package com.example.jian2.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: DiaryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)
        val tvAuthor = view.findViewById<TextView>(R.id.tvAuthor)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalCount)
        val tvMonth = view.findViewById<TextView>(R.id.tvMonthCount)
        val tvAvg = view.findViewById<TextView>(R.id.tvMonthAvgMood)

        val btnRefresh = view.findViewById<MaterialButton>(R.id.btnRefreshStats)
        val btnExport = view.findViewById<MaterialButton>(R.id.btnExportShare)

        // 版本信息
        val pkg = requireContext().packageManager
        val pName = requireContext().packageName
        val versionName = try {
            pkg.getPackageInfo(pName, 0).versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
        tvVersion.text = "版本：$versionName"


        tvAuthor.text = "作者：孙亿豪  学号：202305100226"

        // 进入就刷新一次统计
        viewModel.loadProfileStats()

        // 订阅统计数据流
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileStats.collect { s ->
                    tvTotal.text = "总日记数：${s.totalCount}"
                    tvMonth.text = "本月日记数：${s.monthCount}"

                    // 平均心情保留 1 位小数（你的 mood 若是 0~5，就显示 /5）
                    val avg1 = (s.monthAvgMood * 10.0).roundToInt() / 10.0
                    tvAvg.text = "本月平均心情：$avg1"
                }
            }
        }

        btnRefresh.setOnClickListener {
            viewModel.loadProfileStats()
        }

        btnExport.setOnClickListener {
            viewModel.exportRecentAsText(20) { text ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "日记导出（最近20条）")
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                startActivity(Intent.createChooser(intent, "分享导出内容"))
            }
        }
    }
}
