package com.brainyclockuser.ui.clockin

import AllStaffShift
import SimplePopupDialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.FragmentAllBinding
import com.brainyclockuser.databinding.PopupLoginBinding
import com.brainyclockuser.ui.shifts.SiteEmployeeAttendanceModel
import com.brainyclockuser.ui.shifts.SiteEmployeeModel
import com.brainyclockuser.ui.shifts.SiteEmployeeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllFragment : BaseFragment() {

    private lateinit var binding: FragmentAllBinding
    private lateinit var adapter: EmployeeAdapter

    val list: MutableLiveData<List<AllStaffShift>> = MutableLiveData()

    private val viewModel: SiteEmployeeViewModel by viewModels()

    private var currentEmployeeAttendanceModel = SiteEmployeeAttendanceModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllBinding.inflate(inflater, container, false)
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
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Pass true for isAllTabSelected to handle the specific functionality
        adapter =   EmployeeAdapter(
            getEmployees(),
            isAllTabSelected = true
        ) { context, employee, position, message ->
            showSimplePopup(context, employee, position, message)
        }
        adapter.attendance.observe(viewLifecycleOwner) {
            currentEmployeeAttendanceModel = it
            markAttendance(it)
        }
        viewModel.postAttendanceModel.observe(viewLifecycleOwner) {
            if(currentEmployeeAttendanceModel.arrayIndex != -1) {
                list.value!![currentEmployeeAttendanceModel.arrayIndex].clockIn = it.postClockInTime
                list.value!![currentEmployeeAttendanceModel.arrayIndex].clockOut = it.postClockOutTime
                adapter.notifyItemChanged(currentEmployeeAttendanceModel.arrayIndex, list.value!![currentEmployeeAttendanceModel.arrayIndex])
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun getEmployees(): List<AllStaffShift> {
        return list.value!!
    }

    private fun showSimplePopup(context: android.content.Context, employeeModel: AllStaffShift, position: Int, message: String) {
        val simplePopup = SimplePopupDialog(context)
        simplePopup.binding.employeeName.text = "${employeeModel.firstName} ${employeeModel.lastName}"
        val df = SimpleDateFormat("hh:mm a", Locale.getDefault())

        if(employeeModel.clockIn != null) {
            try {
                simplePopup.binding.clockinTime.text = df.format(
                    SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                    ).parse(employeeModel.clockIn)!!
                ).uppercase()
            } catch (e: Exception) {}
        }
        if(employeeModel.clockOut != null) {
            try {
                simplePopup.binding.clockoutTime.text = df.format(
                    SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                    ).parse(employeeModel.clockOut)!!
                ).uppercase()
            } catch (e: Exception) {}
        }
        if(employeeModel.getCustomStatus() == AllStaffShift.STATUS_PRESENT || employeeModel.getCustomStatus() == AllStaffShift.STATUS_LATE) {
            simplePopup.binding.clockoutButton.visibility = View.VISIBLE
            simplePopup.binding.clockoutButton.setOnClickListener {

                val formatter: SimpleDateFormat = SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()
                )

                var attendanceModel = SiteEmployeeAttendanceModel()
                attendanceModel.employeeId = employeeModel.employeeId
                attendanceModel.eventId = employeeModel.eventId
                attendanceModel.position = employeeModel.position
                attendanceModel.siteId = employeeModel.siteId
                attendanceModel.time = formatter.format(Date())
                attendanceModel.type = "timeOut"
                attendanceModel.arrayIndex = position
                attendanceModel.deptNumber = employeeModel.distNumber
                currentEmployeeAttendanceModel = attendanceModel
                markAttendance(attendanceModel)
                simplePopup.dismiss()
            }
        } else {
            simplePopup.binding.clockoutButton.visibility = View.GONE
        }
        simplePopup.show(message)
    }

    private fun markAttendance(attendanceModel: SiteEmployeeAttendanceModel) {
        viewModel.markAttendance(attendanceModel)
    }

}