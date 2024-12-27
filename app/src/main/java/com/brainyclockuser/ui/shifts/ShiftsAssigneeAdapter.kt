package com.brainyclockuser.ui.shifts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainyclockuser.databinding.RowShiftAssigneeBinding

class ShiftsAssigneeAdapter(private val list: ArrayList<String>) :
    RecyclerView.Adapter<ShiftsAssigneeAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowShiftAssigneeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowShiftAssigneeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return list.size
    }
}