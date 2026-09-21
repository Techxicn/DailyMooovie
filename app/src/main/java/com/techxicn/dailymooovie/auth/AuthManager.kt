package com.techxicn.dailymooovie.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.techxicn.dailymooovie.R
import kotlinx.coroutines.tasks.await

/**
 * AuthManager
 * ═══════════
 * Capa de autenticación de MOOOVIE. Encapsula:
 *   • Firebase Auth para email/contraseña (createUser / signIn).
 *   • Google Sign-In vía Credential Manager (API moderna) → ID token →
 *     FirebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(...)).
 *
 * Las funciones son suspend y devuelven [AuthResult], de modo que la UI solo
 * decide navegar (Success) o mostrar un mensaje (Error con un stringRes).
 */
class AuthManager(private val appContext: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(appContext)

    /** ¿Hay una sesión de Firebase activa? Se usa para decidir Login vs Today al arrancar. */
    fun isLoggedIn(): Boolean = auth.currentUser != null

    /**
     * Nombre a mostrar registrado por el usuario (displayName de Firebase).
     * NO usa el correo como sustituto: si no hay displayName devuelve null y la UI
     * decide el texto de reserva. Devuelve null también si no hay sesión.
     */
    fun currentDisplayName(): String? {
        val name = auth.currentUser?.displayName?.trim().orEmpty()
        return name.ifEmpty { null }
    }

    /**
     * Recarga el perfil del usuario desde el servidor de Firebase. Útil tras el
     * primer login para asegurar que displayName ya esté propagado localmente.
     * Silencioso ante fallos (mantiene el valor en caché).
     */
    suspend fun reloadUser() {
        try {
            auth.currentUser?.reload()?.await()
        } catch (_: Exception) {
            // Sin conexión u otro error: se conserva el perfil en caché.
        }
    }

    /** Correo del usuario actual (o null si no hay sesión). */
    fun currentEmail(): String? = auth.currentUser?.email

    /** Inicial en mayúscula para el avatar (del nombre registrado). "?" si no hay dato. */
    fun currentInitial(): String =
        currentDisplayName()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    /** Cierra la sesión de Firebase. */
    fun signOut() = auth.signOut()

    // ─────────────────────────────────────────────────────────────────────
    //  EMAIL / CONTRASEÑA
    // ─────────────────────────────────────────────────────────────────────

    /** Registro con email/contraseña. Guarda [displayName] en el perfil de Firebase. */
    suspend fun registerWithEmail(email: String, password: String, displayName: String): AuthResult =
        try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            // Guarda el nombre a mostrar en el perfil del usuario recién creado.
            val name = displayName.trim()
            if (name.isNotEmpty()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                result.user?.updateProfile(profileUpdates)?.await()
            }
            AuthResult.Success
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error(R.string.auth_error_email_in_use)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error(R.string.auth_error_invalid_email)
        } catch (e: Exception) {
            AuthResult.Error(R.string.auth_error_generic)
        }

    /** Inicio de sesión con email/contraseña. */
    suspend fun signInWithEmail(email: String, password: String): AuthResult =
        try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            AuthResult.Success
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error(R.string.auth_error_invalid_credentials)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error(R.string.auth_error_invalid_credentials)
        } catch (e: Exception) {
            AuthResult.Error(R.string.auth_error_generic)
        }

    // ─────────────────────────────────────────────────────────────────────
    //  GOOGLE (Credential Manager + Firebase)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Inicia sesión con Google. Requiere un [Context] de UI (Activity) para que
     * Credential Manager pueda mostrar el selector de cuentas.
     *
     * Flujo:
     *   1. GetSignInWithGoogleOption con el serverClientId (Web client ID).
     *   2. credentialManager.getCredential(...) → GoogleIdTokenCredential.
     *   3. GoogleAuthProvider.getCredential(idToken, null) → signInWithCredential.
     */
    suspend fun signInWithGoogle(activityContext: Context): AuthResult {
        val serverClientId = appContext.getString(R.string.default_web_client_id)

        // Sin Web client ID configurado no se puede pedir el ID token.
        if (serverClientId.startsWith("TODO_")) {
            return AuthResult.Error(R.string.auth_error_google_config)
        }

        val googleOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val idToken: String = try {
            val response = credentialManager.getCredential(activityContext, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } else {
                return AuthResult.Error(R.string.auth_error_generic)
            }
        } catch (e: GetCredentialCancellationException) {
            return AuthResult.Error(R.string.auth_error_google_cancelled)
        } catch (e: NoCredentialException) {
            return AuthResult.Error(R.string.auth_error_google_cancelled)
        } catch (e: GetCredentialException) {
            return AuthResult.Error(R.string.auth_error_google_config)
        }

        // Intercambia el ID token de Google por una sesión de Firebase.
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(firebaseCredential).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(R.string.auth_error_generic)
        }
    }
}

/** Resultado de una operación de autenticación. */
sealed class AuthResult {
    /** Autenticación correcta → navegar a Today. */
    object Success : AuthResult()

    /** Falló: [messageRes] es el string a mostrar al usuario. */
    data class Error(val messageRes: Int) : AuthResult()
}
