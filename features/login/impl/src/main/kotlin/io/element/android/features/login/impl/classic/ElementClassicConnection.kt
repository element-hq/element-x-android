/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.classic

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.annotation.VisibleForTesting
import androidx.core.os.BundleCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.login.impl.BuildConfig
import io.element.android.libraries.androidutils.service.ServiceBinder
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.auth.ElementClassicSession
import io.element.android.libraries.matrix.api.auth.HomeServerLoginCompatibilityChecker
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

interface ElementClassicConnection {
    fun start()
    fun stop()
    fun requestSession()
    val stateFlow: StateFlow<ElementClassicConnectionState>
}

sealed interface ElementClassicConnectionState {
    object Idle : ElementClassicConnectionState
    object ElementClassicNotFound : ElementClassicConnectionState
    object ElementClassicReadyNoSession : ElementClassicConnectionState
    data class ElementClassicReady(
        val elementClassicSession: ElementClassicSession,
        val displayName: String?,
        val avatar: Bitmap?,
    ) : ElementClassicConnectionState

    data class Error(val error: String) : ElementClassicConnectionState
}

private val loggerTag = LoggerTag("ECConnection")

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultElementClassicConnection(
    private val serviceBinder: ServiceBinder,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
    private val matrixAuthenticationService: MatrixAuthenticationService,
    private val homeServerLoginCompatibilityChecker: HomeServerLoginCompatibilityChecker,
    private val featureFlagService: FeatureFlagService,
) : ElementClassicConnection {
    // Messenger for communicating with the service.
    private var messenger: Messenger? = null

    // Target we publish for external service to send messages to IncomingHandler.
    private val incomingMessenger: Messenger = Messenger(IncomingHandler())

    // Flag indicating whether we have called bind on the service.
    private var bound: Boolean = false

    private val mutableStateFlow = MutableStateFlow<ElementClassicConnectionState>(ElementClassicConnectionState.Idle)
    override val stateFlow = mutableStateFlow.asStateFlow()

    private val elementClassicComponent = ComponentName(
        BuildConfig.elementClassicPackage,
        ELEMENT_CLASSIC_SERVICE_FULL_CLASS_NAME,
    )

    /**
     * Class for interacting with the main interface of the service.
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Timber.tag(loggerTag.value).d("onServiceConnected")
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            messenger = Messenger(service)
            bound = true
            // Request the data as soon as possible
            requestSession()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Timber.tag(loggerTag.value).d("onServiceDisconnected")
            // This is called when the connection with the service has been
            // unexpectedly disconnected&mdash;that is, its process crashed.
            messenger = null
            bound = false
        }
    }

    override fun start() {
        Timber.tag(loggerTag.value).d("start()")
        coroutineScope.launch {
            if (!featureFlagService.isFeatureEnabled(FeatureFlags.SignInWithClassic)) {
                Timber.tag(loggerTag.value).d("Login with Element Classic is disabled, not starting connection")
                return@launch
            }
            // Establish a connection with the service. We use an explicit
            // class name because there is no reason to be able to let other
            // applications replace our component.
            try {
                val intentService = Intent()
                intentService.setComponent(elementClassicComponent)
                if (serviceBinder.bindService(intentService, serviceConnection, BIND_AUTO_CREATE)) {
                    Timber.tag(loggerTag.value).d("Binding returned true")
                } else {
                    // This happens when the app is not installed
                    Timber.tag(loggerTag.value).d("Binding returned false")
                    emitState(ElementClassicConnectionState.ElementClassicNotFound)
                }
            } catch (e: SecurityException) {
                Timber.tag(loggerTag.value).e(e, "Can't bind to Service")
                emitState(ElementClassicConnectionState.Error(e.localizedMessage.orEmpty()))
            }
        }
    }

    override fun stop() {
        Timber.tag(loggerTag.value).d("stop(): Unbinding (bound=$bound)")
        if (bound) {
            // Detach our existing connection.
            serviceBinder.unbindService(serviceConnection)
            bound = false
        }
        coroutineScope.launch {
            emitState(ElementClassicConnectionState.Idle)
        }
    }

    override fun requestSession() {
        Timber.tag(loggerTag.value).d("requestSession()")
        coroutineScope.launch {
            if (!featureFlagService.isFeatureEnabled(FeatureFlags.SignInWithClassic)) {
                Timber.tag(loggerTag.value).d("Login with Element Classic is disabled")
                emitState(ElementClassicConnectionState.Error("The feature is disabled"))
                return@launch
            }
            val finalMessenger = messenger
            if (finalMessenger == null) {
                Timber.tag(loggerTag.value).d("The messenger is null, can't request data")
                // Do not emit error, else the regular on boarding flow will be displayed
            } else {
                try {
                    // Get the data
                    val msg = Message.obtain(null, MSG_GET_SESSION)
                    msg.replyTo = incomingMessenger
                    finalMessenger.send(msg)
                } catch (e: RemoteException) {
                    // In this case the service has crashed before we could even
                    // do anything with it; we can count on soon being
                    // disconnected (and then reconnected if it can be restarted)
                    // so there is no need to do anything here.
                    Timber.tag(loggerTag.value).e(e, "RemoteException")
                    emitState(ElementClassicConnectionState.Error(e.localizedMessage.orEmpty()))
                }
            }
        }
    }

    private fun requestAvatar(userId: UserId) {
        Timber.tag(loggerTag.value).d("requestAvatar()")
        coroutineScope.launch {
            val finalMessenger = messenger
            if (finalMessenger == null) {
                Timber.tag(loggerTag.value).w("The messenger is null, can't request extra data")
            } else {
                try {
                    // Get the data
                    val msg = Message.obtain(null, MSG_GET_AVATAR)
                    msg.data = Bundle().apply {
                        putString(KEY_USER_ID_STR, userId.value)
                    }
                    msg.replyTo = incomingMessenger
                    finalMessenger.send(msg)
                } catch (e: RemoteException) {
                    // In this case the service has crashed before we could even
                    // do anything with it; we can count on soon being
                    // disconnected (and then reconnected if it can be restarted)
                    // so there is no need to do anything here.
                    Timber.tag(loggerTag.value).e(e, "RemoteException")
                }
            }
        }
    }

    /**
     * Handler of incoming messages from service.
     */
    @Suppress("DEPRECATION")
    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            Timber.tag(loggerTag.value).d("IncomingHandler handling message ${msg.what}")
            when (msg.what) {
                MSG_GET_SESSION -> onSessionReceived(msg.data)
                MSG_GET_AVATAR -> onAvatarReceived(msg.data)
                else -> {
                    Timber.tag(loggerTag.value).w("Received unknown message ${msg.what}")
                    super.handleMessage(msg)
                }
            }
        }
    }

    @VisibleForTesting
    fun onSessionReceived(data: Bundle) {
        // The data must be extracted from the bundle before we launch the coroutine, else the bundle will be emptied
        val state = data.toElementClassicConnectionState()
        coroutineScope.launch {
            val updatedState = ensureHomeserverIsSupported(state)
            emitState(updatedState)
            val userId = (updatedState as? ElementClassicConnectionState.ElementClassicReady)?.elementClassicSession?.userId
            if (userId != null) {
                // Step 2, request the avatar
                requestAvatar(userId)
            }
        }
    }

    @VisibleForTesting
    fun onAvatarReceived(data: Bundle) {
        val currentState = stateFlow.value
        if (currentState is ElementClassicConnectionState.ElementClassicReady) {
            // Check that the userId is still the same
            val userId = data.getString(KEY_USER_ID_STR)
            if (userId != currentState.elementClassicSession.userId.value) {
                Timber.tag(loggerTag.value).w(
                    "Received profile data for userId $userId but current" +
                        " userId is ${currentState.elementClassicSession.userId}, ignoring"
                )
            } else {
                val avatar = BundleCompat.getParcelable(data, KEY_USER_AVATAR_PARCELABLE, Bitmap::class.java)
                // If the avatar is identical to the current one, do not emit a new state to avoid unnecessary recompositions
                // and blink on the avatar image
                if (avatar == null || !avatar.sameAs(currentState.avatar)) {
                    val updatedState = currentState.copy(
                        avatar = avatar,
                    )
                    coroutineScope.launch {
                        emitState(updatedState)
                    }
                }
            }
        } else {
            Timber.tag(loggerTag.value).w("Received profile data but current state is not ElementClassicReady: %s", currentState)
        }
    }

    private suspend fun ensureHomeserverIsSupported(state: ElementClassicConnectionState): ElementClassicConnectionState {
        return if (state is ElementClassicConnectionState.ElementClassicReady) {
            val elementXCanConnect = setOfNotNull(
                // Try with the domain name first
                state.elementClassicSession.userId.domainName?.ensureProtocol(),
                // Then try with the resolved homeserver URL, if provided and distinct
                state.elementClassicSession.homeserverUrl,
            ).any { url ->
                val isCompatible = homeServerLoginCompatibilityChecker.check(url)
                    .onFailure {
                        Timber.tag(loggerTag.value).w(it, "Failed to check compatibility with homeserver: $url")
                    }
                    .getOrNull() == true
                if (isCompatible) {
                    Timber.tag(loggerTag.value).d("Found compatible homeserver URL: %s", url)
                } else {
                    Timber.tag(loggerTag.value).d("Homeserver URL is not compatible: %s", url)
                }
                isCompatible
            }
            if (elementXCanConnect) {
                state
            } else {
                Timber.tag(loggerTag.value).w("Cannot import session because the homeserver is not compatible with Element X")
                ElementClassicConnectionState.Error("The homeserver is not compatible with Element X")
            }
        } else {
            state
        }
    }

    private suspend fun emitState(state: ElementClassicConnectionState) {
        when (state) {
            is ElementClassicConnectionState.Error -> {
                Timber.tag(loggerTag.value).w("Error: %s", state.error)
            }
            is ElementClassicConnectionState.ElementClassicReady -> {
                Timber.tag(loggerTag.value).d("Ready state for user: %s", state.elementClassicSession.userId)
            }
            ElementClassicConnectionState.ElementClassicReadyNoSession -> {
                Timber.tag(loggerTag.value).d("No session from Element Classic")
            }
            ElementClassicConnectionState.ElementClassicNotFound -> {
                Timber.tag(loggerTag.value).d("Element Classic not found")
            }
            ElementClassicConnectionState.Idle -> {
                Timber.tag(loggerTag.value).d("Idle")
            }
        }
        // Also give the Element Classic session info to the MatrixAuthenticationService
        matrixAuthenticationService.setElementClassicSession(
            session = (state as? ElementClassicConnectionState.ElementClassicReady)?.elementClassicSession
        )
        mutableStateFlow.emit(state)
    }

    private fun Bundle.toElementClassicConnectionState(): ElementClassicConnectionState {
        val error = getString(KEY_ERROR_STR)
        return if (error != null) {
            ElementClassicConnectionState.Error(error)
        } else {
            val userId = getString(KEY_USER_ID_STR)?.takeIf { it.isNotEmpty() }?.let(::UserId)
            if (userId == null) {
                ElementClassicConnectionState.ElementClassicReadyNoSession
            } else {
                var secrets = getString(KEY_SECRETS_STR)?.takeIf { it.isNotEmpty() }
                val roomKeysVersion = getString(KEY_ROOM_KEYS_VERSION_STR)
                    .also {
                        if (secrets != null && it == null) {
                            Timber.tag(loggerTag.value).w("Room keys version is null, outdated version of Element Classic, ignore secrets")
                            // In this case, just ignore the secrets, the SDK will not accept them anyway
                            secrets = null
                        }
                    }
                    ?.takeIf { it.isNotEmpty() }
                val homeserverUrl = getString(KEY_HOMESERVER_URL_STR)?.takeIf { it.isNotEmpty() }
                val displayName = getString(KEY_USER_DISPLAY_NAME_STR)?.takeIf { it.isNotEmpty() }
                val doesContainBackupKey = secrets != null &&
                    roomKeysVersion != null &&
                    matrixAuthenticationService.doSecretsContainBackupKey(userId, secrets, roomKeysVersion)
                Timber.tag(loggerTag.value).d(
                    buildString {
                        append("Receiving session $userId ($displayName) from Element Classic, with secrets: ")
                        append(secrets != null)
                        append(", with roomKeysVersion: ")
                        append(roomKeysVersion != null)
                        append(", with valid backup key: ")
                        append(doesContainBackupKey)
                    }
                )
                // Ensure avatar is not lost when refreshing the data
                val currentAvatar = (stateFlow.value as? ElementClassicConnectionState.ElementClassicReady)
                    ?.takeIf { it.elementClassicSession.userId == userId }
                    ?.avatar
                ElementClassicConnectionState.ElementClassicReady(
                    elementClassicSession = ElementClassicSession(
                        userId = userId,
                        homeserverUrl = homeserverUrl,
                        secrets = secrets,
                        roomKeysVersion = roomKeysVersion,
                        doesContainBackupKey = doesContainBackupKey,
                    ),
                    displayName = displayName,
                    avatar = currentAvatar,
                )
            }
        }
    }

    // Everything in this companion object must match what is defined in Element Classic
    companion object {
        const val ELEMENT_CLASSIC_SERVICE_FULL_CLASS_NAME = "im.vector.app.features.importer.ImporterService"

        // Command to the service to get the userId/displayName/secrets of a verified session.
        const val MSG_GET_SESSION = 1

        // Command to the service to get the avatar oor the session.
        const val MSG_GET_AVATAR = 2

        // Keys for the bundle returned from the service
        const val KEY_ERROR_STR = "error"
        const val KEY_USER_ID_STR = "userId"
        const val KEY_HOMESERVER_URL_STR = "homeserverUrl"
        const val KEY_USER_DISPLAY_NAME_STR = "displayName"

        /**
         * Key to extract the secrets from the bundle, as a Json string.
         * Json will have this format:
         * {
         *   "cross_signing" : {
         *     "master_key" : "z8RUxnaAGu___REDACTED___k+BQL9o",
         *     "user_signing_key" : "baJHzA___REDACTED___xMLbSUAXw9QUzqms",
         *     "self_signing_key" : "DU0CvLtR2G/___REDACTED___dV/MONNq4nsQhM"
         *   },
         *   "backup" : {
         *     "algorithm" : "m.megolm_backup.v1.curve25519-aes-sha2",
         *     "key" : "VzncmQ+UOV___REDACTED___patxDz7m0Nc",
         *     "backup_version" : "1"
         *   }
         * }
         */
        const val KEY_SECRETS_STR = "secrets"
        const val KEY_ROOM_KEYS_VERSION_STR = "roomKeysVersion"

        // For the avatar
        const val KEY_USER_AVATAR_PARCELABLE = "avatar"
    }
}
