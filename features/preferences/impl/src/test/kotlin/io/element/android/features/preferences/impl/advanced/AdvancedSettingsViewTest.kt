/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class AdvancedSettingsViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setAdvancedSettingsView(
                state = aAdvancedSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on other theme emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.common_appearance)
        rule.clickOn(CommonStrings.common_dark)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetTheme(ThemeOption.Dark))
    }

    @Test
    fun `clicking on View source emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_view_source)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetDeveloperModeEnabled(true))
    }

    @Test
    fun `clicking on Share presence emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_advanced_settings_share_presence)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetSharePresenceEnabled(true))
    }

    @Test
    fun `clicking on media to enable compression emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        val analyticsService = FakeAnalyticsService()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
            ),
            analyticsService = analyticsService
        )
        rule.clickOn(R.string.screen_advanced_settings_media_compression_description)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetCompressMedia(true))
        assertThat(analyticsService.capturedEvents).isEqualTo(
            listOf(
                Interaction(
                    name = Interaction.Name.MobileSettingsOptimizeMediaUploadsEnabled
                )
            )
        )
    }

    @Test
    fun `clicking on media to disable compression emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        val analyticsService = FakeAnalyticsService()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                mediaOptimizationState = MediaOptimizationState.AllMedia(isEnabled = true),
                eventSink = eventsRecorder,
            ),
            analyticsService = analyticsService
        )
        rule.clickOn(R.string.screen_advanced_settings_media_compression_description)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetCompressMedia(false))
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
    fun `clicking on hide invite avatars emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
                hideInviteAvatars = false
            ),
        )
        rule.clickOn(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetHideInviteAvatars(true))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on timeline media preview always hide emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.On
            ),
        )
        rule.clickOn(R.string.screen_advanced_settings_show_media_timeline_always_hide)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on timeline media preview private rooms emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.On
            ),
        )
        rule.clickOn(R.string.screen_advanced_settings_show_media_timeline_private_rooms)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on timeline media preview always show emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.Off
            ),
        )
        rule.clickOn(R.string.screen_advanced_settings_show_media_timeline_always_show)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.On))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `hide invite avatars toggle is disabled when action is loading`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>(expectEvents = false)
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
                hideInviteAvatars = false,
                setHideInviteAvatarsAction = AsyncAction.Loading
            ),
        )
        // The toggle should be disabled, so clicking should not emit any events
        rule.clickOn(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title)
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `timeline media preview options are disabled when action is loading`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>(expectEvents = false)
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.On,
                setTimelineMediaPreviewAction = AsyncAction.Loading
            ),
        )
        // The options should be disabled, so clicking should not emit any events
        rule.clickOn(R.string.screen_advanced_settings_show_media_timeline_always_hide)
        rule.clickOn(R.string.screen_advanced_settings_show_media_timeline_private_rooms)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setAdvancedSettingsView(
    state: AdvancedSettingsState,
    analyticsService: AnalyticsService = FakeAnalyticsService(),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        CompositionLocalProvider(
            LocalAnalyticsService provides analyticsService,
        ) {
            AdvancedSettingsView(
                state = state,
                onBackClick = onBackClick,
            )
        }
    }
}
