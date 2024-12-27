package com.brainyclockuser.ui.clockin

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainyclockuser.databinding.RowActiveOvertimeNaShiftBinding
import com.brainyclockuser.databinding.RowActiveOvertimePendingShiftBinding
import com.brainyclockuser.databinding.RowActiveOvertimeShiftBinding
import com.brainyclockuser.databinding.RowActiveShiftBinding
import com.brainyclockuser.databinding.RowClockedOutShiftBinding
import com.brainyclockuser.databinding.RowOtherShiftBinding
import com.brainyclockuser.databinding.RowUpcomingShiftBinding
import com.brainyclockuser.utils.AppConstant
import com.brainyclockuser.utils.OnItemClickListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockInAdapter(
    private val list: ArrayList<ShiftsModels>,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class VHUpcoming(val binding: RowUpcomingShiftBinding) : RecyclerView.ViewHolder(binding.root)
    class VHActive(val binding: RowActiveShiftBinding) : RecyclerView.ViewHolder(binding.root)
    class VHClockedOut(val binding: RowClockedOutShiftBinding) :
        RecyclerView.ViewHolder(binding.root)

    class VHActiveOvertime(val binding: RowActiveOvertimeShiftBinding) :
        RecyclerView.ViewHolder(binding.root)

    class VHActiveOvertimeNA(val binding: RowActiveOvertimeNaShiftBinding) :
        RecyclerView.ViewHolder(binding.root)

    class VHActiveOvertimePending(val binding: RowActiveOvertimePendingShiftBinding) :
        RecyclerView.ViewHolder(binding.root)

    class VHOther(val binding: RowOtherShiftBinding) : RecyclerView.ViewHolder(binding.root)

    private val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.e("---", "View Type: $viewType")
        when (viewType) {
            AppConstant.UPCOMING_SHIFT -> {
                val binding = RowUpcomingShiftBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return VHUpcoming(binding)
            }

            /*AppConstant.ACTIVE_SHIFT -> {
                val binding = RowActiveShiftBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return VHActive(binding)
            }

            AppConstant.CLOCKED_OUT_SHIFT -> {
                val binding = RowClockedOutShiftBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return VHClockedOut(binding)
            }

            AppConstant.ACTIVE_OVERTIME_SHIFT,
            AppConstant.ACTIVE_IN_OVERTIME_SHIFT
            -> {
                val binding = RowActiveOvertimeShiftBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return VHActiveOvertime(binding)
            }

            AppConstant.ACTIVE_OVERTIME_NA_SHIFT -> {
                val binding = RowActiveOvertimeNaShiftBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return VHActiveOvertimeNA(binding)
            }

            AppConstant.ACTIVE_OVERTIME_PENDING_SHIFT -> {
                val binding = RowActiveOvertimePendingShiftBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return VHActiveOvertimePending(binding)
            }*/

            else -> {
                val binding =
                    RowOtherShiftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return VHOther(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dayName = dayFormatter.format(Date()).lowercase()
        when (holder) {
            is VHUpcoming -> {
                val event = list[position]
                holder.binding.model = event
                Log.e("---", "list: ${position} = ${list.size} = ${list[position].shiftId} = ${list[position].shiftName}")
                holder.binding.shiftDone.visibility=View.GONE
                holder.binding.shiftDoneOut.visibility=View.GONE
                holder.binding.tvShiftTime.visibility = View.VISIBLE
                val dayShift = list[position].days!!.first { value -> value.day == dayName }
                if (dayShift != null) {
                    holder.binding.tvShiftTime.text = dayShift.timeIn + " - " + dayShift.timeOut
                }
                if(event.clockIn != null) {
                    holder.binding.tvShiftTime.visibility = View.GONE
                    holder.binding.shiftDone.visibility=View.VISIBLE
                }
                if(event.clockIn != null && event.clockOut != null) {
                    holder.binding.shiftDone.visibility=View.GONE
                    holder.binding.shiftDoneOut.visibility=View.VISIBLE
                }
                holder.binding.cardShift.setOnClickListener {
                    if(event.clockIn.isNullOrBlank()) {
                        itemClickListener.onItemClick(position)
                        holder.binding.tvShiftTime.visibility=View.GONE
                        holder.binding.shiftDone.visibility=View.VISIBLE
                    }
//                    if(event.clockIn != null && event.clockOut.isNullOrBlank()) {
//                        itemClickListener.onItemClick(position)
//                        holder.binding.tvShiftTime.visibility=View.GONE
//                        holder.binding.shiftDone.visibility=View.GONE
//                        holder.binding.shiftDoneOut.visibility=View.VISIBLE
//                    }
                }
            }

//            is VHActive -> {
//                holder.binding.model = list[position]
//            }
//            is VHClockedOut -> {
//                holder.binding.model = list[position]
//            }
//            is VHActiveOvertime -> {
//                holder.binding.model = list[position]
//                holder.binding.inOvertime =
//                    list[position].customStatus == AppConstant.ACTIVE_IN_OVERTIME_SHIFT
//                holder.binding.tvOvertime.visibility = View.GONE
//                holder.binding.tvOvertime.setOnClickListener { itemClickListener.onItemClick(holder.absoluteAdapterPosition) }
//            }
//            is VHActiveOvertimeNA -> {
//                holder.binding.model = list[position]
//            }
//            is VHActiveOvertimePending -> {
//                holder.binding.model = list[position]
//            }
            is VHOther -> {
                holder.binding.model = list[position]
                val dayShift = list[position].days!!.first { value -> value.day == dayName }
                if (dayShift != null) {
                    holder.binding.tvShiftTime.text = dayShift.timeIn + " - " + dayShift.timeOut
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].customStatus
    }

}