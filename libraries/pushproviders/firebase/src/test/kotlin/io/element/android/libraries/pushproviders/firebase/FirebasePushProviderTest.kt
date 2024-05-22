/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.pushproviders.firebase

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.push.test.FakePusherSubscriber
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FirebasePushProviderTest {
    @Test
    fun `test index and name`() {
        val firebasePushProvider = createFirebasePushProvider()
        assertThat(firebasePushProvider.name).isEqualTo(FirebaseConfig.NAME)
        assertThat(firebasePushProvider.index).isEqualTo(FirebaseConfig.INDEX)
    }

    @Test
    fun `getDistributors return the unique distributor`() {
        val firebasePushProvider = createFirebasePushProvider()
        val result = firebasePushProvider.getDistributors()
        assertThat(result).containsExactly(Distributor("Firebase", "Firebase"))
    }

    @Test
    fun `getCurrentDistributor always return the unique distributor`() = runTest {
        val firebasePushProvider = createFirebasePushProvider()
        val result = firebasePushProvider.getCurrentDistributor(FakeMatrixClient())
        assertThat(result).isEqualTo(Distributor("Firebase", "Firebase"))
    }

    @Test
    fun `isAvailable true`() {
        val firebasePushProvider = createFirebasePushProvider(
            isPlayServiceAvailable = FakeIsPlayServiceAvailable(isAvailable = true)
        )
        assertThat(firebasePushProvider.isAvailable()).isTrue()
    }

    @Test
    fun `isAvailable false`() {
        val firebasePushProvider = createFirebasePushProvider(
            isPlayServiceAvailable = FakeIsPlayServiceAvailable(isAvailable = false)
        )
        assertThat(firebasePushProvider.isAvailable()).isFalse()
    }

    @Test
    fun `register ok`() = runTest {
        val matrixClient = FakeMatrixClient()
        val registerPusherResultLambda = lambdaRecorder<MatrixClient, String, String, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val firebasePushProvider = createFirebasePushProvider(
            firebaseStore = InMemoryFirebaseStore(
                token = "aToken"
            ),
            pusherSubscriber = FakePusherSubscriber(
                registerPusherResult = registerPusherResultLambda
            )
        )
        val result = firebasePushProvider.registerWith(matrixClient, Distributor("value", "Name"))
        assertThat(result).isEqualTo(Result.success(Unit))
        registerPusherResultLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(matrixClient), value("aToken"), value(FirebaseConfig.PUSHER_HTTP_URL)))
    }

    @Test
    fun `register ko no token`() = runTest {
        val firebasePushProvider = createFirebasePushProvider(
            firebaseStore = InMemoryFirebaseStore(
                token = null
            ),
            pusherSubscriber = FakePusherSubscriber(
                registerPusherResult = { _, _, _ -> Result.success(Unit) }
            )
        )
        val result = firebasePushProvider.registerWith(FakeMatrixClient(), Distributor("value", "Name"))
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `register ko error`() = runTest {
        val firebasePushProvider = createFirebasePushProvider(
            firebaseStore = InMemoryFirebaseStore(
                token = "aToken"
            ),
            pusherSubscriber = FakePusherSubscriber(
                registerPusherResult = { _, _, _ -> Result.failure(AN_EXCEPTION) }
            )
        )
        val result = firebasePushProvider.registerWith(FakeMatrixClient(), Distributor("value", "Name"))
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `unregister ok`() = runTest {
        val matrixClient = FakeMatrixClient()
        val unregisterPusherResultLambda = lambdaRecorder<MatrixClient, String, String, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val firebasePushProvider = createFirebasePushProvider(
            firebaseStore = InMemoryFirebaseStore(
                token = "aToken"
            ),
            pusherSubscriber = FakePusherSubscriber(
                unregisterPusherResult = unregisterPusherResultLambda
            )
        )
        val result = firebasePushProvider.unregister(matrixClient)
        assertThat(result).isEqualTo(Result.success(Unit))
        unregisterPusherResultLambda.assertions()
            .isCalledExactly(1)
            .withSequence(listOf(value(matrixClient), value("aToken"), value(FirebaseConfig.PUSHER_HTTP_URL)))
    }

    @Test
    fun `unregister ko no token`() = runTest {
        val firebasePushProvider = createFirebasePushProvider(
            firebaseStore = InMemoryFirebaseStore(
                token = null
            ),
            pusherSubscriber = FakePusherSubscriber(
                unregisterPusherResult = { _, _, _ -> Result.success(Unit) }
            )
        )
        val result = firebasePushProvider.unregister(FakeMatrixClient())
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `unregister ko error`() = runTest {
        val firebasePushProvider = createFirebasePushProvider(
            firebaseStore = InMemoryFirebaseStore(
                token = "aToken"
            ),
            pusherSubscriber = FakePusherSubscriber(
                unregisterPusherResult = { _, _, _ -> Result.failure(AN_EXCEPTION) }
            )
        )
        val result = firebasePushProvider.unregister(FakeMatrixClient())
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `getCurrentUserPushConfig no push ket`() = runTest {
        val firebasePushProvider = createFirebasePushProvider(
            firebaseStore = InMemoryFirebaseStore(
                token = null
            )
        )
        val result = firebasePushProvider.getCurrentUserPushConfig()
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig ok`() = runTest {
        val firebasePushProvider = createFirebasePushProvider(
            firebaseStore = InMemoryFirebaseStore(
                token = "aToken"
            ),
        )
        val result = firebasePushProvider.getCurrentUserPushConfig()
        assertThat(result).isEqualTo(CurrentUserPushConfig(FirebaseConfig.PUSHER_HTTP_URL, "aToken"))
    }

    private fun createFirebasePushProvider(
        firebaseStore: FirebaseStore = InMemoryFirebaseStore(),
        pusherSubscriber: PusherSubscriber = FakePusherSubscriber(),
        isPlayServiceAvailable: IsPlayServiceAvailable = FakeIsPlayServiceAvailable(false),
    ): FirebasePushProvider {
        return FirebasePushProvider(
            firebaseStore = firebaseStore,
            pusherSubscriber = pusherSubscriber,
            isPlayServiceAvailable = isPlayServiceAvailable,
        )
    }
}
