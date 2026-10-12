/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.preferences.impl

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.google.common.truth.Truth.assertThat
import io.element.android.features.deactivation.test.FakeAccountDeactivationEntryPoint
import io.element.android.features.licenses.test.FakeOpenSourceLicensesEntryPoint
import io.element.android.features.lockscreen.test.FakeLockScreenEntryPoint
import io.element.android.features.logout.test.FakeLogoutEntryPoint
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.preferences.api.PreferencesExtension
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.mediaviewer.test.FakeFileViewerEntryPoint
import io.element.android.libraries.troubleshoot.test.FakeNotificationTroubleShootEntryPoint
import io.element.android.libraries.troubleshoot.test.FakePushHistoryEntryPoint
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class PreferencesFlowNodeTest : RobolectricTest() {
    @Test
    fun `supplementary route delegates to extension with its parent and child context`() {
        val context = BuildContext.root(null)
        val child = Node(context)
        var receivedParent: Node? = null
        val extension = object : PreferencesExtension {
            @Composable
            override fun ColumnScope.Render(onNavigate: (String) -> Unit, modifier: Modifier) = Unit
            override fun createNode(parentNode: Node, buildContext: BuildContext, route: String): Node {
                receivedParent = parentNode
                assertThat(buildContext).isSameInstanceAs(context)
                assertThat(route).isEqualTo("extra")
                return child
            }
        }
        val flow = createFlowNode(extension = extension)
        assertThat(flow.resolve(PreferencesFlowNode.NavTarget.SupplementarySettings("extra"), context)).isSameInstanceAs(child)
        assertThat(receivedParent).isSameInstanceAs(flow)
    }

    @Test
    fun `unsupported restored route returns to the existing settings screen`() = runAndroidComposeUiTest<ComponentActivity> {
        val flow = createFlowNode()
        val route = PreferencesFlowNode.NavTarget.SupplementarySettings("unsupported")
        flow.backstack.push(route)
        val child = flow.resolve(route, BuildContext.root(null))
        setContent { child.View(Modifier) }
        runOnIdle { assertThat(flow.backstack.activeElement).isEqualTo(PreferencesFlowNode.NavTarget.Root) }
    }

    @Test
    fun `supplementary route restores and back returns to settings`() {
        val flow = createFlowNode()
        val route = PreferencesFlowNode.NavTarget.SupplementarySettings("extra")
        flow.backstack.push(route)
        val state = flow.saveInstanceState(SaverScope { true })
        val restored = createFlowNode(context = BuildContext.root(state))
        assertThat(restored.backstack.activeElement).isEqualTo(route)
        restored.backstack.pop()
        assertThat(restored.backstack.activeElement).isEqualTo(PreferencesFlowNode.NavTarget.Root)
    }

    private fun createFlowNode(
        context: BuildContext = BuildContext.root(null),
        extension: PreferencesExtension = NoopPreferencesExtension(),
    ) = PreferencesFlowNode(
        buildContext = context,
        plugins = listOf(
            PreferencesEntryPoint.Params(PreferencesEntryPoint.InitialTarget.Root),
            object : PreferencesEntryPoint.Callback {
                override fun navigateToAddAccount() = Unit
                override fun navigateToLinkNewDevice() = Unit
                override fun navigateToBugReport() = Unit
                override fun navigateToSecureBackup() = Unit
                override fun navigateToRoomNotificationSettings(roomId: RoomId) = Unit
                override fun navigateToEvent(roomId: RoomId, eventId: EventId) = Unit
            },
        ),
        preferencesExtension = extension,
        lockScreenEntryPoint = FakeLockScreenEntryPoint(),
        notificationTroubleShootEntryPoint = FakeNotificationTroubleShootEntryPoint(),
        pushHistoryEntryPoint = FakePushHistoryEntryPoint(),
        fileViewerEntryPoint = FakeFileViewerEntryPoint(),
        logoutEntryPoint = FakeLogoutEntryPoint(),
        openSourceLicensesEntryPoint = FakeOpenSourceLicensesEntryPoint(),
        accountDeactivationEntryPoint = FakeAccountDeactivationEntryPoint(),
    )
}
