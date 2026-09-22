/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.moderation

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ModerationAndSafetyPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createModerationAndSafetyPresenter().test {
            with(awaitItem()) {
                assertThat(isSharePresenceEnabled).isTrue()
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isFalse()
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)
                assertThat(mediaPreviewConfigState.setHideInviteAvatarsAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(mediaPreviewConfigState.setTimelineMediaPreviewAction).isEqualTo(AsyncAction.Uninitialized)
            }
        }
    }

    @Test
    fun `present - share presence off on`() = runTest {
        createModerationAndSafetyPresenter().test {
            with(awaitItem()) {
                assertThat(isSharePresenceEnabled).isTrue()
                eventSink(ModerationAndSafetyEvent.SetSharePresenceEnabled(false))
            }
            with(awaitItem()) {
                assertThat(isSharePresenceEnabled).isFalse()
                eventSink(ModerationAndSafetyEvent.SetSharePresenceEnabled(true))
            }
            with(awaitItem()) {
                assertThat(isSharePresenceEnabled).isTrue()
            }
        }
    }

    @Test
    fun `present - hide invite avatars`() = runTest {
        val mediaPreviewStore = FakeMediaPreviewConfigStateStore()
        createModerationAndSafetyPresenter(mediaPreviewConfigStateStore = mediaPreviewStore).test {
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isFalse()
                eventSink(ModerationAndSafetyEvent.SetHideInviteAvatars(true))
            }
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isTrue()
                eventSink(ModerationAndSafetyEvent.SetHideInviteAvatars(false))
            }
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isFalse()
            }
        }
        assertThat(mediaPreviewStore.getSetHideInviteAvatarsEvents()).isEqualTo(listOf(true, false))
    }

    @Test
    fun `present - timeline media preview value`() = runTest {
        val mediaPreviewStore = FakeMediaPreviewConfigStateStore()
        createModerationAndSafetyPresenter(mediaPreviewConfigStateStore = mediaPreviewStore).test {
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)
                eventSink(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
            }
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Off)
                eventSink(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
            }
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Private)
            }
        }
        assertThat(mediaPreviewStore.getSetTimelineMediaPreviewValueEvents()).isEqualTo(
            listOf(MediaPreviewValue.Off, MediaPreviewValue.Private)
        )
    }

    @Test
    fun `present - media preview state with custom initial values`() = runTest {
        val mediaPreviewStore = FakeMediaPreviewConfigStateStore(
            hideInviteAvatarsValue = true,
            timelineMediaPreviewValue = MediaPreviewValue.Private
        )
        createModerationAndSafetyPresenter(mediaPreviewConfigStateStore = mediaPreviewStore).test {
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isTrue()
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Private)
            }
        }
    }

    @Test
    fun `present - async actions state`() = runTest {
        val mediaPreviewStore = FakeMediaPreviewConfigStateStore(
            setHideInviteAvatarsActionValue = AsyncAction.Loading,
            setTimelineMediaPreviewActionValue = AsyncAction.Success(Unit)
        )
        createModerationAndSafetyPresenter(mediaPreviewConfigStateStore = mediaPreviewStore).test {
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.setHideInviteAvatarsAction).isEqualTo(AsyncAction.Loading)
                assertThat(mediaPreviewConfigState.setTimelineMediaPreviewAction).isEqualTo(AsyncAction.Success(Unit))
            }
        }
    }

    private fun CoroutineScope.createModerationAndSafetyPresenter(
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
        mediaPreviewConfigStateStore: MediaPreviewConfigStateStore = FakeMediaPreviewConfigStateStore(),
    ) = ModerationAndSafetyPresenter(
        sessionPreferencesStore = sessionPreferencesStore,
        mediaPreviewConfigStateStore = mediaPreviewConfigStateStore,
        sessionCoroutineScope = this,
    )
}
