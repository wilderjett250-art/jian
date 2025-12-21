package com.example.jian2.ui.diary.write

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.jian2.R
import com.example.jian2.ui.diary.DiaryViewModel
import com.example.jian2.ui.diary.data.AppDatabase
import com.google.android.material.button.MaterialButton

class WriteDiaryFragment : Fragment() {

    private val viewModel: DiaryViewModel by activityViewModels {
        DiaryViewModel.Factory(AppDatabase.get(requireContext()).diaryDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_write_diary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etContent = view.findViewById<EditText>(R.id.etContent)
        val moodBar = view.findViewById<SeekBar>(R.id.seekMood)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            val mood = moodBar.progress

            viewModel.addDiary(title, content, mood)
            parentFragmentManager.popBackStack()
        }
    }
}
