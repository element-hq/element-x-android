/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.tasks

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.ftue.test.FakeFtueService
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.features.preferences.impl.DefaultCacheService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.push.test.FakePushService
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultClearCacheUseCaseTest {
    @Test
    fun `execute clear cache should do all the expected tasks`() = runTest {
        val activeRoomsHolder = ActiveRoomsHolder().apply { addRoom(FakeJoinedRoom()) }
        val clearCacheLambda = lambdaRecorder<Unit> { }
        val matrixClient = FakeMatrixClient(
            sessionId = A_SESSION_ID,
            clearCacheLambda = clearCacheLambda,
        )
        val defaultCacheService = DefaultCacheService()
        val resetFtueLambda = lambdaRecorder<Unit> { }
        val ftueService = FakeFtueService(
            resetLambda = resetFtueLambda,
        )
        val setIgnoreRegistrationErrorLambda = lambdaRecorder<SessionId, Boolean, Unit> { _, _ -> }
        val resetBatteryOptimizationStateResult = lambdaRecorder<Unit> { }
        val pushService = FakePushService(
            setIgnoreRegistrationErrorLambda = setIgnoreRegistrationErrorLambda,
            resetBatteryOptimizationStateResult = resetBatteryOptimizationStateResult,
        )
        val seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID))
        assertThat(seenInvitesStore.seenRoomIds().first()).isNotEmpty()
        val sut = DefaultClearCacheUseCase(
            context = InstrumentationRegistry.getInstrumentation().context,
            matrixClient = matrixClient,
            coroutineDispatchers = testCoroutineDispatchers(),
            defaultCacheService = defaultCacheService,
            okHttpClient = { OkHttpClient.Builder().build() },
            ftueService = ftueService,
            pushService = pushService,
            seenInvitesStore = seenInvitesStore,
            activeRoomsHolder = activeRoomsHolder,
        )
        defaultCacheService.clearedCacheEventFlow.test {
            sut.invoke()
            clearCacheLambda.assertions().isCalledOnce()
            resetFtueLambda.assertions().isCalledOnce()
            setIgnoreRegistrationErrorLambda.assertions().isCalledOnce()
                .with(value(matrixClient.sessionId), value(false))
            resetBatteryOptimizationStateResult.assertions().isCalledOnce()
            assertThat(awaitItem()).isEqualTo(matrixClient.sessionId)
            assertThat(seenInvitesStore.seenRoomIds().first()).isEmpty()
            assertThat(activeRoomsHolder.getActiveRoom(A_SESSION_ID)).isNull()
        }
    }
}
