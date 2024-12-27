package com.brainyclockuser.ui.clockin

import AllStaffShift
import SimplePopupDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainyclockuser.databinding.FragmentLateBinding

class LateFragment : Fragment() {

    private lateinit var binding: FragmentLateBinding
    private lateinit var adapter: EmployeeAdapter

    val list: MutableLiveData<List<AllStaffShift>> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Pass false for isAllTabSelected to hide buttons in "LATE" tab
        adapter = EmployeeAdapter(getEmployees(), isAllTabSelected = false) { context, employee, position, message ->
            showSimplePopup(context, message)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun getEmployees(): List<AllStaffShift> {
        return list.value!!
    }
    private fun showSimplePopup(context: android.content.Context, message: String) {
        val simplePopup = SimplePopupDialog(context)
        simplePopup.show(message)
    }
}
