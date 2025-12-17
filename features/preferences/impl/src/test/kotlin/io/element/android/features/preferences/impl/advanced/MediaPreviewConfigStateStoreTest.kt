/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.media.MediaPreviewConfig
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.test.media.FakeMediaPreviewService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MediaPreviewConfigStateStoreTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `initial state is correct with default values`() = runTest {
        val store = createMediaPreviewConfigStateStore()

        moleculeFlow(RecompositionMode.Immediate) {
            store.state()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.hideInviteAvatars).isFalse()
            assertThat(initialState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)
            assertThat(initialState.setHideInviteAvatarsAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            assertThat(initialState.setTimelineMediaPreviewAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    @Test
    fun `state updates when config flow emits new values`() = runTest {
        val configFlow = MutableStateFlow(MediaPreviewConfig.DEFAULT)
        val mediaPreviewService = FakeMediaPreviewService(configFlow)
        val store = createMediaPreviewConfigStateStore(mediaPreviewService = mediaPreviewService)

        moleculeFlow(RecompositionMode.Immediate) {
            store.state()
        }.test {
            // Initial state
            val initialState = awaitItem()
            assertThat(initialState.hideInviteAvatars).isFalse()
            assertThat(initialState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)

            // Update config
            configFlow.value = MediaPreviewConfig(hideInviteAvatar = true, mediaPreviewValue = MediaPreviewValue.Private)

            skipItems(1)
            // Updated state
            val updatedState = awaitItem()
            assertThat(updatedState.hideInviteAvatars).isTrue()
            assertThat(updatedState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Private)
        }
    }

    @Test
    fun `setHideInviteAvatars updates state and calls service on success`() = runTest {
        val setHideInviteAvatarsValueLambda = lambdaRecorder<Boolean, Result<Unit>> { Result.success(Unit) }
        val mediaPreviewService = FakeMediaPreviewService(
            setHideInviteAvatarsResult = setHideInviteAvatarsValueLambda
        )
        val store = createMediaPreviewConfigStateStore(mediaPreviewService = mediaPreviewService)
        moleculeFlow(RecompositionMode.Immediate) {
            store.state()
        }.test {
            awaitItem().also { state ->
                assertThat(state.hideInviteAvatars).isFalse()
            }
            store.setHideInviteAvatars(true)

            awaitItem().also { state ->
                assertThat(state.hideInviteAvatars).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.hideInviteAvatars).isTrue()
                assertThat(state.setHideInviteAvatarsAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.hideInviteAvatars).isTrue()
                assertThat(state.setHideInviteAvatarsAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            assert(setHideInviteAvatarsValueLambda).isCalledOnce()
        }
    }

    @Test
    fun `setHideInviteAvatars reverts state on failure`() = runTest {
        val setHideInviteAvatarsValueLambda = lambdaRecorder<Boolean, Result<Unit>> {
            Result.failure(Exception())
        }
        val mediaPreviewService = FakeMediaPreviewService(
            setHideInviteAvatarsResult = setHideInviteAvatarsValueLambda
        )
        val store = createMediaPreviewConfigStateStore(mediaPreviewService = mediaPreviewService)
        moleculeFlow(RecompositionMode.Immediate) {
            store.state()
        }.test {
            awaitItem().also { state ->
                assertThat(state.hideInviteAvatars).isFalse()
            }
            store.setHideInviteAvatars(true)

            awaitItem().also { state ->
                assertThat(state.hideInviteAvatars).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.hideInviteAvatars).isTrue()
                assertThat(state.setHideInviteAvatarsAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.hideInviteAvatars).isFalse()
                assertThat(state.setHideInviteAvatarsAction).isInstanceOf(AsyncAction.Failure::class.java)
            }
            assert(setHideInviteAvatarsValueLambda).isCalledOnce()
        }
    }

    @Test
    fun `setTimelineMediaPreviewValue updates state and calls service on success`() = runTest {
        val setMediaPreviewValueLambda = lambdaRecorder<MediaPreviewValue, Result<Unit>> { Result.success(Unit) }
        val mediaPreviewService = FakeMediaPreviewService(
            setMediaPreviewValueResult = setMediaPreviewValueLambda
        )
        val store = createMediaPreviewConfigStateStore(mediaPreviewService = mediaPreviewService)
        moleculeFlow(RecompositionMode.Immediate) {
            store.state()
        }.test {
            awaitItem().also { state ->
                assertThat(state.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)
            }
            store.setTimelineMediaPreviewValue(MediaPreviewValue.Off)

            awaitItem().also { state ->
                assertThat(state.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Off)
            }
            awaitItem().also { state ->
                assertThat(state.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Off)
                assertThat(state.setTimelineMediaPreviewAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Off)
                assertThat(state.setTimelineMediaPreviewAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            assert(setMediaPreviewValueLambda).isCalledOnce()
        }
    }

    @Test
    fun `setTimelineMediaPreviewValue reverts state on failure`() = runTest {
        val setMediaPreviewValueLambda = lambdaRecorder<MediaPreviewValue, Result<Unit>> {
            Result.failure(Exception())
        }
        val mediaPreviewService = FakeMediaPreviewService(
            setMediaPreviewValueResult = setMediaPreviewValueLambda
        )
        val store = createMediaPreviewConfigStateStore(mediaPreviewService = mediaPreviewService)
        moleculeFlow(RecompositionMode.Immediate) {
            store.state()
        }.test {
            awaitItem().also { state ->
                assertThat(state.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)
            }
            store.setTimelineMediaPreviewValue(MediaPreviewValue.Off)

            awaitItem().also { state ->
                assertThat(state.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Off)
            }
            awaitItem().also { state ->
                assertThat(state.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Off)
                assertThat(state.setTimelineMediaPreviewAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)
                assertThat(state.setTimelineMediaPreviewAction).isInstanceOf(AsyncAction.Failure::class.java)
            }
            assert(setMediaPreviewValueLambda).isCalledOnce()
        }
    }

    private fun TestScope.createMediaPreviewConfigStateStore(
        mediaPreviewService: FakeMediaPreviewService = FakeMediaPreviewService(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher()
    ): MediaPreviewConfigStateStore = DefaultMediaPreviewConfigStateStore(
        sessionCoroutineScope = backgroundScope,
        mediaPreviewService = mediaPreviewService,
        snackbarDispatcher = snackbarDispatcher
    )
}
