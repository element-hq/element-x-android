/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.metadata.withReleaseBehavior
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.pusher.SetHttpPusherData
import io.element.android.libraries.matrix.api.pusher.UnsetHttpPusherData
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.pushers.FakePushersService
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultPusherSubscriberTest {
    @Test
    fun `test register pusher OK`() = runTest {
        testRegisterPusher(
            currentPushKey = null,
            registerResult = Result.success(Unit),
        )
    }

    @Test
    fun `test re-register pusher OK`() = runTest {
        testRegisterPusher(
            currentPushKey = "aPushKey",
            registerResult = Result.success(Unit),
        )
    }

    @Test
    fun `test register pusher error`() = runTest {
        testRegisterPusher(
            currentPushKey = null,
            registerResult = Result.failure(AN_EXCEPTION),
        )
    }

    @Test
    fun `test re-register pusher error`() = runTest {
        testRegisterPusher(
            currentPushKey = "aPushKey",
            registerResult = Result.failure(AN_EXCEPTION),
        )
    }

    @Test
    fun `test register pusher with another session on the same server using the same push key uses append true`() = runTest {
        testRegisterPusher(
            currentPushKey = null,
            registerResult = Result.success(Unit),
            otherSessionPushKeys = mapOf(A_SESSION_ID_2 to "aPushKey"),
            expectedAppend = true,
        )
    }

    @Test
    fun `test register pusher with another session on the same server using another push key uses append false`() = runTest {
        testRegisterPusher(
            currentPushKey = null,
            registerResult = Result.success(Unit),
            otherSessionPushKeys = mapOf(A_SESSION_ID_2 to "anotherPushKey"),
            expectedAppend = false,
        )
    }

    @Test
    fun `test register pusher with another session on another server using the same push key uses append false`() = runTest {
        testRegisterPusher(
            currentPushKey = null,
            registerResult = Result.success(Unit),
            otherSessionPushKeys = mapOf(SessionId("@bob:other.org") to "aPushKey"),
            expectedAppend = false,
        )
    }

    @Test
    fun `test register pusher for a session without server name uses append false`() = runTest {
        var sessionIdWithoutServerName = A_SESSION_ID
        withReleaseBehavior {
            sessionIdWithoutServerName = SessionId("@alice:")
        }
        testRegisterPusher(
            currentPushKey = null,
            registerResult = Result.success(Unit),
            sessionId = sessionIdWithoutServerName,
            otherSessionPushKeys = mapOf(A_SESSION_ID_2 to "aPushKey"),
            expectedAppend = false,
        )
    }

    private suspend fun testRegisterPusher(
        currentPushKey: String?,
        registerResult: Result<Unit>,
        sessionId: SessionId = A_SESSION_ID,
        otherSessionPushKeys: Map<SessionId, String?> = emptyMap(),
        expectedAppend: Boolean = false,
    ) {
        val setHttpPusherResult = lambdaRecorder<SetHttpPusherData, Result<Unit>> { registerResult }
        val userPushStore = FakeUserPushStore().apply {
            setCurrentRegisteredPushKey(currentPushKey)
        }
        assertThat(userPushStore.getCurrentRegisteredPushKey()).isEqualTo(currentPushKey)
        val otherUserPushStores = otherSessionPushKeys.mapValues { (_, pushKey) ->
            FakeUserPushStore().apply {
                setCurrentRegisteredPushKey(pushKey)
            }
        }

        val matrixClient = FakeMatrixClient(
            sessionId = sessionId,
            pushersService = FakePushersService(
                setHttpPusherResult = setHttpPusherResult,
            ),
        )
        val defaultPusherSubscriber = createDefaultPusherSubscriber(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET },
            ),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { otherUserPushStores[it] ?: userPushStore },
            ),
            sessionStore = InMemorySessionStore(
                initialList = (listOf(sessionId) + otherSessionPushKeys.keys).map {
                    aSessionData(sessionId = it.value)
                },
            ),
        )
        val result = defaultPusherSubscriber.registerPusher(
            matrixClient = matrixClient,
            pushKey = "aPushKey",
            gateway = "aGateway",
        )
        assertThat(result).isEqualTo(registerResult)
        setHttpPusherResult.assertions()
            .isCalledOnce()
            .with(
                value(
                    SetHttpPusherData(
                        pushKey = "aPushKey",
                        appId = BuildConfig.PUSHER_APP_ID,
                        url = "aGateway",
                        appDisplayName = "MyApp",
                        deviceDisplayName = "MyDevice",
                        profileTag = null,
                        lang = "en",
                        defaultPayload = """{"cs":"$A_SECRET"}""",
                        append = expectedAppend,
                    ),
                )
            )
        assertThat(userPushStore.getCurrentRegisteredPushKey()).isEqualTo(
            if (registerResult.isSuccess) "aPushKey" else currentPushKey
        )
    }

    @Test
    fun `test unregister pusher OK`() = runTest {
        testUnregisterPusher(
            currentPushKey = "aPushKey",
            unregisterResult = Result.success(Unit),
        )
    }

    @Test
    fun `test unregister pusher error`() = runTest {
        testUnregisterPusher(
            currentPushKey = "aPushKey",
            unregisterResult = Result.failure(AN_EXCEPTION),
        )
    }

    private suspend fun testUnregisterPusher(
        currentPushKey: String?,
        unregisterResult: Result<Unit>,
    ) {
        val unsetHttpPusherResult = lambdaRecorder<UnsetHttpPusherData, Result<Unit>> { unregisterResult }
        val userPushStore = FakeUserPushStore().apply {
            setCurrentRegisteredPushKey(currentPushKey)
        }
        assertThat(userPushStore.getCurrentRegisteredPushKey()).isEqualTo(currentPushKey)

        val matrixClient = FakeMatrixClient(
            pushersService = FakePushersService(
                unsetHttpPusherResult = unsetHttpPusherResult,
            ),
        )
        val defaultPusherSubscriber = createDefaultPusherSubscriber(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET },
            ),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { userPushStore },
            ),
        )
        val result = defaultPusherSubscriber.unregisterPusher(
            matrixClient = matrixClient,
            pushKey = "aPushKey",
            gateway = "aGateway",
        )
        assertThat(result).isEqualTo(unregisterResult)
        unsetHttpPusherResult.assertions()
            .isCalledOnce()
            .with(
                value(
                    UnsetHttpPusherData(
                        pushKey = "aPushKey",
                        appId = BuildConfig.PUSHER_APP_ID,
                    ),
                )
            )
        assertThat(userPushStore.getCurrentRegisteredPushKey()).isEqualTo(
            if (unregisterResult.isSuccess) null else currentPushKey
        )
    }

    private fun createDefaultPusherSubscriber(
        buildMeta: BuildMeta = aBuildMeta(applicationName = "MyApp"),
        userPushStoreFactory: UserPushStoreFactory = FakeUserPushStoreFactory(),
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        sessionStore: SessionStore = InMemorySessionStore(),
    ): DefaultPusherSubscriber {
        return DefaultPusherSubscriber(
            buildMeta = buildMeta,
            pushClientSecret = pushClientSecret,
            userPushStoreFactory = userPushStoreFactory,
            sessionStore = sessionStore,
        )
    }
}
