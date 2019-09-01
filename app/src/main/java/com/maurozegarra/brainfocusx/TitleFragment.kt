package com.maurozegarra.brainfocusx

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.maurozegarra.brainfocusx.databinding.FragmentTitleBinding
import java.text.SimpleDateFormat
import java.util.*

class TitleFragment : Fragment() {

    companion object {
        // These represent different important times
        // This is when the game is over
        const val DONE = 0L
        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L
        // This is the total time of the game
        const val COUNTDOWN_TIME = 10000L

        const val DAY = 1000L * 60L * 60L * 24L
        const val HOUR = 1000L * 60L * 60L
        const val ONE_MINUTE = 1000L * 60L
        const val POMO1 = 25
        const val POMO2 = 15
    }

    lateinit var timer: CountDownTimer
    var currentTime: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentTitleBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_title, container, false
        )

        binding.buttonWork.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_titleFragment_to_sessionFragment)
        )

        ////////////////////////////////////////////////////////////////////////////////////////////
        // setear el reloj a una hora específica \\
        // obtiene una instancia del Calendar
        var calendar = Calendar.getInstance()
        // fija el calendar a las 4 horas
        calendar.set(Calendar.HOUR, 5)
        // fija el calendar a los 35 minutos
        calendar.set(Calendar.MINUTE, 15)
        // fija el calendar a los 0 segundos
        calendar.set(Calendar.SECOND, 0)
        // obtiene el tiempo fijado en millis
        var futureTime = calendar.timeInMillis
        Log.i("onCreateView", "futureTime: $futureTime")

        var futureTimeFormatted = convertLongToDateString(futureTime)
        Log.i("onCreateView", "futureTimeFormatted: $futureTimeFormatted")

        currentTime = System.currentTimeMillis()
        var diffTime = futureTime - currentTime
        Log.i("onCreateView", "currentTime: $currentTime, futureTime: $futureTime")
        Log.i("onCreateView", "diffTime: $diffTime")

        timer = object : CountDownTimer(diffTime, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                currentTime = System.currentTimeMillis()
                Log.i("onCreateView: Timer", "diffTime: $diffTime")
                Log.i("onCreateView: Timer", "millisUntilFinished: $millisUntilFinished")

                // muestra el reloj del sistema
                var formattedTime = convertLongToDateString(currentTime)
                binding.textTime.text = formattedTime
                //Log.i("onCreateView", "system time: $formattedTime")

                // Muestra el Timer
                var minutes = SimpleDateFormat("m").format(currentTime).toInt()
                var seconds = SimpleDateFormat("s").format(currentTime).toInt()

                var timerMinutes = POMO2 - minutes - 1
                var timerSeconds: Int

                if (seconds == 0) {
                    timerSeconds = 0
                    timerMinutes += 1
                } else {
                    timerSeconds = 60 - seconds
                }

                binding.textTimer.text = String.format(
                    Locale.getDefault(), "%2d:%02d", timerMinutes, timerSeconds
                )
            }

            override fun onFinish() {
                currentTime = DONE
                // fix para que el tiempo mostrado sea el tiempo fijado
                // ejemplo tiempo fijado 4:55:00
                // antes del fix se muestra 4:54:59
                binding.textTime.text = convertLongToDateString(futureTime)
                // fix para que el timer no se quede en 0:01
                binding.textTimer.text = String.format(Locale.getDefault(), "%2d:%02d", 0, 0)
            }
        }

        timer.start()

        return binding.root
    }

    fun convertLongToDateString(systemTime: Long): String {
        return SimpleDateFormat("h:mm:ss").format(systemTime).toString()
    }

    fun formatTimer(timeInMillis: Long): String {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
