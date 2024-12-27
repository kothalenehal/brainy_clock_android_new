package com.brainyclockuser.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.FragmentSignUpBinding
import com.brainyclockuser.ui.MainActivity
import com.brainyclockuser.ui.auth.AuthenticationViewModel
import com.brainyclockuser.ui.auth.Constant

class SignUpFragment : BaseFragment() {

    private lateinit var binding: FragmentSignUpBinding
    private val viewModel: AuthenticationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        observeValidation()
    }

    private fun init() {
        binding.viewmodel = viewModel
        binding.tvLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeValidation() {
        viewModel.validateLivedata.observe(viewLifecycleOwner) {
            when (it) {
                Constant.IS_FIRST_NAME_EMPTY -> {
                    resetErrorMessages()
                    binding.etFirstName.requestFocus()
                    binding.etFirstName.error = getString(R.string.strWarnFirstName)
                }
                Constant.IS_LAST_NAME_EMPTY -> {
                    resetErrorMessages()
                    binding.etLastName.requestFocus()
                    binding.etLastName.error = getString(R.string.strWarnLastname)
                }
                Constant.IS_EMAIL_EMPTY -> {
                    resetErrorMessages()
                    binding.etEmail.requestFocus()
                    binding.etEmail.error = getString(R.string.strWarnEmail)
                }
                Constant.IS_EMAIL_INVALID -> {
                    resetErrorMessages()
                    binding.etEmail.requestFocus()
                    binding.etEmail.error = getString(R.string.strValidEmail)
                }
                Constant.IS_COMPANY_ID_EMPTY -> {
                    resetErrorMessages()
                    binding.etCompanyId.requestFocus()
                    binding.etCompanyId.error = getString(R.string.strWarnCompanyId)
                }
                Constant.IS_EMP_ID_EMPTY -> {
                    resetErrorMessages()
                    binding.etEmpId.requestFocus()
                    binding.etEmpId.error = getString(R.string.strWarnEmpId)
                }
                Constant.IS_PASSWORD_EMPTY -> {
                    resetErrorMessages()
                    binding.etPassword.requestFocus()
                    binding.etPassword.error = getString(R.string.strWarnPassword)
                }
                Constant.IS_PASSWORD_INVALID -> {
                    resetErrorMessages()
                    binding.etPassword.requestFocus()
                    binding.etPassword.error = getString(R.string.password_helper_text)
                }
                Constant.IS_RE_PASS_EMPTY -> {
                    resetErrorMessages()
                    binding.etReenterPassword.requestFocus()
                    binding.etReenterPassword.error = getString(R.string.strWarnRePassword)
                }
                Constant.IS_PASS_NOT_MATCH -> {
                    resetErrorMessages()
                    binding.etReenterPassword.requestFocus()
                    binding.etReenterPassword.error = getString(R.string.strMatchPassword)
                }
                Constant.IS_VALID -> {

                }
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { if (it) showLoader() else hideLoader() }
        viewModel.throwable.observe(viewLifecycleOwner) { handleError(it) }

        viewModel.userModelLivedata.observe(viewLifecycleOwner) {
            if (it.success) {
                findNavController().popBackStack()
            } else showError(it.message)
        }

    }

    private fun resetErrorMessages() {
        binding.etFirstName.error = null
        binding.etLastName.error = null
        binding.etEmail.error = null
        binding.etCompanyId.error = null
        binding.etEmpId.error = null
        binding.etPassword.error = null
        binding.etReenterPassword.error = null
    }

}