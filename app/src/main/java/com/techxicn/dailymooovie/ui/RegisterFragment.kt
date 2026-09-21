package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.techxicn.dailymooovie.R
import com.techxicn.dailymooovie.auth.AuthHost
import com.techxicn.dailymooovie.auth.AuthManager
import com.techxicn.dailymooovie.auth.AuthResult
import com.techxicn.dailymooovie.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

/**
 * Pantalla de REGISTRO (Sign Up).
 * Crea cuenta con email/contraseña (FirebaseAuth) guardando el displayName, o
 * inicia con Google (Credential Manager). Al autenticar navega a Today.
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager
    private val host get() = activity as? AuthHost

    /** Estado del toggle mostrar/ocultar contraseña. */
    private var passwordVisible = false

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager(requireContext().applicationContext)

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.btnGoogle.setOnClickListener { attemptGoogleSignIn() }
        binding.btnGoLogin.setOnClickListener { host?.showLogin() }
        binding.btnTogglePassword.setOnClickListener { togglePasswordVisibility() }
    }

    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
        val selection = binding.etPassword.selectionEnd
        binding.etPassword.inputType = if (passwordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        // Mantiene la posición del cursor tras cambiar el inputType.
        binding.etPassword.setSelection(selection.coerceAtLeast(0))
        binding.btnTogglePassword.setImageResource(
            if (passwordVisible) R.drawable.ic_action_eye else R.drawable.ic_eye_off
        )
    }

    private fun attemptRegister() {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etPasswordConfirm.text?.toString().orEmpty()

        when {
            name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() -> {
                showError(R.string.auth_error_empty_fields); return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError(R.string.auth_error_invalid_email); return
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                showError(R.string.auth_error_short_password); return
            }
            // Valida que contraseña y confirmación coincidan antes de enviar.
            password != confirm -> {
                showError(R.string.auth_error_password_mismatch); return
            }
        }

        hideError()
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val result = authManager.registerWithEmail(email, password, name)
            setLoading(false)
            handleResult(result)
        }
    }

    private fun attemptGoogleSignIn() {
        hideError()
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val result = authManager.signInWithGoogle(requireActivity())
            setLoading(false)
            handleResult(result)
        }
    }

    private fun handleResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> host?.onAuthSuccess()
            is AuthResult.Error -> showError(result.messageRes)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressRegister.visibility = if (loading) View.VISIBLE else View.GONE
        binding.tvRegisterLabel.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        binding.btnRegister.isClickable = !loading
        binding.btnGoogle.isClickable = !loading
    }

    private fun showError(messageRes: Int) {
        binding.tvError.setText(messageRes)
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
