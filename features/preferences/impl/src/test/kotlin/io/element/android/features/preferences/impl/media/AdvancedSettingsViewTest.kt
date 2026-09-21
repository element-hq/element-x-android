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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
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
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import org.robolectric.annotation.Config

class AdvancedSettingsViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>(expectEvents = false)
        ensureCalledOnce {
            setAdvancedSettingsView(
                state = aMediaSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on other theme emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.common_appearance)
        clickOn(R.string.theme_dark)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetTheme(ThemeOption.Dark))
    }

    @Test
    fun `black theme is shown when available`() = runAndroidComposeUiTest {
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                availableThemeOptions = ThemeOption.entries.toImmutableList(),
            ),
        )
        clickOn(CommonStrings.common_appearance)
        run {
            val text = activity!!.getString(R.string.theme_black)
            onNodeWithText(text).assertExists()
        }
    }

    @Test
    fun `black theme is hidden when unavailable`() = runAndroidComposeUiTest {
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                availableThemeOptions = ThemeOption.entries.filterNot { it == ThemeOption.Black }.toImmutableList(),
            ),
        )
        clickOn(CommonStrings.common_appearance)
        assertNoNodeWithText(R.string.theme_black)
    }

    @Test
    fun `clicking on View source emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_view_source)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetDeveloperModeEnabled(true))
    }

    @Test
    fun `clicking on Share presence emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_advanced_settings_share_presence)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetSharePresenceEnabled(true))
    }

    @Test
    fun `clicking on media to enable compression emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        val analyticsService = FakeAnalyticsService()
        setAdvancedSettingsView(
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
        setAdvancedSettingsView(
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
    @Config(qualifiers = "h1080dp")
    fun `clicking on hide invite avatars emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
                hideInviteAvatars = false
            ),
        )
        clickOn(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetHideInviteAvatars(true))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on timeline media preview always hide emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.On
            ),
        )
        clickOn(R.string.screen_advanced_settings_show_media_timeline_always_hide)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on timeline media preview private rooms emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.On
            ),
        )
        clickOn(R.string.screen_advanced_settings_show_media_timeline_private_rooms)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on timeline media preview always show emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>()
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.Off
            ),
        )
        clickOn(R.string.screen_advanced_settings_show_media_timeline_always_show)
        eventsRecorder.assertSingle(MediaSettingsEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.On))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `hide invite avatars toggle is disabled when action is loading`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>(expectEvents = false)
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
                hideInviteAvatars = false,
                setHideInviteAvatarsAction = AsyncAction.Loading
            ),
        )
        // The toggle should be disabled, so clicking should not emit any events
        clickOn(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title)
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `timeline media preview options are disabled when action is loading`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<MediaSettingsEvent>(expectEvents = false)
        setAdvancedSettingsView(
            state = aMediaSettingsState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.On,
                setTimelineMediaPreviewAction = AsyncAction.Loading
            ),
        )
        // The options should be disabled, so clicking should not emit any events
        clickOn(R.string.screen_advanced_settings_show_media_timeline_always_hide)
        clickOn(R.string.screen_advanced_settings_show_media_timeline_private_rooms)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setAdvancedSettingsView(
    state: MediaSettingsState,
    analyticsService: AnalyticsService = FakeAnalyticsService(),
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onOpenAppSettings: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        CompositionLocalProvider(
            LocalAnalyticsService provides analyticsService,
        ) {
            MediaSettingsView(
                state = state,
                onBackClick = onBackClick,
                onOpenAppSettingsClick = onOpenAppSettings
            )
        }
    }
}
