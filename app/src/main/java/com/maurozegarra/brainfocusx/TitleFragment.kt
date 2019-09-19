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
import java.text.DateFormat
import java.util.*

class TitleFragment : Fragment() {
    // todo: add type of interval: work or rest

    data class Interval(var start: Int, var end: Int)

    private val allIntervals = listOf(
        Interval(0, 25),
        Interval(25, 30),
        Interval(30, 55),
        Interval(55, 60)
    )

    private var currentInterval = allIntervals[0]

    private var isWorking = false

    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val TARGET_TIME = "targetTime"
        const val IS_WORKING = "isWorking"

        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L
        const val DAY = 1000L * 60L * 60L * 24L
    }

    lateinit var timer: CountDownTimer
    lateinit var alarmManager: AlarmManager

    // Contains all the views
    private lateinit var binding: FragmentTitleBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_title, container, false)

        binding.buttonWork.setOnClickListener {
            onStartWork()
        }

        binding.buttonBreak.setOnClickListener {
            cancelAlarm()
        }

        timer = object : CountDownTimer(DAY, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                // Muestra el reloj del sistema
                val now = Calendar.getInstance()

                var hour = now.get(Calendar.HOUR)
                if (hour == 0) {
                    hour = 12
                }

                val minute = now.get(Calendar.MINUTE)
                //val minute = 0
                val second = now.get(Calendar.SECOND)

                binding.textTime.text =
                    String.format(Locale.getDefault(), "%2d:%02d:%02d", hour, minute, second)

                // Muestra el Timer
                getCurrentStartInterval(minute)
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

    private fun onStartWork() {
        // todo: create alarms
        val timeToAlarm = Calendar.getInstance()

        val hour = timeToAlarm.get(Calendar.HOUR)
        val minute = timeToAlarm.get(Calendar.MINUTE)

        getCurrentStartInterval(minute)

        timeToAlarm.set(Calendar.HOUR, hour)
        timeToAlarm.set(Calendar.MINUTE, currentInterval.end)
        timeToAlarm.set(Calendar.SECOND, 0)
//        Timber.i("hour: $hour, currentInterval.end: ${currentInterval.end}")

        updateTimeText(timeToAlarm)
        scheduleAlarm(timeToAlarm)
        isWorking = true
        updateButtons()
    }

    private fun scheduleAlarm(calendar: Calendar) {
        alarmManager = activity?.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm() {
        alarmManager = activity?.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0)

        alarmManager.cancel(pendingIntent)
        binding.textTarget.text = getString(R.string.no_alarm_set)
        isWorking = false

        updateButtons()
    }

    private fun updateButtons() {
        if (isWorking) {
            binding.buttonWork.visibility = View.INVISIBLE
            binding.buttonBreak.visibility = View.VISIBLE
        } else {
            binding.buttonWork.visibility = View.VISIBLE
            binding.buttonBreak.visibility = View.INVISIBLE
        }
    }

    private fun updateTimeText(calendar: Calendar) {
        var timeText = "Focus until: "
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.time)
        binding.textTarget.text = timeText
    }

    private fun getCurrentStartInterval(currentMinute: Int) {
        var newInterval = allIntervals[0]

        for (interval in allIntervals) {
            //Timber.i("${interval.start}, ${interval.end}")
            if (interval.start <= currentMinute && currentMinute < interval.end) {
                //Timber.i("$interval")
                newInterval = interval
                break
            }
        }

        // If the new start interval is actually different than the current start interval
        if (newInterval != currentInterval) {
            currentInterval = newInterval
            //binding.dessertButton.setImageResource(newDessert.imageId)
        }
        //Timber.i("currentInterval: $currentInterval")
    }

    override fun onStop() {
        super.onStop()
        saveData()
    }

    private fun saveData() {
        val preference = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)

        with (preference?.edit()) {
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
