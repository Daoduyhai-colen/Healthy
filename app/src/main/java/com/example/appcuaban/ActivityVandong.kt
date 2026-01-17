package com.example.appcuaban

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appcuaban.databinding.ActivityVandongBinding

class ActivityVandong : AppCompatActivity() {
    private lateinit var binding: ActivityVandongBinding
    private var currentMinutes = 30 

    private val activityData = mapOf(
        "Đi bộ nhẹ nhàng" to 3,
        "Đi bộ nhanh" to 5,
        "Chạy bộ chậm" to 8,
        "Chạy bộ nhanh" to 12,
        "Đạp xe thong thả" to 6,
        "Đạp xe nhanh" to 10,
        "Tập Gym (Tạ)" to 7,
        "Bơi lội" to 11,
        "Nhảy dây" to 15,
        "Yoga / Pilates" to 4,
        "Đá bóng" to 10,
        "Cầu lông" to 7,
        "Bóng rổ" to 9,
        "Leo cầu thang" to 13,
        "Làm việc nhà" to 3,
        "Nhảy Zumba" to 9
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVandongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshStats()

        val activities = activityData.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, activities)
        binding.spActivities.adapter = adapter

        binding.sbDuration.max = 120
        binding.sbDuration.progress = currentMinutes
        binding.tvDurationDisplay.text = "Thời gian: $currentMinutes phút"

        binding.sbDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentMinutes = progress
                binding.tvDurationDisplay.text = "Thời gian: $currentMinutes phút"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnSaveActivity.setOnClickListener {
            val selectedActivity = binding.spActivities.selectedItem.toString()
            val caloPerMin = activityData[selectedActivity] ?: 5
            val newBurned = currentMinutes * caloPerMin

            val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            
            val oldLevel = prefs.getString("level", "") ?: ""
            val oldCalo = prefs.getInt("total_burned_calo", 0)
            val oldMins = prefs.getInt("total_all_minutes", 0)

            val newLevel = if (oldLevel.isEmpty()) "• $selectedActivity: ${currentMinutes}p" 
                           else "$oldLevel\n• $selectedActivity: ${currentMinutes}p"
            
            prefs.edit().apply {
                putString("level", newLevel)
                putInt("total_burned_calo", oldCalo + newBurned)
                putInt("total_all_minutes", oldMins + currentMinutes)
                apply()
            }

            refreshStats()
            Toast.makeText(this, "Đã thêm $newBurned kcal!", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun refreshStats() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val level = prefs.getString("level", "") ?: ""
        val totalCalo = prefs.getInt("total_burned_calo", 0)
        val totalMins = prefs.getInt("total_all_minutes", 0)
        
        val itemCount = if (level.isEmpty()) 0 else level.trim().split("\n").size
        
        binding.tvTotalItems.text = itemCount.toString()
        binding.tvTotalBurned.text = totalCalo.toString()
        binding.tvTotalMins.text = totalMins.toString()

        
        val effortText = when {
            totalMins == 0 -> "Hôm nay bạn chưa vận động, cố lên!"
            totalMins < 30 -> "Khởi đầu tốt! Thêm chút nữa để đạt mốc 30p nhé."
            totalMins < 60 -> "Tuyệt vời! Bạn đang ở mức vận động lý tưởng. ✨"
            else -> "Chiến binh thực thụ! Bạn đã vượt mục tiêu hôm nay! 🔥"
        }
        binding.tvEffortStatus.text = effortText

        
        val healthTips = listOf(
            "Mẹo: Uống nước trước khi tập giúp tăng 15% hiệu suất.",
            "Mẹo: 15p nhảy dây đốt calo bằng 30p chạy bộ chậm.",
            "Mẹo: Khởi động kỹ giúp tránh chấn thương 80%.",
            "Mẹo: Tập buổi sáng giúp đốt mỡ thừa tốt hơn.",
            "Mẹo: Đừng quên giãn cơ sau khi tập để giảm đau mỏi."
        )
        binding.tvHealthTip.text = healthTips.random()
    }
}