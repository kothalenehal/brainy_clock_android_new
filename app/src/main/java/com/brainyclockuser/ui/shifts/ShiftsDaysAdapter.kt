package com.brainyclockuser.ui.shifts

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainyclockuser.databinding.RowShiftDaysBinding
import com.brainyclockuser.ui.clockin.ShiftDay
import java.text.SimpleDateFormat
import java.util.*

class ShiftsDaysAdapter(
    private val list: List<ShiftDay>,
    private val isTodaysShift: Boolean,
) :
    RecyclerView.Adapter<ShiftsDaysAdapter.ViewHolder>() {
    private lateinit var context: Context

    class ViewHolder(val binding: RowShiftDaysBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding =
            RowShiftDaysBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.isTodaysShift = isTodaysShift

        var time = ""
        val timeIn = list[position].timeIn?.let {
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(it)
        }
        val timeOut = list[position].timeOut?.let {
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(it)
        }
        val df = SimpleDateFormat("hh:mm a", Locale.getDefault())
        if (timeIn != null && timeOut != null) {
            time += "${df.format(timeIn)} to ${df.format(timeOut)}"
        } else {
            time += " - "
        }

        val shortDayNames = mapOf(
            "sun" to "Sunday",
            "mon" to "Monday",
            "tue" to "Tuesday",
            "wed" to "Wednesday",
            "thu" to "Thursday",
            "fri" to "Friday",
            "sat" to "Saturday"
        )
        val shortDayName = shortDayNames[list[position].day].toString()
        val today = Calendar.getInstance()[Calendar.DAY_OF_WEEK]
        val dayNames = mapOf(
            Calendar.SUNDAY to "sun",
            Calendar.MONDAY to "mon",
            Calendar.TUESDAY to "tue",
            Calendar.WEDNESDAY to "wed",
            Calendar.THURSDAY to "thu",
            Calendar.FRIDAY to "fri",
            Calendar.SATURDAY to "sat"
        )

        if (list[position].day == dayNames[today]) {
            holder.binding.tvDay.text = Html.fromHtml("<b>$shortDayName</b>", Html.FROM_HTML_MODE_LEGACY)
            holder.binding.tvDate.text = Html.fromHtml("<b>$time</b>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            holder.binding.tvDay.text = shortDayName
            holder.binding.tvDate.text = time
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}