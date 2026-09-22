/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.preferences.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.emoji.api.picker.NoOpEmojiPickerRenderer
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.assertNoNodeWithText
import io.element.android.tests.testutils.assertNodeWithTextIsDisplayed
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class PreferencesRootViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes back callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `click on the user row invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    myUser = aMatrixUser(),
                    eventSink = eventsRecorder,
                ),
                onOpenAccountSettings = callback,
            )
            onNodeWithText("Alice").performClick()
        }
    }

    @Test
    fun `clicking on other session sends a SwitchToSession`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>()
        setView(
            aPreferencesRootState(
                isMultiAccountEnabled = true,
                isOtherAccountsSectionExpanded = true,
                otherSessions = listOf(
                    aMatrixUser(
                        id = A_USER_ID_2.value,
                        displayName = "Bob",
                    )
                ),
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText("Bob").performClick()
        eventsRecorder.assertSingle(PreferencesRootEvent.SwitchToSession(A_USER_ID_2))
    }

    @Test
    fun `clicking on Switch accounts sends a ToggleOtherAccountsExpanded`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>()
        setView(
            aPreferencesRootState(
                isMultiAccountEnabled = true,
                isOtherAccountsSectionExpanded = false,
                otherSessions = listOf(
                    aMatrixUser(
                        id = A_USER_ID_2.value,
                        displayName = "Bob",
                    )
                ),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.common_switch_account)
        eventsRecorder.assertSingle(PreferencesRootEvent.ToggleOtherAccountsExpanded)
    }

    @Test
    fun `when the other accounts section is collapsed, other sessions and add account are not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                isMultiAccountEnabled = true,
                isOtherAccountsSectionExpanded = false,
                otherSessions = listOf(
                    aMatrixUser(
                        id = A_USER_ID_2.value,
                        displayName = "Bob",
                    )
                ),
                eventSink = eventsRecorder,
            ),
        )
        assertNodeWithTextIsDisplayed(CommonStrings.common_switch_account)
        onNodeWithText("Bob").assertDoesNotExist()
        assertNoNodeWithText(CommonStrings.common_add_another_account)
    }

    @Test
    fun `click on Add account of the expanded section invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    isMultiAccountEnabled = true,
                    isOtherAccountsSectionExpanded = true,
                    otherSessions = listOf(
                        aMatrixUser(
                            id = A_USER_ID_2.value,
                            displayName = "Bob",
                        )
                    ),
                    eventSink = eventsRecorder,
                ),
                onAddAccountClick = callback,
            )
            clickOn(CommonStrings.common_add_another_account)
        }
    }

    @Test
    fun `click on Add account invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    isMultiAccountEnabled = true,
                    eventSink = eventsRecorder,
                ),
                onAddAccountClick = callback,
            )
            clickOn(CommonStrings.common_add_another_account)
        }
    }

    @Test
    fun `when multi account is not enabled, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                isMultiAccountEnabled = false,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_add_another_account)).assertDoesNotExist()
    }

    @Test
    fun `clicking on another theme emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>()
        setView(
            aPreferencesRootState(
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.common_appearance)
        clickOn(R.string.theme_dark)
        eventsRecorder.assertSingle(PreferencesRootEvent.SetTheme(ThemeOption.Dark))
    }

    @Test
    fun `black theme is shown when available`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                availableThemeOptions = ThemeOption.entries.toImmutableList(),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.common_appearance)
        onNodeWithText(activity!!.getString(R.string.theme_black)).assertExists()
    }

    @Test
    fun `black theme is hidden when unavailable`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                availableThemeOptions = ThemeOption.entries.filterNot { it == ThemeOption.Black }.toImmutableList(),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.common_appearance)
        assertNoNodeWithText(R.string.theme_black)
    }

    @Test
    fun `click on Media upload quality invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    eventSink = eventsRecorder,
                ),
                onOpenMediaSettings = callback,
            )
            clickOn(CommonStrings.common_media_upload_quality)
        }
    }

    @Test
    fun `click on Screen lock invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    eventSink = eventsRecorder,
                ),
                onOpenLockScreenSettings = callback,
            )
            clickOn(CommonStrings.common_screen_lock)
        }
    }

    @Test
    fun `click on Location sharing invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    eventSink = eventsRecorder,
                ),
                onOpenLocationSettings = callback,
            )
            val text = activity!!.getString(CommonStrings.common_location_sharing)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `click on Analytics invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    showAnalyticsSettings = true,
                    eventSink = eventsRecorder,
                ),
                onOpenAnalytics = callback,
            )
            val text = activity!!.getString(CommonStrings.common_analytics)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `when showAnalyticsSettings is false, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                showAnalyticsSettings = false,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_analytics)).assertDoesNotExist()
    }

    @Test
    fun `click on Labs invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    showLabsItem = true,
                    eventSink = eventsRecorder,
                ),
                onOpenLabs = callback,
            )
            val text = activity!!.getString(R.string.screen_labs_title)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `when showLabsItem is false, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                showLabsItem = false,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(R.string.screen_labs_title)).assertDoesNotExist()
    }

    @Test
    fun `click on About invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    eventSink = eventsRecorder,
                ),
                onOpenAbout = callback,
            )
            val text = activity!!.getString(CommonStrings.common_about)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `click on Developer settings invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    showDeveloperSettings = true,
                    eventSink = eventsRecorder,
                ),
                onOpenDeveloperSettings = callback,
            )
            val text = activity!!.getString(CommonStrings.common_developer_options)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `when showDeveloperSettings is false, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                showDeveloperSettings = false,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_developer_options)).assertDoesNotExist()
    }

    @Test
    fun `when userStatusState is null, the status section is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                userStatusState = null,
                eventSink = eventsRecorder,
            ),
        )
        assertNoNodeWithText(R.string.screen_settings_user_status_placeholder)
    }

    @Test
    fun `clicking on version sends a PreferencesRootEvents`() = runAndroidComposeUiTest {
        val version = "VERSION"
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>()
        setView(
            aPreferencesRootState(
                version = version,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(version).performScrollTo().performClick()
        eventsRecorder.assertSingle(PreferencesRootEvent.OnVersionInfoClick)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setView(
    state: PreferencesRootState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onAddAccountClick: () -> Unit = EnsureNeverCalled(),
    onOpenAnalytics: () -> Unit = EnsureNeverCalled(),
    onOpenLockScreenSettings: () -> Unit = EnsureNeverCalled(),
    onOpenAbout: () -> Unit = EnsureNeverCalled(),
    onOpenDeveloperSettings: () -> Unit = EnsureNeverCalled(),
    onOpenMediaSettings: () -> Unit = EnsureNeverCalled(),
    onOpenLocationSettings: () -> Unit = EnsureNeverCalled(),
    onOpenLabs: () -> Unit = EnsureNeverCalled(),
    onOpenAccountSettings: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        PreferencesRootView(
            state = state,
            emojiPickerRenderer = NoOpEmojiPickerRenderer,
            onBackClick = onBackClick,
            onAddAccountClick = onAddAccountClick,
            onOpenAnalytics = onOpenAnalytics,
            onOpenLockScreenSettings = onOpenLockScreenSettings,
            onOpenAbout = onOpenAbout,
            onOpenDeveloperSettings = onOpenDeveloperSettings,
            onOpenMediaSettings = onOpenMediaSettings,
            onOpenLocationSettings = onOpenLocationSettings,
            onOpenLabs = onOpenLabs,
            onOpenAccountSettings = onOpenAccountSettings,
        )
    }
}
