package com.brainyclockuser.ui.clockin

import AllStaffShift
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.brainyclockuser.databinding.EmployeeDetailBinding
import com.brainyclockuser.ui.shifts.SiteEmployeeAttendanceModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EmployeeAdapter(
    private val employees: List<AllStaffShift>,
    private val isAllTabSelected: Boolean,
    private val onShowPopup: (Context, AllStaffShift, Int, String) -> Unit
) : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    val formatter: SimpleDateFormat = SimpleDateFormat(
        "HH:mm:ss",
        Locale.getDefault()
    )

    var attendance: MutableLiveData<SiteEmployeeAttendanceModel> = MutableLiveData()

    class EmployeeViewHolder(val binding: EmployeeDetailBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val binding = EmployeeDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmployeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = employees[position]
        holder.binding.apply {
            employeeName.text = employee.firstName+ " " +employee.lastName
//            val status = AllStaffShift.STATUS_INITIAL
            val status = employee.getCustomStatus()

            button1.visibility = if(status == AllStaffShift.STATUS_INITIAL) View.VISIBLE else View.GONE
            button2.visibility = if(status == AllStaffShift.STATUS_INITIAL) View.VISIBLE else View.GONE
            clockedIn.visibility = if(status == AllStaffShift.STATUS_PRESENT) View.VISIBLE else View.GONE
            clockout.visibility = if(status == AllStaffShift.STATUS_CLOCKED_OUT) View.VISIBLE else View.GONE
            Late.visibility = if(status == AllStaffShift.STATUS_LATE) View.VISIBLE else View.GONE
            Absent.visibility = if(status == AllStaffShift.STATUS_ABSENT) View.VISIBLE else View.GONE

            if(status == AllStaffShift.STATUS_INITIAL) {
                button2.setOnClickListener {
                    var attendanceModel = SiteEmployeeAttendanceModel()
                    attendanceModel.employeeId = employee.employeeId
                    attendanceModel.eventId = employee.eventId
                    attendanceModel.position = employee.position
                    attendanceModel.siteId = employee.siteId
                    attendanceModel.time = formatter.format(Date())
                    attendanceModel.type = "timeIn"
                    attendanceModel.arrayIndex = position
                    attendanceModel.deptNumber = employee.distNumber
                    attendance.value = attendanceModel
                }
            }

            if(status == AllStaffShift.STATUS_PRESENT) {
                clockedIn.setOnClickListener {
                    onShowPopup(holder.itemView.context, employee, position, "Clocked In button clicked for ${employee.firstName}")
                }
            }

            if(status == AllStaffShift.STATUS_LATE) {
                Late.setOnClickListener {
                    onShowPopup(holder.itemView.context, employee, position, "Clocked In button clicked for ${employee.firstName}")
                }
            }

            if(status == AllStaffShift.STATUS_CLOCKED_OUT) {
                clockout.setOnClickListener {
                    onShowPopup(holder.itemView.context, employee, position, "Clocked Out button clicked for ${employee.lastName}")
                }
            }

//            if (isAllTabSelected) {
//                button1.visibility = View.VISIBLE
//                button2.visibility = View.VISIBLE
//                clockedIn.visibility = View.GONE
//                Late.visibility = View.GONE
//                Absent.visibility = View.GONE
//                clockout.visibility = View.GONE
//
//                button2.setOnClickListener {
//                    button1.visibility = View.GONE
//                    button2.visibility = View.GONE
//
//                    when (status) {
//                        Employee.STATUS_PRESENT -> {
//                            clockedIn.visibility = View.VISIBLE
//                            Late.visibility = View.GONE
//                            Absent.visibility = View.GONE
//                            clockout.visibility = View.GONE
//
//                            // Move the popup trigger to the clockedIn button click listener
//                            clockedIn.setOnClickListener {
//                                onShowPopup(holder.itemView.context, "Clocked In button clicked for ${employee.employeeName}")
//                                // Handle additional login logic here if needed
//                            }
//
//                            clockout.setOnClickListener {
//                                onShowPopup(holder.itemView.context, "Clocked Out button clicked for ${employee.employeeName}")
//                                // Handle additional clock-out logic here if needed
//                            }
//                        }
//                        Employee.STATUS_LATE -> {
//                            clockedIn.visibility = View.GONE
//                            Late.visibility = View.VISIBLE
//                            Absent.visibility = View.GONE
//                            clockout.visibility = View.GONE
//                        }
//                        Employee.STATUS_ABSENT -> {
//                            clockedIn.visibility = View.GONE
//                            Late.visibility = View.GONE
//                            Absent.visibility = View.VISIBLE
//                            clockout.visibility = View.GONE
//                        }
//                        Employee.STATUS_CLOCKED_OUT -> {
//                            clockedIn.visibility = View.GONE
//                            Late.visibility = View.GONE
//                            Absent.visibility = View.GONE
//                            clockout.visibility = View.VISIBLE
//
//                            clockout.setOnClickListener {
//                                onShowPopup(holder.itemView.context, "Clocked Out button clicked for ${employee.employeeName}")
//                                // Handle additional clock-out logic here if needed
//                            }
//                        }
//                    }
//
//                }
//            } else {
//                button1.visibility = View.GONE
//                button2.visibility = View.GONE
//                clockedIn.visibility = View.GONE
//                Late.visibility = View.GONE
//                Absent.visibility = View.GONE
//                clockout.visibility = View.GONE
//
//                when (status) {
//                    Employee.STATUS_LATE -> Late.visibility = View.VISIBLE
//                    Employee.STATUS_ABSENT -> Absent.visibility = View.VISIBLE
//                }
//            }
        }
    }

    override fun getItemCount() = employees.size
}