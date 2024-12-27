package com.brainyclockuser.ui.clockin

import AllStaffShift
import SimplePopupDialog
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainyclockuser.databinding.FragmentAbsentBinding

class AbsentFragment : Fragment() {

    private lateinit var binding: FragmentAbsentBinding
    private lateinit var adapter: EmployeeAdapter

    val list: MutableLiveData<List<AllStaffShift>> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAbsentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Pass false for isAllTabSelected to hide buttons in "ABSENT" tab
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