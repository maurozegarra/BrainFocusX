package com.maurozegarra.brainfocusx

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.maurozegarra.brainfocusx.databinding.FragmentTitleBinding
import timber.log.Timber
import java.util.*

class TitleFragment : Fragment() {
    companion object {
        const val WORK_DURATION = 25
        const val BREAK_DURATION = 5
        const val IS_LONG_BREAK_ENABLED = true
        const val LONG_BREAK_DURATION = 35
        const val WORK_SESSIONS_BEFORE_LONG_BREAK = 4
        // los recordatorios se gatillan a las 2:55, 3:00, 3:25, 3:30, etc
        var IS_FIXED_MODE_ENABLED = false

        const val SHARED_PREFS = "sharedPrefs"
        const val TARGET_TIME = "targetTime"
        const val IS_WORKING = "isWorking"

        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L
        const val DAY = 1000L * 60L * 60L * 24L
    }

    private lateinit var timer: CountDownTimer
    private lateinit var alarmManager: AlarmManager
    private lateinit var binding: FragmentTitleBinding

    // todo: al entrar a la aplicación, la notificación pendiente debería descartarse(dismiss)
    // todo: add type of interval: work or rest
    data class Interval(var start: Int, var end: Int)

    private val intervals = listOf(
        Interval(0, 25),
        Interval(25, 30),
        Interval(30, 55),
        Interval(55, 60)
    )

    data class Reminder(val hour: Int, val minute: Int)

    private val reminders = listOf(
        Reminder(2, 0),
        Reminder(2, 25),
        Reminder(2, 30),
        Reminder(2, 55),
        Reminder(3, 0)
    )

    private var currentInterval = intervals[0]
    private var isWorking = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_title, container, false)

        binding.buttonWork.setOnClickListener { onStartWork() }
        binding.buttonBreak.setOnClickListener { cancelAlarm() }
        binding.buttonAutoFixed.setOnClickListener { setMode() }


        timer = object : CountDownTimer(DAY, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                // Muestra el reloj del sistema
                val now = Calendar.getInstance()

                var hour = now.get(Calendar.HOUR)
                //Timber.i("HOUR: ${now.get(Calendar.HOUR)}")
                if (hour == 0) {
                    hour = 12
                }

                val minute = now.get(Calendar.MINUTE)
                val second = now.get(Calendar.SECOND)

                binding.textTime.text =
                    String.format(Locale.getDefault(), "%2d:%02d:%02d", hour, minute, second)

                // Muestra el Timer
                getCurrentInterval(minute)
                var timerMinute = currentInterval.end - minute - 1
                //Timber.i("timerMinute: $timerMinute, currentInterval: $currentInterval, minute: $minute")

                val timerSecond: Int

                if (second == 0) {
                    timerSecond = 0
                    timerMinute += 1
                } else {
                    timerSecond = 60 - second
                }

                binding.textTimer.text =
                    String.format(Locale.getDefault(), "%2d:%02d", timerMinute, timerSecond)
            }

            override fun onFinish() {}
        }

        timer.start()

        loadData()
        updateButtons()

        return binding.root
    }

    private fun setMode() {
        when (binding.buttonAutoFixed.text.toString().toUpperCase(Locale.getDefault())) {
            "AUTO" -> {
                binding.buttonAutoFixed.text = "FIXED"
                IS_FIXED_MODE_ENABLED = true
            }
            "FIXED" -> {
                binding.buttonAutoFixed.text = "AUTO"
                IS_FIXED_MODE_ENABLED = false
            }
            else -> "UNKNOWN"
        }
        Timber.i("IS_FIXED_MODE_ENABLED $IS_FIXED_MODE_ENABLED")
    }

    private fun onStartWork() {
        if (IS_FIXED_MODE_ENABLED) {

        }
        createRemindersData()
        scheduleReminders()

        isWorking = true
        updateButtons()
    }

    private fun createRemindersData() {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR)
        val minute = now.get(Calendar.MINUTE)

        getCurrentInterval(minute)

    }

    private fun scheduleReminders() {
        alarmManager = activity?.getSystemService(ALARM_SERVICE) as AlarmManager

        for (reminder in reminders) {
            val alarmIntent = createPendingIntent()
            scheduleAlarm(reminder, alarmIntent, alarmManager)
        }
    }

    private fun createPendingIntent(): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(context, 1, intent, 0)
    }

    private fun scheduleAlarm(
        reminder: Reminder,
        pendingIntent: PendingIntent?,
        alarmManager: AlarmManager
    ) {
        val timeToAlarm = Calendar.getInstance()

        val hour = timeToAlarm.get(Calendar.HOUR)
        val minute = timeToAlarm.get(Calendar.MINUTE)

        getCurrentInterval(minute)

        timeToAlarm.set(Calendar.HOUR, hour)
        timeToAlarm.set(Calendar.MINUTE, currentInterval.end)
        timeToAlarm.set(Calendar.SECOND, 0)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeToAlarm.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm() {
        alarmManager = activity?.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0)
        alarmManager.cancel(pendingIntent)

        isWorking = false
        updateButtons()
    }

    private fun updateButtons() {
        if (isWorking) {
            binding.buttonWork.visibility = View.INVISIBLE
            binding.buttonBreak.visibility = View.VISIBLE

            var timeText = "Focus until: "
            //timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(nextAlarm.time)
            binding.textTarget.text = timeText
        } else {
            binding.buttonWork.visibility = View.VISIBLE
            binding.buttonBreak.visibility = View.INVISIBLE
            binding.textTarget.text = getString(R.string.no_alarm_set)
        }
    }

    private fun getCurrentInterval(currentMinute: Int) {
        var newInterval = intervals[0]

        for (interval in intervals) {
            if (interval.start <= currentMinute && currentMinute < interval.end) {
                newInterval = interval
                break
            }
        }

        // If the new start interval is actually different than the current start interval
        if (newInterval != currentInterval) {
            currentInterval = newInterval
            //binding.dessertButton.setImageResource(newDessert.imageId)
        }
    }

    override fun onStop() {
        super.onStop()
        saveData()
    }

    private fun saveData() {
        val preference = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)

        with(preference?.edit()) {
            this?.putString(TARGET_TIME, binding.textTarget.text.toString())
            this?.putBoolean(IS_WORKING, isWorking)
            this?.apply()
        }
    }

    private fun loadData() {
        val preference = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)

        binding.textTarget.text = preference?.getString(TARGET_TIME, "")
        isWorking = preference!!.getBoolean(IS_WORKING, false)
    }
}
