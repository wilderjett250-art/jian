package com.example.jian2.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.example.jian2.worker.ReminderScheduler
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: DiaryViewModel by activityViewModels()

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            toast("未授予通知权限，无法发送提醒")
            view?.findViewById<SwitchMaterial>(R.id.swReminder)?.isChecked = false
            saveReminderEnabled(false)
            ReminderScheduler.cancel(requireContext())
        } else {
            saveReminderEnabled(true)
            ReminderScheduler.scheduleNext2230(requireContext())
            toast("已开启 22:30 提醒")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)
        val tvAuthor = view.findViewById<TextView>(R.id.tvAuthor)

        val swDark = view.findViewById<SwitchMaterial>(R.id.swDarkMode)
        val swReminder = view.findViewById<SwitchMaterial>(R.id.swReminder)

        val tvTotal = view.findViewById<TextView>(R.id.tvTotalCount)
        val tvMonth = view.findViewById<TextView>(R.id.tvMonthCount)
        val tvAvg = view.findViewById<TextView>(R.id.tvMonthAvgMood)
        val tvMonthDays = view.findViewById<TextView>(R.id.tvMonthDays)
        val tvStreak = view.findViewById<TextView>(R.id.tvStreak)

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

        // 作者信息
        tvAuthor.text = "开发者：孙亿豪  学号：202305100226"

        // 读取设置
        swDark.isChecked = getPrefs().getBoolean("dark_mode", false)
        swReminder.isChecked = getPrefs().getBoolean("reminder_enabled", false)

        // 进入就刷新统计
        viewModel.loadProfileStats()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileStats.collect { s ->
                tvTotal.text = "累计记录：${s.totalCount} 篇"
                tvMonth.text = "本月新增：${s.monthCount} 篇"
                tvAvg.text = "本月情绪均值：${s.monthAvgMood}"
                tvMonthDays.text = "本月记录天数：${s.monthDays} 天"
                tvStreak.text = "连续打卡：${s.streakDays} 天"
            }
        }

        btnRefresh.setOnClickListener {
            viewModel.loadProfileStats()
            toast("已刷新")
        }

        btnExport.setOnClickListener {
            viewModel.exportRecentAsText(20) { text: String ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "墨笺日记导出（最近20条）")
                    putExtra(Intent.EXTRA_TEXT, text) // ✅ text 明确是 String，不会歧义
                }
                startActivity(Intent.createChooser(intent, "分享导出内容"))
            }
        }

        swDark.setOnCheckedChangeListener { _, checked ->
            getPrefs().edit().putBoolean("dark_mode", checked).apply()
            requireActivity().recreate()
        }

        swReminder.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                if (Build.VERSION.SDK_INT >= 33) {
                    val granted = ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!granted) {
                        requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@setOnCheckedChangeListener
                    }
                }
                saveReminderEnabled(true)
                ReminderScheduler.scheduleNext2230(requireContext())
                toast("已开启 22:30 提醒")
            } else {
                saveReminderEnabled(false)
                ReminderScheduler.cancel(requireContext())
                toast("已关闭提醒")
            }
        }
    }

    private fun getPrefs() = requireContext().getSharedPreferences("mojian_prefs", 0)

    private fun saveReminderEnabled(enabled: Boolean) {
        getPrefs().edit().putBoolean("reminder_enabled", enabled).apply()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
