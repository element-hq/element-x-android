/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.login.impl.classic

import android.graphics.Bitmap
import android.os.Bundle
import androidx.core.graphics.createBitmap
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.service.ServiceBinder
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.auth.ElementClassicSession
import io.element.android.libraries.matrix.api.auth.HomeServerLoginCompatibilityChecker
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.test.A_FAILURE_REASON
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.auth.FakeHomeServerLoginCompatibilityChecker
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultElementClassicConnectionTest {
    @Test
    fun `connection can be started Element Classic service can be bound`() = runTest {
        val connection = createDefaultElementClassicConnection(
            serviceBinder = FakeServiceBinder(
                bindServiceResult = {
                    // Element Classic is found
                    true
                },
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            connection.start()
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun `connection can be started Element Classic service cannot be bound`() = runTest {
        val setElementClassicSessionResult = lambdaRecorder<ElementClassicSession?, Unit> { }
        val connection = createDefaultElementClassicConnection(
            serviceBinder = FakeServiceBinder(
                bindServiceResult = {
                    // Element Classic not found
                    false
                },
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = setElementClassicSessionResult,
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            connection.start()
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.ElementClassicNotFound)
            setElementClassicSessionResult.assertions().isCalledOnce().with(value(null))
        }
    }

    @Test
    fun `connection cannot be started in case of security error`() = runTest {
        val setElementClassicSessionResult = lambdaRecorder<ElementClassicSession?, Unit> { }
        val connection = createDefaultElementClassicConnection(
            serviceBinder = FakeServiceBinder(
                bindServiceResult = { throw SecurityException(A_FAILURE_REASON) },
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = setElementClassicSessionResult,
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            connection.start()
            assertThat(awaitItem()).isInstanceOf(ElementClassicConnectionState.Error::class.java)
            setElementClassicSessionResult.assertions().isCalledOnce().with(value(null))
        }
    }

    @Test
    fun `requestSession when messenger is not ready has no effect`() = runTest {
        val connection = createDefaultElementClassicConnection()
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            connection.requestSession()
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun `requestSession when the feature is disabled emits an error`() = runTest {
        val connection = createDefaultElementClassicConnection(
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
            isFeatureEnabled = false,
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            connection.requestSession()
            assertThat(awaitItem()).isInstanceOf(ElementClassicConnectionState.Error::class.java)
        }
    }

    @Test
    fun `when an error is received, an error is emitted`() = runTest {
        val connection = createDefaultElementClassicConnection(
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving a session from Element Classic
            connection.onSessionReceived(
                Bundle().apply {
                    putString(DefaultElementClassicConnection.KEY_ERROR_STR, A_FAILURE_REASON)
                }
            )
            assertThat(awaitItem()).isInstanceOf(ElementClassicConnectionState.Error::class.java)
        }
    }

    @Test
    fun `when there is no Element Classic session, ElementClassicReadyNoSession is emitted`() = runTest {
        val connection = createDefaultElementClassicConnection(
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving no session from Element Classic
            connection.onSessionReceived(Bundle())
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.ElementClassicReadyNoSession)
        }
    }

    @Test
    fun `when there is Element Classic session with empty userId, ElementClassicReadyNoSession is emitted`() = runTest {
        val connection = createDefaultElementClassicConnection(
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving empty userId from Element Classic
            connection.onSessionReceived(Bundle().apply {
                putString(DefaultElementClassicConnection.KEY_USER_ID_STR, "")
            })
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.ElementClassicReadyNoSession)
        }
    }

    @Test
    fun `when session is received, but homeserver is not supported, an error is emitted`() = runTest {
        val connection = createDefaultElementClassicConnection(
            homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
                checkResult = { Result.success(false) }
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving a session from Element Classic
            connection.onSessionReceived(
                Bundle().apply {
                    putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                }
            )
            assertThat(awaitItem()).isInstanceOf(ElementClassicConnectionState.Error::class.java)
        }
    }

    @Test
    fun `when session is received without secrets, and homeserver is supported, ElementClassicReady is emitted`() = runTest {
        val connection = createDefaultElementClassicConnection(
            homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
                checkResult = { Result.success(true) }
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving a session from Element Classic
            connection.onSessionReceived(
                Bundle().apply {
                    putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                }
            )
            assertThat(awaitItem()).isEqualTo(
                ElementClassicConnectionState.ElementClassicReady(
                    elementClassicSession = ElementClassicSession(
                        userId = A_USER_ID,
                        homeserverUrl = null,
                        secrets = null,
                        roomKeysVersion = null,
                        doesContainBackupKey = false,
                    ),
                    displayName = null,
                    avatar = null,
                )
            )
        }
    }

    @Test
    fun `when session is received with all data including key backup, and homeserver is supported, ElementClassicReady is emitted`() {
        `when session is received with all data, and homeserver is supported, ElementClassicReady is emitted`(
            withKeyBackup = true,
        )
    }

    @Test
    fun `when session is received with all data without key backup, and homeserver is supported, ElementClassicReady is emitted - backup key is missing`() {
        `when session is received with all data, and homeserver is supported, ElementClassicReady is emitted`(
            withKeyBackup = false,
        )
    }

    private fun `when session is received with all data, and homeserver is supported, ElementClassicReady is emitted`(
        withKeyBackup: Boolean,
    ) = runTest {
        val connection = createDefaultElementClassicConnection(
            homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
                checkResult = { Result.success(true) }
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
                doSecretsContainBackupKeyResult = { _, _, _ -> withKeyBackup },
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving a session from Element Classic
            connection.onSessionReceived(
                Bundle().apply {
                    putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                    putString(DefaultElementClassicConnection.KEY_HOMESERVER_URL_STR, A_HOMESERVER_URL)
                    putString(DefaultElementClassicConnection.KEY_SECRETS_STR, A_SECRET)
                    putString(DefaultElementClassicConnection.KEY_ROOM_KEYS_VERSION_STR, ROOM_KEYS_VERSION)
                    putString(DefaultElementClassicConnection.KEY_USER_DISPLAY_NAME_STR, A_USER_NAME)
                }
            )
            assertThat(awaitItem()).isEqualTo(
                ElementClassicConnectionState.ElementClassicReady(
                    elementClassicSession = ElementClassicSession(
                        userId = A_USER_ID,
                        homeserverUrl = A_HOMESERVER_URL,
                        secrets = A_SECRET,
                        roomKeysVersion = ROOM_KEYS_VERSION,
                        doesContainBackupKey = withKeyBackup,
                    ),
                    displayName = A_USER_NAME,
                    avatar = null,
                )
            )
        }
    }

    @Test
    fun `when session is received with secret but without room keys version Element Classic is outdated and the secret is ignored`() = runTest {
        val connection = createDefaultElementClassicConnection(
            homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
                checkResult = { Result.success(true) }
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving a session from Element Classic
            connection.onSessionReceived(
                Bundle().apply {
                    putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                    putString(DefaultElementClassicConnection.KEY_HOMESERVER_URL_STR, A_HOMESERVER_URL)
                    putString(DefaultElementClassicConnection.KEY_SECRETS_STR, A_SECRET)
                    putString(DefaultElementClassicConnection.KEY_ROOM_KEYS_VERSION_STR, null)
                    putString(DefaultElementClassicConnection.KEY_USER_DISPLAY_NAME_STR, A_USER_NAME)
                }
            )
            assertThat(awaitItem()).isEqualTo(
                ElementClassicConnectionState.ElementClassicReady(
                    elementClassicSession = ElementClassicSession(
                        userId = A_USER_ID,
                        homeserverUrl = A_HOMESERVER_URL,
                        secrets = null,
                        roomKeysVersion = null,
                        doesContainBackupKey = false,
                    ),
                    displayName = A_USER_NAME,
                    avatar = null,
                )
            )
        }
    }

    @Test
    fun `when session is received with secret but with empty room keys version, doesContainBackupKey is false`() = runTest {
        val connection = createDefaultElementClassicConnection(
            homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
                checkResult = { Result.success(true) }
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving a session from Element Classic
            connection.onSessionReceived(
                Bundle().apply {
                    putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                    putString(DefaultElementClassicConnection.KEY_HOMESERVER_URL_STR, A_HOMESERVER_URL)
                    putString(DefaultElementClassicConnection.KEY_SECRETS_STR, A_SECRET)
                    putString(DefaultElementClassicConnection.KEY_ROOM_KEYS_VERSION_STR, "")
                    putString(DefaultElementClassicConnection.KEY_USER_DISPLAY_NAME_STR, A_USER_NAME)
                }
            )
            assertThat(awaitItem()).isEqualTo(
                ElementClassicConnectionState.ElementClassicReady(
                    elementClassicSession = ElementClassicSession(
                        userId = A_USER_ID,
                        homeserverUrl = A_HOMESERVER_URL,
                        secrets = A_SECRET,
                        roomKeysVersion = null,
                        doesContainBackupKey = false,
                    ),
                    displayName = A_USER_NAME,
                    avatar = null,
                )
            )
        }
    }

    @Test
    fun `when session is received with empty data, and homeserver is supported, ElementClassicReady is emitted`() = runTest {
        val connection = createDefaultElementClassicConnection(
            homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
                checkResult = { Result.success(true) }
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving a session from Element Classic
            connection.onSessionReceived(
                Bundle().apply {
                    putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                    putString(DefaultElementClassicConnection.KEY_HOMESERVER_URL_STR, "")
                    putString(DefaultElementClassicConnection.KEY_SECRETS_STR, "")
                    putString(DefaultElementClassicConnection.KEY_USER_DISPLAY_NAME_STR, "")
                }
            )
            assertThat(awaitItem()).isEqualTo(
                ElementClassicConnectionState.ElementClassicReady(
                    elementClassicSession = ElementClassicSession(
                        userId = A_USER_ID,
                        homeserverUrl = null,
                        secrets = null,
                        roomKeysVersion = null,
                        doesContainBackupKey = false,
                    ),
                    displayName = null,
                    avatar = null,
                )
            )
        }
    }

    @Test
    fun `when avatar is received when the state is not ElementClassicReady, nothing happen`() = runTest {
        val connection = createDefaultElementClassicConnection(
            homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
                checkResult = { Result.success(true) }
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving an avatar from Element Classic
            connection.onAvatarReceived(Bundle().apply {
                putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                putParcelable(DefaultElementClassicConnection.KEY_USER_AVATAR_PARCELABLE, createBitmap(1, 1, Bitmap.Config.ARGB_8888))
            })
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun `when avatar is received when the state is ElementClassicReady with a different user, nothing happen`() = runTest {
        val connection = createDefaultElementClassicConnection(
            homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
                checkResult = { Result.success(true) }
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving a session from Element Classic
            connection.onSessionReceived(
                Bundle().apply {
                    putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                }
            )
            assertThat(awaitItem()).isEqualTo(
                ElementClassicConnectionState.ElementClassicReady(
                    elementClassicSession = ElementClassicSession(
                        userId = A_USER_ID,
                        homeserverUrl = null,
                        secrets = null,
                        roomKeysVersion = null,
                        doesContainBackupKey = false,
                    ),
                    displayName = null,
                    avatar = null,
                )
            )
            // Simulate receiving an avatar for another user from Element Classic
            connection.onAvatarReceived(Bundle().apply {
                putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID_2.value)
                putParcelable(DefaultElementClassicConnection.KEY_USER_AVATAR_PARCELABLE, createBitmap(1, 1, Bitmap.Config.ARGB_8888))
            })
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun `when avatar is received, the state is updated`() = runTest {
        val connection = createDefaultElementClassicConnection(
            homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
                checkResult = { Result.success(true) }
            ),
            matrixAuthenticationService = FakeMatrixAuthenticationService(
                setElementClassicSessionResult = {},
            ),
        )
        connection.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(ElementClassicConnectionState.Idle)
            // Simulate receiving a session from Element Classic
            connection.onSessionReceived(
                Bundle().apply {
                    putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                }
            )
            assertThat(awaitItem()).isEqualTo(
                ElementClassicConnectionState.ElementClassicReady(
                    elementClassicSession = ElementClassicSession(
                        userId = A_USER_ID,
                        homeserverUrl = null,
                        secrets = null,
                        roomKeysVersion = null,
                        doesContainBackupKey = false,
                    ),
                    displayName = null,
                    avatar = null,
                )
            )
            // Simulate receiving an avatar from Element Classic
            connection.onAvatarReceived(Bundle().apply {
                putString(DefaultElementClassicConnection.KEY_USER_ID_STR, A_USER_ID.value)
                putParcelable(DefaultElementClassicConnection.KEY_USER_AVATAR_PARCELABLE, createBitmap(1, 1, Bitmap.Config.ARGB_8888))
            })
            assertThat((awaitItem() as? ElementClassicConnectionState.ElementClassicReady)?.avatar).isNotNull()
        }
    }

    private fun TestScope.createDefaultElementClassicConnection(
        serviceBinder: ServiceBinder = FakeServiceBinder(
            bindServiceResult = { true },
            unbindServiceResult = { },
        ),
        coroutineScope: CoroutineScope = backgroundScope,
        matrixAuthenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
        homeServerLoginCompatibilityChecker: HomeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(
            checkResult = { Result.success(true) }
        ),
        isFeatureEnabled: Boolean = true,
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(
            initialState = mapOf(
                FeatureFlags.SignInWithClassic.key to isFeatureEnabled,
            )
        ),
    ) = DefaultElementClassicConnection(
        serviceBinder = serviceBinder,
        coroutineScope = coroutineScope,
        matrixAuthenticationService = matrixAuthenticationService,
        homeServerLoginCompatibilityChecker = homeServerLoginCompatibilityChecker,
        featureFlagService = featureFlagService,
    )
}
