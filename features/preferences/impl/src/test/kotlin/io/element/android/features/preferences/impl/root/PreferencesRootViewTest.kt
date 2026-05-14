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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesRootViewTest {
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
    fun `click on User profile invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        val user = aMatrixUser()
        ensureCalledOnceWithParam(user) { callback ->
            setView(
                aPreferencesRootState(
                    myUser = user,
                    eventSink = eventsRecorder,
                ),
                onOpenUserProfile = callback,
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
    fun `click on Encryption invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    showSecureBackup = true,
                    eventSink = eventsRecorder,
                ),
                onSecureBackupClick = callback,
            )
            clickOn(CommonStrings.common_encryption)
        }
    }

    @Test
    fun `when showSecureBackup is false, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                showSecureBackup = false,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_encryption)).assertDoesNotExist()
    }

    @Test
    fun `click on Manage account invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnceWithParam("aUrl") { callback ->
            setView(
                aPreferencesRootState(
                    accountManagementUrl = "aUrl",
                    eventSink = eventsRecorder,
                ),
                onManageAccountClick = callback,
            )
            clickOn(CommonStrings.action_manage_account_and_devices)
        }
    }

    @Test
    fun `when accountManagementUrl is null, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                accountManagementUrl = null,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.action_manage_account_and_devices)).assertDoesNotExist()
    }

    @Test
    fun `click on Link new devices invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    showLinkNewDevice = true,
                    eventSink = eventsRecorder,
                ),
                onLinkNewDeviceClick = callback,
            )
            clickOn(CommonStrings.common_link_new_device)
        }
    }

    @Test
    fun `when showLinkNewDevice is false, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                showLinkNewDevice = false,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_link_new_device)).assertDoesNotExist()
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
            clickOn(CommonStrings.common_analytics)
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
    fun `click on Report a problem invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    canReportBug = true,
                    eventSink = eventsRecorder,
                ),
                onOpenRageShake = callback,
            )
            clickOn(CommonStrings.common_report_a_problem)
        }
    }

    @Test
    fun `when canReportBug is false, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                canReportBug = false,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_report_a_problem)).assertDoesNotExist()
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
    fun `click on About invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    eventSink = eventsRecorder,
                ),
                onOpenAbout = callback,
            )
            clickOn(CommonStrings.common_about)
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
            clickOn(CommonStrings.common_developer_options)
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
    fun `click on Advanced settings invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    eventSink = eventsRecorder,
                ),
                onOpenAdvancedSettings = callback,
            )
            clickOn(CommonStrings.common_advanced_settings)
        }
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
            clickOn(R.string.screen_labs_title)
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
    fun `click on Notification invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    eventSink = eventsRecorder,
                ),
                onOpenNotificationSettings = callback,
            )
            clickOn(R.string.screen_notification_settings_title)
        }
    }

    @Test
    fun `click on Blocked users invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    nbOfBlockedUsers = 1,
                    eventSink = eventsRecorder,
                ),
                onOpenBlockedUsers = callback,
            )
            clickOn(CommonStrings.common_blocked_users)
        }
    }

    @Test
    fun `when nbOfBlockedUsers is 0, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                nbOfBlockedUsers = 0,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_blocked_users)).assertDoesNotExist()
    }

    @Test
    fun `click on Remove this device invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    eventSink = eventsRecorder,
                ),
                onSignOutClick = callback,
            )
            clickOn(CommonStrings.action_signout)
        }
    }

    @Test
    fun `click on Deactivate invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                aPreferencesRootState(
                    canDeactivateAccount = true,
                    eventSink = eventsRecorder,
                ),
                onDeactivateClick = callback,
            )
            clickOn(CommonStrings.action_delete_account)
        }
    }

    @Test
    fun `when canDeactivateAccount is false, item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PreferencesRootEvent>(expectEvents = false)
        setView(
            aPreferencesRootState(
                canDeactivateAccount = false,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.action_delete_account)).assertDoesNotExist()
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
        onNodeWithText(version).performClick()
        eventsRecorder.assertSingle(PreferencesRootEvent.OnVersionInfoClick)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setView(
    state: PreferencesRootState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onAddAccountClick: () -> Unit = EnsureNeverCalled(),
    onSecureBackupClick: () -> Unit = EnsureNeverCalled(),
    onManageAccountClick: (url: String) -> Unit = EnsureNeverCalledWithParam(),
    onLinkNewDeviceClick: () -> Unit = EnsureNeverCalled(),
    onOpenAnalytics: () -> Unit = EnsureNeverCalled(),
    onOpenRageShake: () -> Unit = EnsureNeverCalled(),
    onOpenLockScreenSettings: () -> Unit = EnsureNeverCalled(),
    onOpenAbout: () -> Unit = EnsureNeverCalled(),
    onOpenDeveloperSettings: () -> Unit = EnsureNeverCalled(),
    onOpenAdvancedSettings: () -> Unit = EnsureNeverCalled(),
    onOpenLabs: () -> Unit = EnsureNeverCalled(),
    onOpenNotificationSettings: () -> Unit = EnsureNeverCalled(),
    onOpenUserProfile: (MatrixUser) -> Unit = EnsureNeverCalledWithParam(),
    onOpenBlockedUsers: () -> Unit = EnsureNeverCalled(),
    onSignOutClick: () -> Unit = EnsureNeverCalled(),
    onDeactivateClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        PreferencesRootView(
            state = state,
            onBackClick = onBackClick,
            onAddAccountClick = onAddAccountClick,
            onSecureBackupClick = onSecureBackupClick,
            onManageAccountClick = onManageAccountClick,
            onLinkNewDeviceClick = onLinkNewDeviceClick,
            onOpenAnalytics = onOpenAnalytics,
            onOpenRageShake = onOpenRageShake,
            onOpenLockScreenSettings = onOpenLockScreenSettings,
            onOpenAbout = onOpenAbout,
            onOpenDeveloperSettings = onOpenDeveloperSettings,
            onOpenAdvancedSettings = onOpenAdvancedSettings,
            onOpenLabs = onOpenLabs,
            onOpenNotificationSettings = onOpenNotificationSettings,
            onOpenUserProfile = onOpenUserProfile,
            onOpenBlockedUsers = onOpenBlockedUsers,
            onSignOutClick = onSignOutClick,
            onDeactivateClick = onDeactivateClick,
        )
    }
}
