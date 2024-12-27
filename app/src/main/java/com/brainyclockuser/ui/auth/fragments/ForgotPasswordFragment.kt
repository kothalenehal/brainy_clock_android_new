package com.brainyclockuser.ui.auth.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.FragmentForgotPasswordBinding
import com.brainyclockuser.ui.auth.AuthenticationViewModel
import com.brainyclockuser.ui.auth.Constant

class ForgotPasswordFragment : BaseFragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private val viewModel: AuthenticationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForgotPasswordBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeValidation()
    }

    var index = 1
    private fun initUI() {
        binding.viewmodel = viewModel
        binding.visibleIndex = index

        binding.toolbar.ivRight.setOnClickListener { findNavController().popBackStack() }

        binding.btnSubmit.setOnClickListener {
            if (index == 1)
                viewModel.forgotPassword()
            else
                viewModel.resetPassword()
        }
    }

    private fun observeValidation() {
        viewModel.validateLivedata.observe(viewLifecycleOwner) {
            when (it) {
                Constant.IS_EMAIL_EMPTY -> {
                    reset()
                    binding.etEmail.requestFocus()
                    binding.etEmail.error = getString(R.string.strWarnEmail)
                }
                Constant.IS_EMAIL_INVALID -> {
                    reset()
                    binding.etEmail.requestFocus()
                    binding.etEmail.error = getString(R.string.strValidEmail)
                }
                Constant.IS_CODE_EMPTY -> {
                    reset()
                    binding.etCode.requestFocus()
                    binding.etCode.error = getString(R.string.strWarnCode)
                }
                Constant.IS_PASSWORD_EMPTY -> {
                    reset()
                    binding.etPassword.requestFocus()
                    binding.etPassword.error = getString(R.string.strWarnPassword)
                }
                Constant.IS_PASSWORD_INVALID -> {
                    reset()
                    binding.etPassword.requestFocus()
                    binding.etPassword.error = getString(R.string.strValidPassword)
                }
                else -> reset()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { if (it) showLoader() else hideLoader() }
        viewModel.throwable.observe(viewLifecycleOwner) { handleError(it) }
        viewModel.forgotPassword.observe(viewLifecycleOwner) {
            showError(it.message)
            if (it.success) {
                binding.visibleIndex = ++index
            }
        }
        viewModel.resetPassword.observe(viewLifecycleOwner) {
            showError(it.message)
            if (it.success) {
                findNavController().popBackStack()
            }
        }
    }

    private fun reset() {
        binding.etEmail.error = null
        binding.etCode.error = null
        binding.etPassword.error = null
    }

}