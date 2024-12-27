package com.brainyclockuser.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.FragmentChangePasswordBinding
import com.brainyclockuser.ui.auth.AuthActivity
import java.util.regex.Matcher
import java.util.regex.Pattern

class ChangePasswordFragment : BaseFragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loading.observe(viewLifecycleOwner) { if (it) showLoader() else hideLoader() }
        viewModel.throwable.observe(viewLifecycleOwner) { handleError(it) }

        binding.toolbar.ivRight.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnChangePassword.setOnClickListener {
            if (isValid()) {
                viewModel.callChangePasswordApi(
                    binding.etPassword.editText?.text.toString(),
                    binding.etNewPassword.editText?.text.toString()
                )
            }
        }

        viewModel.changePassword.observe(viewLifecycleOwner) {
            showError(it.message)
            if (it.success) {
                prefUtils.clearData(requireActivity())
                requireActivity().finish()
                startActivity(Intent(requireActivity(), AuthActivity::class.java))
//                findNavController().popBackStack()
            }
        }
    }

    private fun isValid(): Boolean {
        val password = binding.etPassword.editText?.text.toString()
        val newPassword = binding.etNewPassword.editText?.text.toString()
        val rePassword = binding.etReenterPassword.editText?.text.toString()

        if (TextUtils.isEmpty(password.trim())) {
            resetErrorMessages()
            binding.etPassword.requestFocus()
            binding.etPassword.error = getString(R.string.strWarnOldPassword)
            return false
        } else if (TextUtils.isEmpty(newPassword.trim())) {
            resetErrorMessages()
            binding.etNewPassword.requestFocus()
            binding.etNewPassword.error = getString(R.string.strWarnNewPassword)
            return false
        } else if (newPassword.trim().length < 8) {
            resetErrorMessages()
            binding.etNewPassword.requestFocus()
            binding.etNewPassword.error = getString(R.string.strValidNewPassword)
            return false
        } else if (!isValidPassword(newPassword)) {
            resetErrorMessages()
            binding.etNewPassword.requestFocus()
            binding.etNewPassword.error = getString(R.string.password_helper_text)
            return false
        } else if (TextUtils.isEmpty(rePassword.trim())) {
            resetErrorMessages()
            binding.etReenterPassword.requestFocus()
            binding.etReenterPassword.error = getString(R.string.strWarnRePassword)
            return false
        } else if (newPassword != rePassword) {
            resetErrorMessages()
            binding.etReenterPassword.requestFocus()
            binding.etReenterPassword.error = getString(R.string.strMatchPassword)
            return false
        }
        return true
    }

    private fun resetErrorMessages() {
        binding.etPassword.error = null
        binding.etNewPassword.error = null
        binding.etReenterPassword.error = null
    }

    private fun isValidPassword(password: String): Boolean {
        val pattern: Pattern
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(passwordPattern)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

}