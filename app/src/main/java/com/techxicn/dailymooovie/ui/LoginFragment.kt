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
import com.techxicn.dailymooovie.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

/**
 * Pantalla de INICIO DE SESIÓN (Login).
 * Email/contraseña (FirebaseAuth) + Google (Credential Manager) vía AuthManager.
 * Al autenticar correctamente notifica al host (MainActivity) para ir a Today.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager
    private val host get() = activity as? AuthHost

    /** Estado del toggle mostrar/ocultar contraseña. */
    private var passwordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager(requireContext().applicationContext)

        binding.btnSignIn.setOnClickListener { attemptEmailSignIn() }
        binding.btnGoogle.setOnClickListener { attemptGoogleSignIn() }
        binding.btnGoRegister.setOnClickListener { host?.showRegister() }
        binding.btnTogglePassword.setOnClickListener { togglePasswordVisibility() }
        // "¿Olvidaste tu contraseña?" — solo UI por ahora (sin lógica de recuperación).
        binding.tvForgotPassword.setOnClickListener { /* TODO: flujo de recuperación */ }
    }

    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
        val selection = binding.etPassword.selectionEnd
        binding.etPassword.inputType = if (passwordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etPassword.setSelection(selection.coerceAtLeast(0))
        binding.btnTogglePassword.setImageResource(
            if (passwordVisible) R.drawable.ic_action_eye else R.drawable.ic_eye_off
        )
    }

    private fun attemptEmailSignIn() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        when {
            email.isEmpty() || password.isEmpty() -> {
                showError(R.string.auth_error_empty_fields); return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError(R.string.auth_error_invalid_email); return
            }
        }

        hideError()
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val result = authManager.signInWithEmail(email, password)
            setLoading(false)
            handleResult(result)
        }
    }

    private fun attemptGoogleSignIn() {
        hideError()
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            // Credential Manager necesita un contexto de UI (Activity).
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
        binding.progressSignIn.visibility = if (loading) View.VISIBLE else View.GONE
        binding.tvSignInLabel.visibility = if (loading) View.INVISIBLE else View.VISIBLE
        binding.btnSignIn.isClickable = !loading
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
