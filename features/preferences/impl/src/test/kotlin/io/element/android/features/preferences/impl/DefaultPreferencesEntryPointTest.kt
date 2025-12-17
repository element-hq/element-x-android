/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.deactivation.test.FakeAccountDeactivationEntryPoint
import io.element.android.features.licenses.test.FakeOpenSourceLicensesEntryPoint
import io.element.android.features.lockscreen.test.FakeLockScreenEntryPoint
import io.element.android.features.logout.test.FakeLogoutEntryPoint
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.troubleshoot.test.FakeNotificationTroubleShootEntryPoint
import io.element.android.libraries.troubleshoot.test.FakePushHistoryEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultPreferencesEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultPreferencesEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            PreferencesFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                lockScreenEntryPoint = FakeLockScreenEntryPoint(),
                notificationTroubleShootEntryPoint = FakeNotificationTroubleShootEntryPoint(),
                pushHistoryEntryPoint = FakePushHistoryEntryPoint(),
                logoutEntryPoint = FakeLogoutEntryPoint(),
                openSourceLicensesEntryPoint = FakeOpenSourceLicensesEntryPoint(),
                accountDeactivationEntryPoint = FakeAccountDeactivationEntryPoint(),
            )
        }
        val callback = object : PreferencesEntryPoint.Callback {
            override fun navigateToAddAccount() = lambdaError()
            override fun navigateToBugReport() = lambdaError()
            override fun navigateToSecureBackup() = lambdaError()
            override fun navigateToRoomNotificationSettings(roomId: RoomId) = lambdaError()
            override fun navigateToEvent(roomId: RoomId, eventId: EventId) = lambdaError()
        }
        val params = PreferencesEntryPoint.Params(
            initialElement = PreferencesEntryPoint.InitialTarget.NotificationSettings,
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(PreferencesFlowNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }

    @Test
    fun `test initial target to nav target mapping`() {
        assertThat(PreferencesEntryPoint.InitialTarget.Root.toNavTarget())
            .isEqualTo(PreferencesFlowNode.NavTarget.Root)
        assertThat(PreferencesEntryPoint.InitialTarget.NotificationSettings.toNavTarget())
            .isEqualTo(PreferencesFlowNode.NavTarget.NotificationSettings)
        assertThat(PreferencesEntryPoint.InitialTarget.NotificationTroubleshoot.toNavTarget())
            .isEqualTo(PreferencesFlowNode.NavTarget.TroubleshootNotifications)
    }
}
