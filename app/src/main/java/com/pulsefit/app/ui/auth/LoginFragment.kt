package com.pulsefit.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pulsefit.app.databinding.FragmentLoginBinding
import com.pulsefit.app.ui.MainActivity
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        GoogleSignInHelper.credentialFromIntent(result.data)
            .onSuccess { viewModel.signInWithGoogle(it) }
            .onFailure { showError("Google sign-in was cancelled or failed.") }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            viewModel.login(
                binding.emailInput.text.toString(),
                binding.passwordInput.text.toString()
            )
        }

        binding.googleSignInButton.setOnClickListener {
            googleSignInLauncher.launch(GoogleSignInHelper.signInIntent(requireContext()))
        }

        binding.goToRegisterLink.setOnClickListener {
            findNavController().navigate(com.pulsefit.app.R.id.action_login_to_register)
        }

        binding.forgotPasswordLink.setOnClickListener {
            Toast.makeText(requireContext(), "Password reset coming soon.", Toast.LENGTH_SHORT).show()
        }

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.loginProgress.visibility = if (state is AuthUiState.Loading) View.VISIBLE else View.GONE
                    binding.loginButton.isEnabled = state !is AuthUiState.Loading

                    when (state) {
                        is AuthUiState.Error -> showError(state.message)
                        is AuthUiState.Success -> goToMain()
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }

    private fun goToMain() {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
