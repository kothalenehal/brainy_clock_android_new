package com.brainyclockuser.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.FragmentLoginBinding
import com.brainyclockuser.ui.MainActivity
import com.brainyclockuser.ui.auth.AuthenticationViewModel
import com.brainyclockuser.ui.auth.Constant
import com.brainyclockuser.utils.AppConstant
import java.util.*

class LoginFragment : BaseFragment() {

    private val viewModel: AuthenticationViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        observeValidation()
        initListener()
    }

    private fun observeValidation() {
        viewModel.validateLivedata.observe(viewLifecycleOwner) {
            when (it) {
                Constant.IS_EMAIL_EMPTY -> {
                    binding.etEmail.requestFocus()
                    binding.etEmail.error = getString(R.string.strWarnEmail)
                }
                Constant.IS_EMAIL_INVALID -> {
                    binding.etEmail.requestFocus()
                    binding.etEmail.error = getString(R.string.strValidEmail)
                }
                Constant.IS_PASSWORD_EMPTY -> {
                    binding.etPassword.requestFocus()
                    binding.etEmail.error = null
                    binding.etPassword.error = getString(R.string.strWarnPassword)
                }
                Constant.IS_PASSWORD_INVALID -> {
                    binding.etPassword.requestFocus()
                    binding.etEmail.error = null
                    binding.etPassword.error = getString(R.string.strValidPassword)
                }
                Constant.IS_VALID -> {
                    binding.etEmail.error = null
                    binding.etPassword.error = null

                }
            }
        }

        viewModel.userModelLivedata.observe(viewLifecycleOwner) {
            if (it.success) {
                Log.e("TAG", "observeValidation: ${it.data?.type}", )
                Log.e("TAG", "observeValidation: ${it.data?.employeeId}", )
                Log.e("TAG", "observeValidation: ${it.data?.accessToken}", )
                prefUtils.saveData(
                    requireActivity(),
                    AppConstant.SharedPreferences.ACCESS_TOKEN,
                    it.data?.accessToken
                )
                prefUtils.saveData(
                    requireActivity(),
                    AppConstant.SharedPreferences.REFRESH_TOKEN,
                    it.data?.refreshToken
                )
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.SECOND, 3600)
                prefUtils.saveData(
                    requireActivity(),
                    AppConstant.SharedPreferences.TOKEN_EXPIRE_ON,
                    calendar.timeInMillis
                )
                prefUtils.saveData(
                    requireActivity(),
                    AppConstant.SharedPreferences.EMAIL,
                    it.data?.email
                )
                prefUtils.saveData(
                    requireActivity(),
                    AppConstant.SharedPreferences.NAME,
                    it.data?.name
                )
//                prefUtils.saveData(
//                    requireActivity(),
//                    AppConstant.SharedPreferences.DEPARTMENTNAME,
//                    it.data?.departmentName
//                )
                prefUtils.saveData(
                    requireActivity(),
                    AppConstant.SharedPreferences.DEPARTMENTNAME,
                    it.data?.departmentId
                )
                prefUtils.saveData(
                    requireActivity(),
                    AppConstant.SharedPreferences.EMPLOYEE_ID,
                    it.data?.employeeId ?: 0
                )
                prefUtils.saveData(
                    requireActivity(),
                    AppConstant.SharedPreferences.COMPANY_ID,
                    it.data?.companyId ?: 0
                )
                prefUtils.saveData(
                    requireActivity(),
                    AppConstant.SharedPreferences.ApplicationType,
                    it.data?.type ?: 1
                )

                if(it.data?.locationModel!=null && it.data?.locationModel?.size!! > 0) {
                    val locationModel = it.data?.locationModel?.get(0)!!
                    prefUtils.saveData(requireActivity(),AppConstant.SharedPreferences.GeofenceRadious,
                        locationModel.geofence_radius ?: 0
                    )
                    prefUtils.saveData(
                        requireActivity(),
                        AppConstant.SharedPreferences.Latitude,
                        locationModel.latitude ?: ""
                    )
                    prefUtils.saveData(
                        requireActivity(),
                        AppConstant.SharedPreferences.Longitude,
                        locationModel.longitude ?: ""
                    )

                    prefUtils.saveData(requireActivity(),AppConstant.SharedPreferences.OfficeLocationName,locationModel.locationName ?: "")

                }

                requireActivity().finish()
                startActivity(Intent(requireActivity(), MainActivity::class.java))
            } else showError(it.message)
        }
    }

    private fun init() {
        binding.viewmodel = viewModel
    }

    private fun initListener() {
        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
        }
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
        }

        viewModel.loading.observe(viewLifecycleOwner) { if (it) showLoader() else hideLoader() }
        viewModel.throwable.observe(viewLifecycleOwner) { handleError(it) }
    }
}