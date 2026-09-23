/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.preferences.impl.account

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class PreferencesAccountViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes back callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                aPreferencesAccountState(),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `click on the avatar invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnceWithParam("anAvatarUrl") { callback ->
            setView(
                aPreferencesAccountState(
                    myUser = aMatrixUser(avatarUrl = "anAvatarUrl"),
                ),
                onAvatarClick = callback,
            )
            onNodeWithContentDescription(activity!!.getString(CommonStrings.a11y_user_avatar)).performClick()
        }
    }

    @Test
    fun `click on Edit profile invokes the expected callback`() = runAndroidComposeUiTest {
        val user = aMatrixUser()
        ensureCalledOnceWithParam(user) { callback ->
            setView(
                aPreferencesAccountState(myUser = user),
                onEditProfileClick = callback,
            )
            clickOn(R.string.screen_edit_profile_title)
        }
    }

    @Test
    fun `click on Manage account invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnceWithParam("aUrl") { callback ->
            setView(
                aPreferencesAccountState(accountManagementUrl = "aUrl"),
                onManageAccountClick = callback,
            )
            clickOn(CommonStrings.action_manage_account_and_devices)
        }
    }

    @Test
    fun `when accountManagementUrl is null, item is not shown`() = runAndroidComposeUiTest {
        setView(aPreferencesAccountState(accountManagementUrl = null))
        onNodeWithText(activity!!.getString(CommonStrings.action_manage_account_and_devices)).assertDoesNotExist()
    }

    @Test
    fun `click on Link new devices invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                aPreferencesAccountState(showLinkNewDevice = true),
                onLinkNewDeviceClick = callback,
            )
            clickOn(CommonStrings.common_link_new_device)
        }
    }

    @Test
    fun `when showLinkNewDevice is false, item is not shown`() = runAndroidComposeUiTest {
        setView(aPreferencesAccountState(showLinkNewDevice = false))
        onNodeWithText(activity!!.getString(CommonStrings.common_link_new_device)).assertDoesNotExist()
    }

    @Test
    fun `click on Blocked users invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                aPreferencesAccountState(numberOfBlockedUsers = 1),
                onOpenBlockedUsers = callback,
            )
            clickOn(CommonStrings.common_blocked_users)
        }
    }

    @Test
    fun `when numberOfBlockedUsers is 0, item is not shown`() = runAndroidComposeUiTest {
        setView(aPreferencesAccountState(numberOfBlockedUsers = 0))
        onNodeWithText(activity!!.getString(CommonStrings.common_blocked_users)).assertDoesNotExist()
    }

    @Test
    fun `click on Notification invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                aPreferencesAccountState(),
                onOpenNotificationSettings = callback,
            )
            clickOn(R.string.screen_notification_settings_title)
        }
    }

    @Test
    fun `click on Encryption invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                aPreferencesAccountState(showSecureBackup = true),
                onSecureBackupClick = callback,
            )
            val text = activity!!.getString(CommonStrings.common_encryption)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `when showSecureBackup is false, item is not shown`() = runAndroidComposeUiTest {
        setView(aPreferencesAccountState(showSecureBackup = false))
        onNodeWithText(activity!!.getString(CommonStrings.common_encryption)).assertDoesNotExist()
    }

    @Test
    fun `click on Moderation and safety invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                aPreferencesAccountState(),
                onModerationAndSafetyClick = callback,
            )
            val text = activity!!.getString(CommonStrings.common_moderation_and_safety)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `click on Report a problem invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                aPreferencesAccountState(canReportBug = true),
                onOpenRageShake = callback,
            )
            val text = activity!!.getString(CommonStrings.common_report_a_problem)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `when canReportBug is false, item is not shown`() = runAndroidComposeUiTest {
        setView(aPreferencesAccountState(canReportBug = false))
        onNodeWithText(activity!!.getString(CommonStrings.common_report_a_problem)).assertDoesNotExist()
    }

    @Test
    fun `click on Remove this device invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                aPreferencesAccountState(),
                onSignOutClick = callback,
            )
            val text = activity!!.getString(CommonStrings.action_signout)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `click on Deactivate invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                aPreferencesAccountState(canDeactivateAccount = true),
                onDeactivateClick = callback,
            )
            val text = activity!!.getString(CommonStrings.action_delete_account)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `when canDeactivateAccount is false, item is not shown`() = runAndroidComposeUiTest {
        setView(aPreferencesAccountState(canDeactivateAccount = false))
        onNodeWithText(activity!!.getString(CommonStrings.action_delete_account)).assertDoesNotExist()
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setView(
    state: PreferencesAccountState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onAvatarClick: (String) -> Unit = EnsureNeverCalledWithParam(),
    onEditProfileClick: (MatrixUser) -> Unit = EnsureNeverCalledWithParam(),
    onSecureBackupClick: () -> Unit = EnsureNeverCalled(),
    onManageAccountClick: (url: String) -> Unit = EnsureNeverCalledWithParam(),
    onLinkNewDeviceClick: () -> Unit = EnsureNeverCalled(),
    onOpenRageShake: () -> Unit = EnsureNeverCalled(),
    onModerationAndSafetyClick: () -> Unit = EnsureNeverCalled(),
    onOpenNotificationSettings: () -> Unit = EnsureNeverCalled(),
    onOpenBlockedUsers: () -> Unit = EnsureNeverCalled(),
    onSignOutClick: () -> Unit = EnsureNeverCalled(),
    onDeactivateClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        PreferencesAccountView(
            state = state,
            onBackClick = onBackClick,
            onAvatarClick = onAvatarClick,
            onEditProfileClick = onEditProfileClick,
            onSecureBackupClick = onSecureBackupClick,
            onManageAccountClick = onManageAccountClick,
            onLinkNewDeviceClick = onLinkNewDeviceClick,
            onOpenRageShake = onOpenRageShake,
            onModerationAndSafetyClick = onModerationAndSafetyClick,
            onOpenNotificationSettings = onOpenNotificationSettings,
            onOpenBlockedUsers = onOpenBlockedUsers,
            onSignOutClick = onSignOutClick,
            onDeactivateClick = onDeactivateClick,
        )
    }
}
