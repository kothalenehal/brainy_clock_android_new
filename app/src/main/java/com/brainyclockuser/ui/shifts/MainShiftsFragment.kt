package com.brainyclockuser.ui.shifts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.brainyclockuser.R
import com.brainyclockuser.databinding.FragmentShiftsBinding


private var _binding: FragmentShiftsBinding? = null
private val binding get() = _binding!!

class MainShiftsFragment : Fragment(R.layout.fragment_main_shifts) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}