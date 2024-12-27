package com.brainyclockuser.ui.shifts

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainyclockuser.databinding.RowDetailsDayBinding
import com.brainyclockuser.utils.OnItemClickListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val list: List<EmployeeHistoryDataModel>,
    private val onClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private lateinit var context: Context

    val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val dateTimeFormatterReadable = SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault())

    class ViewHolder(val binding: RowDetailsDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = RowDetailsDayBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = list.get(position);

        holder.binding.laneText.text = dataModel.siteName
        holder.binding.clockInTime.text = dataModel.clockIn
        holder.binding.clockOutTime.text = dataModel.clockOut
        try {
            val createdAt = dateTimeFormatter.parse(dataModel.createdAt)
            holder.binding.dayAndDate.text = dateTimeFormatterReadable.format(createdAt);
        } catch (e: Exception) {

        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
}