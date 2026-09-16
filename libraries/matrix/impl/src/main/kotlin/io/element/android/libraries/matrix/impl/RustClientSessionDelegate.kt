/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.impl.core.SdkBackgroundTaskError
import io.element.android.libraries.matrix.impl.mapper.toSessionData
import io.element.android.libraries.matrix.impl.paths.getSessionPaths
import io.element.android.libraries.matrix.impl.util.anonymizedTokens
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.ClientSessionDelegate
import org.matrix.rustcomponents.sdk.Session
import timber.log.Timber
import uniffi.matrix_sdk_common.BackgroundTaskFailureReason
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

private val loggerTag = LoggerTag("RustClientSessionDelegate")

/**
 * This class is responsible for handling the session data for the Rust SDK.
 *
 * It implements both [ClientSessionDelegate] and [ClientDelegate] to react to session data updates and auth errors.
 *
 * IMPORTANT: you must set the [client] property as soon as possible so [didReceiveAuthError] can work properly.
 */
class RustClientSessionDelegate(
    private val sessionStore: SessionStore,
    private val appCoroutineScope: CoroutineScope,
    private val analyticsService: AnalyticsService,
) : ClientSessionDelegate, ClientDelegate {
    // Used to ensure several calls to `didReceiveAuthError` don't trigger multiple logouts
    private val isLoggingOut = AtomicBoolean(false)

    // This Client needs to be set up as soon as possible so `didReceiveAuthError` can work properly.
    private var client: WeakReference<RustMatrixClient> = WeakReference(null)

    /**
     * Sets the [ClientDelegate] for the [RustMatrixClient], and keeps a reference to the client so it can be used later.
     */
    fun bindClient(client: RustMatrixClient) {
        this.client = WeakReference(client)
    }

    /**
     * Clears the current client reference.
     */
    fun clearCurrentClient() {
        this.client.clear()
    }

    // This always runs on a background thread, so we *can* do blocking calls here, although we should avoid doing heavy work
    override fun saveSessionInKeychain(session: Session) {
        Timber.tag(loggerTag.value).i("Saving new session info for user ${session.userId} after a token refresh")
        runCatchingExceptions {
            val existingData = runBlocking { sessionStore.getSession(session.userId) } ?: return

            if (existingData.accessToken == session.accessToken) {
                Timber.tag(loggerTag.value).e("Access token is the same as the one already stored, this should not happen after a token refresh!")
                return
            }

            if (existingData.refreshToken == session.refreshToken) {
                Timber.tag(loggerTag.value).e("Refresh token is the same as the one already stored, this should not happen after a token refresh!")
                return
            }

            val (anonymizedAccessToken, anonymizedRefreshToken) = session.anonymizedTokens()
            Timber.tag(loggerTag.value).i(
                "Saving new session data with token: access token '$anonymizedAccessToken' and refresh token '$anonymizedRefreshToken'. " +
                    "Was token valid: ${existingData.isTokenValid}"
            )
            val newData = session.toSessionData(
                isTokenValid = true,
                loginType = existingData.loginType,
                passphrase = existingData.passphrase,
                sessionPaths = existingData.getSessionPaths(),
            )
            runBlocking { sessionStore.updateData(newData) }
            Timber.tag(loggerTag.value).i("Saved new session data.")
        }.onFailure {
            Timber.tag(loggerTag.value).e(it, "Failed to save new session data.")
        }
    }

    // This always runs on a background thread, so we *can* do blocking calls here, although we should avoid doing heavy work
    override fun didReceiveAuthError(isSoftLogout: Boolean) {
        runCatchingExceptions {
            Timber.tag(loggerTag.value).w("didReceiveAuthError(isSoftLogout=$isSoftLogout)")
            if (isLoggingOut.getAndSet(true).not()) {
                Timber.tag(loggerTag.value).v("didReceiveAuthError -> do the cleanup")
                // TODO handle isSoftLogout parameter.
                val currentClient = client.get()
                if (currentClient == null) {
                    Timber.tag(loggerTag.value).w("didReceiveAuthError -> no client, exiting")
                    isLoggingOut.set(false)
                    return
                }
                val existingData = runBlocking { sessionStore.getSession(currentClient.sessionId.value) }
                val (anonymizedAccessToken, anonymizedRefreshToken) = existingData.anonymizedTokens()
                Timber.tag(loggerTag.value).d(
                    "Removing session data with access token '$anonymizedAccessToken' " +
                        "and refresh token '$anonymizedRefreshToken'."
                )
                if (existingData != null) {
                    // Set isTokenValid to false
                    val newData = existingData.copy(isTokenValid = false)
                    runBlocking { sessionStore.updateData(newData) }
                    Timber.tag(loggerTag.value).d("Invalidated session data with access token: '$anonymizedAccessToken'.")
                } else {
                    Timber.tag(loggerTag.value).d("No session data found.")
                }
                appCoroutineScope.launch { currentClient.logout(userInitiated = false, ignoreSdkError = true) }
            } else {
                Timber.tag(loggerTag.value).v("didReceiveAuthError -> already cleaning up")
            }
        }.onFailure {
            Timber.tag(loggerTag.value).e(it, "Failed to remove session data.")
        }
    }

    override fun onBackgroundTaskErrorReport(taskName: String, error: BackgroundTaskFailureReason) {
        val backgroundTaskError = SdkBackgroundTaskError(taskName, error)
        Timber.e(backgroundTaskError, "SDK background task failed")
        analyticsService.trackError(backgroundTaskError)

        if (error is BackgroundTaskFailureReason.Panic) {
            appCoroutineScope.launch {
                // The SDK failed in an unrecoverable way, so it will have indeterminate behaviour now.
                // Crash the app instead after a small delay to send the error.
                delay(500.milliseconds)
                throw backgroundTaskError
            }
        }
    }

    override fun retrieveSessionFromKeychain(userId: String): Session {
        // This should never be called, as it's only used for multi-process setups
        error("retrieveSessionFromKeychain should never be called for Android")
    }
}
