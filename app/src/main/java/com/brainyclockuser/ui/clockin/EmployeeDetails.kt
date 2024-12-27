package com.brainyclockuser.ui.clockin

import AllStaffShift
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.FragmentEmployeeDetailsBinding
import com.brainyclockuser.ui.shifts.AllShiftViewModel
import com.brainyclockuser.ui.shifts.SiteEmployeeModel
import com.brainyclockuser.ui.shifts.SiteEmployeeViewModel
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmployeeDetailsFragment : BaseFragment() {

    private lateinit var binding: FragmentEmployeeDetailsBinding

    private val viewModel: SiteEmployeeViewModel by viewModels()

    private val shiftViewModel: ShiftViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmployeeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loading.observe(viewLifecycleOwner) { if (it) showLoader() else hideLoader() }
        viewModel.throwable.observe(viewLifecycleOwner) {
            handleError(it)
        }
        viewModel.errorMsg.observe(viewLifecycleOwner) {
            showError(it)
        }
        shiftViewModel.callShiftApi()
        binding.textView4.text = ""
        shiftViewModel.shifts.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) {
                displaySiteDetails(it[0]);
                if(it[0].hasSiteEmployeePermission()) {
                    bootSiteEmployees()
                } else {
                    viewModel.errorMsg.value = "Please clock in"
                }
            } else {
                viewModel.errorMsg.value = "No shifts are found"
            }
        }

    }

    private fun displaySiteDetails(shift: ShiftsModels) {
        binding.textView4.text = "${shift.shiftId} ${shift.shiftName}"
    }

    private fun bootSiteEmployees() {
        viewModel.callSiteEmployeeApi()
        Log.e("TAG>>>", "callSiteEmployeeApi: " )


        viewModel.allEmployees.observe(viewLifecycleOwner) {
            setupViewPager()
        }
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        val allFragment = AllFragment()
        allFragment.list.value = allUsers()
        adapter.addFragment(allFragment, "All")
        val lateFragment = LateFragment()
        lateFragment.list.value = lateUsers()
//            .filter {
//            it.getStatus() == SiteEmployeeModel.STATUS_LATE
//        }
        adapter.addFragment(lateFragment, "Late")
        val absentFragment = AbsentFragment()
        absentFragment.list.value = absentUsers()
//            .filter {
//            it.getStatus() == SiteEmployeeModel.STATUS_ABSENT
//        }
        adapter.addFragment(absentFragment, "Absent")
        binding.viewpager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewpager)
    }

    private fun allUsers(): List<AllStaffShift> {
        return viewModel.allEmployees.value!!.filter { upcomingShift ->
            !upcomingShift.isAbsent()
        }
    }

    private fun absentUsers(): List<AllStaffShift> {
        return viewModel.allEmployees.value!!.filter { upcomingShift ->
            upcomingShift.isAbsent()
        }
//        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
//        return viewModel.allEmployees.value!!.filter { upcomingShift ->
//            val clockInDetails = upcomingShift.getClockInTimes()
//            val currentDate = clockInDetails.first ?: clockInDetails.second
//            if(currentDate != null) {
//                val differenceInMilliseconds = Date().time - currentDate.time
//                val differenceInMinutes = differenceInMilliseconds / (1000 * 60)
//                differenceInMinutes > AllStaffShift.ABSENT_BUFFER_IN_MIN
//            } else {
//                false
//            }
//        }
    }

    private fun lateUsers(): List<AllStaffShift> {

        return viewModel.allEmployees.value!!.filter { upcomingShift ->
            upcomingShift.isLate()
        }

//        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
//        return viewModel.allEmployees.value!!.filter { upcomingShift ->
//            val clockInDetails = upcomingShift.getClockInTimes()
//            val currentDate = clockInDetails.first ?: clockInDetails.second
//            if(currentDate != null) {
////                val startDate = dateFormatter.parse(currentDate)
//                val differenceInMilliseconds = Date().time - currentDate.time
//                val differenceInMinutes = differenceInMilliseconds / (1000 * 60)
//                differenceInMinutes > AllStaffShift.LATE_BUFFER_IN_MIN && differenceInMinutes < AllStaffShift.ABSENT_BUFFER_IN_MIN
//            } else {
//                false
//            }
//        }
    }


}