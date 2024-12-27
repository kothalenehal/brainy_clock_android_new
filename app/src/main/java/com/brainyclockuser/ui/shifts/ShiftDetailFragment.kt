package com.brainyclockuser.ui.shifts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainyclockuser.databinding.FragmentShiftDetailBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ShiftDetailFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentShiftDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShiftDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.ivRight.setOnClickListener { findNavController().popBackStack() }
//        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {

        val list = ArrayList<String>()
        list.add("")
        list.add("")
        list.add("")
        binding.rvDays.layoutManager = LinearLayoutManager(requireActivity())
//        binding.rvDays.adapter = ShiftsDaysAdapter(list, "time", true)

        binding.rvAssignee.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvAssignee.adapter = ShiftsAssigneeAdapter(list)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShiftDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}