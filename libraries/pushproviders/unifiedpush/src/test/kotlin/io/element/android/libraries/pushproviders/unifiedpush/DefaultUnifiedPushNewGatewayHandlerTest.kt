/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.push.test.FakePusherSubscriber
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultUnifiedPushNewGatewayHandlerTest {
    @Test
    fun `error when fail to retrieve the session`() = runTest {
        val defaultUnifiedPushNewGatewayHandler = createDefaultUnifiedPushNewGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { null }
            )
        )
        val result = defaultUnifiedPushNewGatewayHandler.handle(
            endpoint = "aEndpoint",
            pushGateway = "aPushGateway",
            clientSecret = A_SECRET,
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Unable to retrieve session")
    }

    @Test
    fun `error when the session is not using UnifiedPush`() = runTest {
        val defaultUnifiedPushNewGatewayHandler = createDefaultUnifiedPushNewGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { FakeUserPushStore(pushProviderName = "other") }
            )
        )
        val result = defaultUnifiedPushNewGatewayHandler.handle(
            endpoint = "aEndpoint",
            pushGateway = "aPushGateway",
            clientSecret = A_SECRET,
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("This session is not using UnifiedPush pusher")
    }

    @Test
    fun `error when the registration fails`() = runTest {
        val aMatrixClient = FakeMatrixClient()
        val defaultUnifiedPushNewGatewayHandler = createDefaultUnifiedPushNewGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { FakeUserPushStore(pushProviderName = UnifiedPushConfig.NAME) }
            ),
            pusherSubscriber = FakePusherSubscriber(
                registerPusherResult = { _, _, _ -> Result.failure(IllegalStateException("an error")) }
            ),
            matrixClientProvider = FakeMatrixClientProvider { Result.success(aMatrixClient) },
        )
        val result = defaultUnifiedPushNewGatewayHandler.handle(
            endpoint = "aEndpoint",
            pushGateway = "aPushGateway",
            clientSecret = A_SECRET,
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("an error")
    }

    @Test
    fun `happy path`() = runTest {
        val aMatrixClient = FakeMatrixClient()
        val lambda = lambdaRecorder { _: MatrixClient, _: String, _: String ->
            Result.success(Unit)
        }
        val defaultUnifiedPushNewGatewayHandler = createDefaultUnifiedPushNewGatewayHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { FakeUserPushStore(pushProviderName = UnifiedPushConfig.NAME) }
            ),
            pusherSubscriber = FakePusherSubscriber(
                registerPusherResult = lambda
            ),
            matrixClientProvider = FakeMatrixClientProvider { Result.success(aMatrixClient) },
        )
        val result = defaultUnifiedPushNewGatewayHandler.handle(
            endpoint = "aEndpoint",
            pushGateway = "aPushGateway",
            clientSecret = A_SECRET,
        )
        assertThat(result).isEqualTo(Result.success(Unit))
        lambda.assertions()
            .isCalledOnce()
            .with(value(aMatrixClient), value("aEndpoint"), value("aPushGateway"))
    }

    private fun createDefaultUnifiedPushNewGatewayHandler(
        pusherSubscriber: PusherSubscriber = FakePusherSubscriber(),
        userPushStoreFactory: UserPushStoreFactory = FakeUserPushStoreFactory(),
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        matrixClientProvider: MatrixClientProvider = FakeMatrixClientProvider()
    ): DefaultUnifiedPushNewGatewayHandler {
        return DefaultUnifiedPushNewGatewayHandler(
            pusherSubscriber = pusherSubscriber,
            userPushStoreFactory = userPushStoreFactory,
            pushClientSecret = pushClientSecret,
            matrixClientProvider = matrixClientProvider,
        )
    }
}
