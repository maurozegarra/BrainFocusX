package com.maurozegarra.brainfocusx

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.maurozegarra.brainfocusx.databinding.FragmentSessionBinding

/**
 * A simple [Fragment] subclass.
 */
class SessionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentSessionBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_session, container, false
        )
        return binding.root
    }

}
