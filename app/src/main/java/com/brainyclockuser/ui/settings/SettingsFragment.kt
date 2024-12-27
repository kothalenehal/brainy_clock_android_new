package com.brainyclockuser.ui.settings

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.FragmentSettingsBinding
import com.brainyclockuser.ui.auth.AuthActivity
import retrofit2.HttpException

class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.toolbar.ivRight.visibility = View.GONE

        viewModel.loading.observe(viewLifecycleOwner) { if (it) showLoader() else hideLoader() }
        viewModel.throwable.observe(viewLifecycleOwner) {
            if (it is HttpException) {
                val status = it.response()?.errorBody()?.string()?.contains("token has been revoked") ?: false
                if(it.code() == 400 && status) {
                    prefUtils.clearData(requireActivity())
                    requireActivity().finish()
                    startActivity(Intent(requireActivity(), AuthActivity::class.java))
                }
            } else {
                handleError(it)
            }
        }

        binding.tvLogout.setOnClickListener { logout() }
        binding.tvChangePassword.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToChangePasswordFragment())
        }
        binding.tvBluetooth.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToBluetoothFragment())
        }
        binding.tvNotificationsAmpAlerts.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToNotificationsFragment())
        }
    }

    private fun logout() {
        val dialogClickListener: DialogInterface.OnClickListener = object :
            DialogInterface.OnClickListener {

            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        viewModel.logout.observe(viewLifecycleOwner) {
                            if (it.success) {
                                prefUtils.clearData(requireActivity())
                                requireActivity().finish()
                                startActivity(Intent(requireActivity(), AuthActivity::class.java))
                            } else {
                                showError(it.message)
                            }
                        }
                        viewModel.callLogoutApi()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {}
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()


    }

}