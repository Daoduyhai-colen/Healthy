package com.example.appcuaban

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appcuaban.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var water = 0
    private var waterGoal = 2000 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        water = prefs.getInt("water", 0)
        waterGoal = prefs.getInt("water_goal", 2000)
        
       
        updateWaterUI()
        updateGreeting()
        
        
        scheduleWaterAlarm()

       
        binding.btnUpdateGoal.setOnClickListener {
            val weight = binding.etWeight.text.toString().toFloatOrNull()
            val age = binding.etAge.text.toString().toIntOrNull()

            if (weight != null && age != null) {
                
                waterGoal = (weight * 0.033f * 1000).toInt()
                val newCaloGoal = if (age < 30) 600 else 400

                prefs.edit().apply {
                    putInt("water_goal", waterGoal)
                    putInt("calo_goal", newCaloGoal)
                    putInt("user_age", age)
                    apply()
                }
                updateWaterUI()
                updateGreeting()
                vibratePhone(100)
                Toast.makeText(this, "Đã cập nhật mục tiêu riêng cho bạn!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng nhập đủ Cân nặng và Tuổi ở các ô dưới!", Toast.LENGTH_SHORT).show()
            }
        }

        
        binding.btnAddWater.setOnClickListener {
            if (water < waterGoal) {
                water += 250
                prefs.edit().putInt("water", water).apply()
                updateWaterUI()
                
                if (water >= waterGoal) {
                    
                    val pattern = longArrayOf(0, 100, 100, 500)
                    val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        vibrator.vibrate(pattern, -1)
                    }
                    Toast.makeText(this, "TUYỆT VỜI! ĐẠT MỤC TIÊU NƯỚC! 🎉", Toast.LENGTH_LONG).show()
                } else {
                    vibratePhone(50)
                }
            }
        }

        
        binding.btnGoToVandong.setOnClickListener {
            startActivity(Intent(this, ActivityVandong::class.java))
        }

       
        binding.btnBmi.setOnClickListener {
            val w = binding.etWeight.text.toString().toFloatOrNull()
            val h = binding.etHeight.text.toString().toFloatOrNull()
            if (w != null && h != null && h > 0) {
                val bmi = w / ((h / 100) * (h / 100))
                val color = if (bmi in 18.5..24.9) Color.parseColor("#4CAF50") else Color.RED
                binding.tvBmiResult.text = "BMI: %.1f".format(bmi)
                binding.tvBmiResult.setTextColor(color)
            }
        }

        // 5. Reset dữ liệu
        binding.btnReset.setOnClickListener {
            prefs.edit().clear().apply()
            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        updateGreeting() 
        updateWaterUI()
    }

    

    private fun updateWaterUI() {
        binding.tvWater.text = "Nước: $water / $waterGoal ml"
        binding.pbWater.max = waterGoal
        binding.pbWater.progress = water

        
        val percent = if (waterGoal > 0) (water * 100 / waterGoal) else 0
        val color = when {
            percent < 50 -> Color.parseColor("#FFA500") // Cam
            percent < 100 -> Color.parseColor("#2196F3") // Xanh dương
            else -> Color.parseColor("#4CAF50") // Xanh lá
        }
        binding.pbWater.progressTintList = ColorStateList.valueOf(color)
    }

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..10 -> "Chào buổi sáng! Năng lượng mới nhé ☀️"
            in 11..16 -> "Chào buổi chiều! Cố gắng lên nào 🏃"
            in 17..21 -> "Chào buổi tối! Kiểm tra mục tiêu nhé 🌙"
            else -> "Ngủ ngon nhé! Mai lại cố gắng nha 😴"
        }
        
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val burnedCalo = prefs.getInt("total_burned_calo", 0)
        val caloGoal = prefs.getInt("calo_goal", 500)
        val level = prefs.getString("level", "Hôm nay chưa vận động")
        
        binding.tvTip.text = "$greeting\n🔥 TIÊU THỤ: $burnedCalo / $caloGoal kcal\n\n$level"
        binding.tvGoalInfo.text = "Mục tiêu: ${waterGoal}ml nước | ${caloGoal}kcal vận động"
    }

    private fun scheduleWaterAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DrinkWaterReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.MINUTE) > 0) calendar.add(Calendar.HOUR_OF_DAY, 1)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_HOUR,
            pendingIntent
        )
    }

    private fun vibratePhone(duration: Long) {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }
}