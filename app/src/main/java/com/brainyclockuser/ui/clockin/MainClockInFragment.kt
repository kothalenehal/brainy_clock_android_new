package com.brainyclockuser.ui.clockin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseFragment

class MainClockInFragment:BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_clockin, container, false)
    }
}