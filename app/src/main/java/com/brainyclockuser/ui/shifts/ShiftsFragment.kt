package com.brainyclockuser.ui.shifts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.FragmentShiftsBinding
import com.brainyclockuser.ui.clockin.ShiftsModels
import com.brainyclockuser.utils.AppConstant
import com.brainyclockuser.utils.OnItemClickListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ShiftsFragment : BaseFragment() {

    private lateinit var binding: FragmentShiftsBinding
    private val viewModel: AllShiftViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShiftsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.callShiftApi()

        val calendar = Calendar.getInstance()

        val df = SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.getDefault())
        val date = df.format(
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(
                "${calendar[Calendar.DAY_OF_MONTH]}" +
                        "-${calendar[Calendar.MONTH] + 1}-" +
                        "${calendar[Calendar.YEAR]}"
            )!!
        )

        binding.day = date.substring(0, date.indexOf(",") + 1)
        binding.date = date.substring(date.indexOf(",") + 1).trim()

        binding.weekCalendarView.backArrowButton.setOnClickListener {
            if (!binding.weekCalendarView.isShowingCurrentWeek) {
                binding.weekCalendarView.isShowingCurrentWeek = true
                binding.weekCalendarView.calendarHelper.moveToCurrentWeek()
                binding.weekCalendarView.updateDisplay()

                viewModel.todaysShifts.observe(viewLifecycleOwner) {
                    setupRecyclerViewToday(it)
                }
            }
        }

        binding.weekCalendarView.forwardArrowButton.setOnClickListener {
            if (binding.weekCalendarView.isShowingCurrentWeek) {
                binding.weekCalendarView.isShowingCurrentWeek = false
                binding.weekCalendarView.calendarHelper.moveToNextWeek()
                binding.weekCalendarView.updateDisplay()

                viewModel.nextShifts.observe(viewLifecycleOwner) {
                    setupRecyclerViewNext(it)
                }
            }
        }

        if (binding.weekCalendarView.isShowingCurrentWeek) {
            viewModel.todaysShifts.observe(viewLifecycleOwner) {
                setupRecyclerViewToday(it)
            }
        } else {
            viewModel.nextShifts.observe(viewLifecycleOwner) {
                setupRecyclerViewNext(it)
            }
        }

        viewModel.errorMsg.observe(viewLifecycleOwner) {
            showError(it)
        }
        binding.name = "${
            prefUtils.getStringData(
                requireActivity(),
                AppConstant.SharedPreferences.NAME
            )
        }, ${
            prefUtils.getStringData(
                requireActivity(),
                AppConstant.SharedPreferences.DEPARTMENTNAME
            )
        }"
        binding.id = "Employee ID: ${
            prefUtils.getIntData(
                requireActivity(),
                AppConstant.SharedPreferences.EMPLOYEE_ID
            )
        }"

        viewModel.loading.observe(viewLifecycleOwner) { if (it) showLoader() else hideLoader() }
        viewModel.throwable.observe(viewLifecycleOwner) { handleError(it) }
        setupHistory()
    }

    private fun setupRecyclerViewToday(shiftsModels: List<ShiftsModels>) {
        Log.e("TAG>>>", "todaysShifts : " + shiftsModels.size)
        binding.rvShifts.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvShifts.adapter = ShiftsAdapter(shiftsModels, true, object : OnItemClickListener {
            override fun onItemClick(position: Int) {
//                findNavController().navigate(ShiftsFragmentDirections.actionShiftsFragmentToShiftDetailFragment())
            }
        })
    }

    private fun setupRecyclerViewNext(shiftsModels: List<ShiftsModels>) {
        Log.e("TAG>>>", "nextShifts : " + shiftsModels.size)
        binding.rvShifts.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvShifts.adapter = ShiftsAdapter(shiftsModels, false, object : OnItemClickListener {
            override fun onItemClick(position: Int) {
//                findNavController().navigate(ShiftsFragmentDirections.actionShiftsFragmentToShiftDetailFragment())
            }

        })
    }

    private fun setupHistory() {
        binding.rvCopinant.layoutManager = LinearLayoutManager(requireActivity())

        viewModel.employeeHistory.observe(viewLifecycleOwner) { displayHistory(it) }

    }

    private fun displayHistory(employeeHistory: EmployeeHistoryModel<EmployeeHistoryDataModel>) {
        binding.clockedHours.text = "You have clocked in " + employeeHistory.totalWorkingHours + " hours during this week"
        binding.rvCopinant.adapter =
            HistoryAdapter(employeeHistory.data!!, object : OnItemClickListener {
                override fun onItemClick(position: Int) {
                }
            })
    }

}