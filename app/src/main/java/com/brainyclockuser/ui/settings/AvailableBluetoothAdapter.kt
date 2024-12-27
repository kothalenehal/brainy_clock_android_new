package com.brainyclockuser.ui.settings

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainyclockuser.databinding.RowAvailableBluetoothBinding
import com.brainyclockuser.utils.OnItemClickListener

class AvailableBluetoothDevice(var name: String, var device: BluetoothDevice) {

}

class AvailableBluetoothAdapter(
    private val onClickListener: BluetoothFragment.SelectBluetoothDevice
) :
    RecyclerView.Adapter<AvailableBluetoothAdapter.ViewHolder>() {

    private val bleDeviceList = ArrayList<AvailableBluetoothDevice>()

    private lateinit var context: Context

    class ViewHolder(val binding: RowAvailableBluetoothBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding =
            RowAvailableBluetoothBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.binding.name = bleDeviceList[position].name ?: bleDeviceList[position].address
        holder.binding.name = bleDeviceList[position].name
            holder.binding.llMain.setOnClickListener {
            onClickListener.onBluetoothDeviceClick(
                position,
                bleDeviceList[holder.absoluteAdapterPosition]
            )
        }
    }


    override fun getItemCount(): Int {
        return bleDeviceList.size
    }

    fun updateItems(results: List<AvailableBluetoothDevice>) {

        //bleDeviceList.clear()
        bleDeviceList.addAll(results)
        var tmpDevices = bleDeviceList.distinctBy { it.device.address } as ArrayList<AvailableBluetoothDevice>
        bleDeviceList.clear();

        bleDeviceList.addAll(tmpDevices)
        notifyDataSetChanged()
    }
}