package com.brainyclockuser.ui.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainyclockuser.databinding.RowPairedBluetoothBinding
import com.brainyclockuser.utils.OnItemClickListener


class PairedBluetoothAdapter(private val list: ArrayList<String>, private val onClickListener: OnItemClickListener) :
    RecyclerView.Adapter<PairedBluetoothAdapter.ViewHolder>() {

    private lateinit var context: Context

    class ViewHolder(val binding: RowPairedBluetoothBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = RowPairedBluetoothBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }


    override fun getItemCount(): Int {
        return list.size
    } }