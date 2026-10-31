package com.example.network

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "YouTubeAuthManager"
private const val PREFS_NAME = "barto_youtube_auth_prefs"
private const val KEY_IS_AUTHORIZED = "is_authorized"
private const val KEY_TOKEN_EXPIRY = "token_expiry"

/**
 * Gestor de autenticación nativa para YouTube Data API v3 utilizando
 * Google Identity Services (AuthorizationClient) y cliente OAuth de tipo Android.
 *
 * Cumple con los requerimientos de seguridad:
 * - NO utiliza el cliente OAuth Web ni redirect URIs con esquemas personalizados.
 * - NO almacena Access Tokens en texto plano en SharedPreferences.
 * - Gestiona el token en memoria durante la sesión activa y utiliza
 *   Identity.getAuthorizationClient para resolución y recuperación de credenciales.
 */
class YouTubeAuthManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PACKAGE_NAME = "com.aistudio.bartoyoutube.qzwkmp"
        const val SHA1_FINGERPRINT = "F7:FF:B9:89:83:91:0A:8D:87:70:E4:CC:42:B5:C3:A5:45:BE:17:7F"
        const val ANDROID_CLIENT_ID = "363588858437-1b2gba4icmt6ggp7p2d8dhve0cbmtojq.apps.googleusercontent.com"

        val YOUTUBE_SCOPES = listOf(
            Scope("https://www.googleapis.com/auth/youtube"),
            Scope("https://www.googleapis.com/auth/youtube.readonly"),
            Scope("https://www.googleapis.com/auth/youtube.upload"),
            Scope("https://www.googleapis.com/auth/yt-analytics.readonly")
        )

        // Token en memoria para la sesión activa (protegido contra almacenamiento en texto plano en disco)
        @Volatile
        private var inMemoryAccessToken: String? = null

        @Volatile
        private var tokenExpiryTimestamp: Long = 0L
    }

    /**
     * Devuelve el Access Token activo en memoria si existe y no ha expirado.
     */
    fun getAccessToken(): String? {
        val token = inMemoryAccessToken
        if (token.isNullOrBlank()) return null
        if (tokenExpiryTimestamp > 0 && System.currentTimeMillis() > tokenExpiryTimestamp) {
            Log.w(TAG, "El Access Token en memoria ha expirado.")
            return null
        }
        return token
    }

    /**
     * Guarda el token únicamente en memoria y persiste únicamente la bandera booleana
     * de estado de autorización en SharedPreferences (nunca el token en texto plano).
     */
    fun saveTokens(accessToken: String, refreshToken: String? = null, expiresInSeconds: Long = 3600) {
        inMemoryAccessToken = accessToken.trim()
        tokenExpiryTimestamp = System.currentTimeMillis() + (expiresInSeconds * 1000)

        // Persistimos únicamente metadatos no sensibles
        prefs.edit()
            .putBoolean(KEY_IS_AUTHORIZED, true)
            .putLong(KEY_TOKEN_EXPIRY, tokenExpiryTimestamp)
            .apply()

        Log.i(TAG, "Access Token guardado en memoria de sesión de forma segura. Validez: $expiresInSeconds s.")
    }

    /**
     * Limpia la sesión en memoria y los metadatos de autorización.
     */
    fun clearTokens() {
        inMemoryAccessToken = null
        tokenExpiryTimestamp = 0L
        prefs.edit()
            .putBoolean(KEY_IS_AUTHORIZED, false)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()
        Log.i(TAG, "Tokens y estado de sesión eliminados.")
    }

    fun hasValidToken(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    fun isAuthorized(): Boolean {
        return hasValidToken() || prefs.getBoolean(KEY_IS_AUTHORIZED, false)
    }

    /**
     * Construye la solicitud de autorización para Google Identity Services (AuthorizationClient)
     * utilizando los scopes oficiales de YouTube.
     */
    fun buildAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.builder()
            .setRequestedScopes(YOUTUBE_SCOPES)
            .build()
    }

    /**
     * Inicia el flujo nativo de autorización de Android mediante AuthorizationClient.
     * Si Google Play Services requiere resolución del usuario (pantalla de consentimiento),
     * emite el PendingIntent para ser lanzado con un ActivityResultLauncher.
     * Si ya estaba autorizado previamente, devuelve directamente el Access Token.
     */
    fun startNativeAuthorization(
        activity: Activity,
        onResolutionRequired: (PendingIntent) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val authClient = Identity.getAuthorizationClient(activity)
        val request = buildAuthorizationRequest()

        authClient.authorize(request)
            .addOnSuccessListener { result: AuthorizationResult ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent
                    if (pendingIntent != null) {
                        Log.i(TAG, "Resolución de autorización requerida por Google Play Services.")
                        onResolutionRequired(pendingIntent)
                    } else {
                        onFailure(IllegalStateException("Google Authorization devolvió hasResolution=true pero el PendingIntent es nulo."))
                    }
                } else {
                    val token = result.accessToken
                    if (!token.isNullOrBlank()) {
                        saveTokens(token)
                        Log.i(TAG, "Autorización completada directamente sin resolución adicional.")
                        onSuccess(token)
                    } else {
                        onFailure(IllegalStateException("No se obtuvo un Access Token en el resultado de autorización."))
                    }
                }
            }
            .addOnFailureListener { exception ->
                val errorMsg = if (exception is ApiException) {
                    "Google Identity API Error [Status ${exception.statusCode}]: ${exception.message}"
                } else {
                    exception.localizedMessage ?: "Error desconocido en AuthorizationClient"
                }
                Log.e(TAG, "Fallo al autorizar con Google Identity Services: $errorMsg", exception)
                onFailure(exception)
            }
    }

    /**
     * Procesa el Intent resultante del ActivityResult de la resolución de consentimiento.
     */
    fun handleAuthorizationResult(
        context: Context,
        data: Intent?,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (data == null) {
            onFailure(IllegalArgumentException("El Intent devuelto por la pantalla de autorización es nulo."))
            return
        }

        try {
            val authClient = Identity.getAuthorizationClient(context)
            val result = authClient.getAuthorizationResultFromIntent(data)
            val token = result.accessToken
            if (!token.isNullOrBlank()) {
                saveTokens(token)
                Log.i(TAG, "Access token nativo obtenido exitosamente desde el resultado de autorización.")
                onSuccess(token)
            } else {
                onFailure(IllegalStateException("El resultado de autorización no contiene un Access Token."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando el resultado de autorización de Google", e)
            onFailure(e)
        }
    }

    /**
     * Intenta recuperar el Access Token de forma silenciosa si ya existía consentimiento.
     */
    suspend fun getOrRefreshAccessTokenSilently(context: Context): Result<String> {
        val current = getAccessToken()
        if (!current.isNullOrBlank()) {
            return Result.success(current)
        }

        return try {
            val authClient = Identity.getAuthorizationClient(context)
            val request = buildAuthorizationRequest()

            val result = suspendCancellableCoroutine<AuthorizationResult> { cont ->
                authClient.authorize(request)
                    .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                    .addOnFailureListener { if (cont.isActive) cont.resumeWith(Result.failure(it)) }
                    .addOnCanceledListener { if (cont.isActive) cont.cancel() }
            }

            if (!result.hasResolution() && !result.accessToken.isNullOrBlank()) {
                saveTokens(result.accessToken!!)
                Result.success(result.accessToken!!)
            } else {
                Result.failure(IllegalStateException("Se requiere consentimiento interactivo del usuario."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
