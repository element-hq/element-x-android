package io.element.android.x.sdk.matrix

import android.util.Log
import uniffi.matrix_sdk_ffi.Client
import uniffi.matrix_sdk_ffi.ClientDelegate

class MatrixClient internal constructor(
    private val client: Client
) {
    fun startSync() {
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
    }

    fun logout() {
        client.logout()
    }
}
