/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.deactivation.api.AccountDeactivationEntryPoint
import io.element.android.features.licenses.api.OpenSourceLicensesEntryPoint
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.features.logout.api.LogoutEntryPoint
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint
import io.element.android.libraries.troubleshoot.api.PushHistoryEntryPoint
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
                lockScreenEntryPoint = object : LockScreenEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext, navTarget: LockScreenEntryPoint.Target) = lambdaError()
                    override fun pinUnlockIntent(context: Context) = lambdaError()
                },
                notificationTroubleShootEntryPoint = object : NotificationTroubleShootEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                pushHistoryEntryPoint = object : PushHistoryEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                logoutEntryPoint = object : LogoutEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                openSourceLicensesEntryPoint = object : OpenSourceLicensesEntryPoint {
                    override fun getNode(node: Node, buildContext: BuildContext) = lambdaError()
                },
                accountDeactivationEntryPoint = object : AccountDeactivationEntryPoint {
                    override fun createNode(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
            )
        }
        val callback = object : PreferencesEntryPoint.Callback {
            override fun onOpenBugReport() = lambdaError()
            override fun onSecureBackupClick() = lambdaError()
            override fun onOpenRoomNotificationSettings(roomId: RoomId) = lambdaError()
            override fun navigateTo(sessionId: SessionId, roomId: RoomId, eventId: EventId) = lambdaError()
        }
        val params = PreferencesEntryPoint.Params(
            initialElement = PreferencesEntryPoint.InitialTarget.NotificationSettings,
        )
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .params(params)
            .callback(callback)
            .build()
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
