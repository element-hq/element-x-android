package io.element.android.x.sdk.matrix

import android.content.Context
import android.util.Log
import java.io.File

private const val LOG_TAG = "Matrix"

class Matrix(
    private val context: Context,
) {
    fun login(username: String, password: String) {
        val authFolder = File(context.filesDir, "auth")
        val authService = AuthenticationService(authFolder.absolutePath)
        authService.configureHomeserver("matrix.org")
        val client = authService.login(username, password, "MatrixRustSDKSample", null)
        val clientDelegate = object : ClientDelegate {
            override fun didReceiveAuthError(isSoftLogout: Boolean) {
                Log.v(LOG_TAG, "didReceiveAuthError()")
            }

            override fun didReceiveSyncUpdate() {
                Log.v(LOG_TAG, "didReceiveSyncUpdate()")
            }

            override fun didUpdateRestoreToken() {
                Log.v(LOG_TAG, "didUpdateRestoreToken()")
            }
        }

        client.setDelegate(clientDelegate)
        Log.v(LOG_TAG, "DisplayName = ${client.displayName()}")
        try {
            client.fullSlidingSync()
        } catch (failure: Throwable) {
            Log.e(LOG_TAG, "fullSlidingSync() fail", failure)
        }
        client.logout()
    }
}