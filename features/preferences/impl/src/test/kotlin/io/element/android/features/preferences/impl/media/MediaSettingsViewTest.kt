/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.preferences.impl.media

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.compose.LocalAnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.assertNoNodeWithText
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class MediaSettingsViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>(expectEvents = false)
        ensureCalledOnce {
            setMediaSettingsView(
                state = aMediaSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `when the media optimization state is not loaded, nothing is rendered`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>(expectEvents = false)
        setMediaSettingsView(
            state = aMediaSettingsState(
                mediaOptimizationState = null,
                eventSink = eventsRecorder,
            ),
        )
        assertNoNodeWithText(R.string.screen_advanced_settings_media_compression_title)
        assertNoNodeWithText(R.string.screen_advanced_settings_optimise_image_upload_quality_title)
    }

    @Test
    fun `clicking on media to enable compression emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        val analyticsService = FakeAnalyticsService()
        setMediaSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
            ),
            analyticsService = analyticsService
        )
        clickOn(R.string.screen_advanced_settings_media_compression_description)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetCompressMedia(true))
        assertThat(analyticsService.capturedEvents).isEqualTo(
            listOf(
                Interaction(
                    name = Interaction.Name.MobileSettingsOptimizeMediaUploadsEnabled
                )
            )
        )
    }

    @Test
    fun `clicking on media to disable compression emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        val analyticsService = FakeAnalyticsService()
        setMediaSettingsView(
            state = aMediaSettingsState(
                mediaOptimizationState = MediaOptimizationState.AllMedia(isEnabled = true),
                eventSink = eventsRecorder,
            ),
            analyticsService = analyticsService
        )
        clickOn(R.string.screen_advanced_settings_media_compression_description)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetCompressMedia(false))
        assertThat(analyticsService.capturedEvents).isEqualTo(
            listOf(
                Interaction(
                    name = Interaction.Name.MobileSettingsOptimizeMediaUploadsDisabled
                )
            )
        )
    }

    @Test
    fun `clicking on image upload quality in split mode emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        setMediaSettingsView(
            state = aMediaSettingsState(
                mediaOptimizationState = MediaOptimizationState.Split(
                    compressImages = false,
                    videoPreset = VideoCompressionPreset.STANDARD,
                ),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_advanced_settings_optimise_image_upload_quality_title)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetCompressMedia(true))
    }

    @Test
    fun `selecting a video upload quality emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        setMediaSettingsView(
            state = aMediaSettingsState(
                mediaOptimizationState = MediaOptimizationState.Split(
                    compressImages = true,
                    videoPreset = VideoCompressionPreset.STANDARD,
                ),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_advanced_settings_optimise_video_upload_quality_title)
        clickOn(R.string.screen_advanced_settings_optimise_video_upload_quality_low, inDialog = true)
        clickOn(CommonStrings.action_ok, inDialog = true)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetVideoUploadQuality(VideoCompressionPreset.LOW))
    }

    @Test
    fun `cancelling the video upload quality dialog emits no event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>(expectEvents = false)
        setMediaSettingsView(
            state = aMediaSettingsState(
                mediaOptimizationState = MediaOptimizationState.Split(
                    compressImages = true,
                    videoPreset = VideoCompressionPreset.STANDARD,
                ),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_advanced_settings_optimise_video_upload_quality_title)
        clickOn(R.string.screen_advanced_settings_optimise_video_upload_quality_low, inDialog = true)
        clickOn(CommonStrings.action_cancel, inDialog = true)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setMediaSettingsView(
    state: MediaSettingsState,
    analyticsService: AnalyticsService = FakeAnalyticsService(),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        CompositionLocalProvider(
            LocalAnalyticsService provides analyticsService,
        ) {
            MediaSettingsView(
                state = state,
                onBackClick = onBackClick,
            )
        }
    }
}
