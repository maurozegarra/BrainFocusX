package com.maurozegarra.brainfocusx

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.maurozegarra.brainfocusx.databinding.FragmentTitleBinding
import timber.log.Timber
import java.util.*

class TitleFragment : Fragment() {
    // todo: add type of interval: work or rest
    data class Interval(var start: Int, var end: Int)

    private val notificationId: Int = 1
    private val allIntervals = listOf(
        Interval(0, 25),
        Interval(25, 30),
        Interval(30, 55),
        Interval(55, 60)
    )

    private var currentInterval = allIntervals[0]

    companion object {
        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L
        const val DAY = 1000L * 60L * 60L * 24L
    }

    lateinit var timer: CountDownTimer

    // Contains all the views
    private lateinit var binding: FragmentTitleBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_title, container, false)

        binding.buttonWork.setOnClickListener {
            // 1.- Set the notification content
            val builder = NotificationCompat.Builder(requireContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle("Title")
                .setContentText("Message")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // 3.- Show the notification
            with(NotificationManagerCompat.from(requireContext())) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
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
                Timber.i("timerMinute: $timerMinute, currentInterval: $currentInterval, minute: $minute")

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

        return binding.root
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
}
