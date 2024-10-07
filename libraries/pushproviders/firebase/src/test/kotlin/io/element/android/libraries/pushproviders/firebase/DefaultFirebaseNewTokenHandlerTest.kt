/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.push.test.FakePusherSubscriber
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.impl.memory.InMemoryMultiSessionsStore
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFirebaseNewTokenHandlerTest {
    @Test
    fun `when a new token is received it is stored in the firebase store`() = runTest {
        val firebaseStore = InMemoryFirebaseStore()
        assertThat(firebaseStore.getFcmToken()).isNull()
        val firebaseNewTokenHandler = createDefaultFirebaseNewTokenHandler(
            firebaseStore = firebaseStore,
        )
        firebaseNewTokenHandler.handle("aToken")
        assertThat(firebaseStore.getFcmToken()).isEqualTo("aToken")
    }

    @Test
    fun `when a new token is received, the handler registers the pusher again to all sessions using Firebase`() = runTest {
        val aMatrixClient1 = FakeMatrixClient(A_USER_ID)
        val aMatrixClient2 = FakeMatrixClient(A_USER_ID_2)
        val aMatrixClient3 = FakeMatrixClient(A_USER_ID_3)
        val registerPusherResult = lambdaRecorder<MatrixClient, String, String, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val pusherSubscriber = FakePusherSubscriber(registerPusherResult = registerPusherResult)
        val firebaseNewTokenHandler = createDefaultFirebaseNewTokenHandler(
            sessionStore = InMemoryMultiSessionsStore().apply {
                storeData(aSessionData(A_USER_ID.value))
                storeData(aSessionData(A_USER_ID_2.value))
                storeData(aSessionData(A_USER_ID_3.value))
            },
            matrixClientProvider = FakeMatrixClientProvider { sessionId ->
                when (sessionId) {
                    A_USER_ID -> Result.success(aMatrixClient1)
                    A_USER_ID_2 -> Result.success(aMatrixClient2)
                    A_USER_ID_3 -> Result.success(aMatrixClient3)
                    else -> Result.failure(IllegalStateException())
                }
            },
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { sessionId ->
                    when (sessionId) {
                        A_USER_ID -> FakeUserPushStore(pushProviderName = FirebaseConfig.NAME)
                        A_USER_ID_2 -> FakeUserPushStore(pushProviderName = "Other")
                        A_USER_ID_3 -> FakeUserPushStore(pushProviderName = FirebaseConfig.NAME)
                        else -> error("Unexpected sessionId: $sessionId")
                    }
                }
            ),
            pusherSubscriber = pusherSubscriber,
        )
        firebaseNewTokenHandler.handle("aToken")
        registerPusherResult.assertions()
            .isCalledExactly(2)
            .withSequence(
                listOf(value(aMatrixClient1), value("aToken"), value(FirebaseConfig.PUSHER_HTTP_URL)),
                listOf(value(aMatrixClient3), value("aToken"), value(FirebaseConfig.PUSHER_HTTP_URL)),
            )
    }

    @Test
    fun `when a new token is received, if the session cannot be restore, nothing happen`() = runTest {
        val registerPusherResult = lambdaRecorder<MatrixClient, String, String, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val pusherSubscriber = FakePusherSubscriber(registerPusherResult = registerPusherResult)
        val firebaseNewTokenHandler = createDefaultFirebaseNewTokenHandler(
            sessionStore = InMemoryMultiSessionsStore().apply {
                storeData(aSessionData(A_USER_ID.value))
            },
            matrixClientProvider = FakeMatrixClientProvider {
                Result.failure(IllegalStateException())
            },
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { _ ->
                    FakeUserPushStore(pushProviderName = FirebaseConfig.NAME)
                }
            ),
            pusherSubscriber = pusherSubscriber,
        )
        firebaseNewTokenHandler.handle("aToken")
        registerPusherResult.assertions()
            .isNeverCalled()
    }

    @Test
    fun `when a new token is received, error when registering the pusher is ignored`() = runTest {
        val aMatrixClient1 = FakeMatrixClient(A_USER_ID)
        val registerPusherResult = lambdaRecorder<MatrixClient, String, String, Result<Unit>> { _, _, _ -> Result.failure(AN_EXCEPTION) }
        val pusherSubscriber = FakePusherSubscriber(registerPusherResult = registerPusherResult)
        val firebaseNewTokenHandler = createDefaultFirebaseNewTokenHandler(
            sessionStore = InMemoryMultiSessionsStore().apply {
                storeData(aSessionData(A_USER_ID.value))
            },
            matrixClientProvider = FakeMatrixClientProvider {
                Result.success(aMatrixClient1)
            },
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { _ ->
                    FakeUserPushStore(pushProviderName = FirebaseConfig.NAME)
                }
            ),
            pusherSubscriber = pusherSubscriber,
        )
        firebaseNewTokenHandler.handle("aToken")
        registerPusherResult.assertions()
        registerPusherResult.assertions()
            .isCalledOnce()
            .with(value(aMatrixClient1), value("aToken"), value(FirebaseConfig.PUSHER_HTTP_URL))
    }

    private fun createDefaultFirebaseNewTokenHandler(
        pusherSubscriber: PusherSubscriber = FakePusherSubscriber(),
        sessionStore: SessionStore = InMemorySessionStore(),
        userPushStoreFactory: UserPushStoreFactory = FakeUserPushStoreFactory(),
        matrixClientProvider: MatrixClientProvider = FakeMatrixClientProvider(),
        firebaseStore: FirebaseStore = InMemoryFirebaseStore(),
    ): FirebaseNewTokenHandler {
        return DefaultFirebaseNewTokenHandler(
            pusherSubscriber = pusherSubscriber,
            sessionStore = sessionStore,
            userPushStoreFactory = userPushStoreFactory,
            matrixClientProvider = matrixClientProvider,
            firebaseStore = firebaseStore
        )
    }
}
