/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl

import com.bumble.appyx.core.modality.AncestryInfo
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectoryImpl
import com.google.common.truth.Truth.assertThat
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyEntryPoint
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SecurityAndPrivacyFlowNodeTest : RobolectricTest() {
    @Test
    fun `initial backstack contains SecurityAndPrivacy`() = runTest {
        val flowNode = createFlowNode()
        assertThat(flowNode.currentNavTarget()).isEqualTo(SecurityAndPrivacyFlowNode.NavTarget.SecurityAndPrivacy)
    }

    @Test
    fun `openEditRoomAddress navigates to EditRoomAddress`() = runTest {
        val flowNode = createFlowNode()
        flowNode.navigator.openEditRoomAddress()
        assertThat(flowNode.currentNavTarget()).isEqualTo(SecurityAndPrivacyFlowNode.NavTarget.EditRoomAddress)
    }

    @Test
    fun `closeEditRoomAddress pops backstack`() = runTest {
        val flowNode = createFlowNode()
        flowNode.navigator.openEditRoomAddress()
        assertThat(flowNode.currentNavTarget()).isEqualTo(SecurityAndPrivacyFlowNode.NavTarget.EditRoomAddress)
        flowNode.navigator.closeEditRoomAddress()
        assertThat(flowNode.currentNavTarget()).isEqualTo(SecurityAndPrivacyFlowNode.NavTarget.SecurityAndPrivacy)
    }

    @Test
    fun `openManageAuthorizedSpaces navigates to ManageAuthorizedSpaces`() = runTest {
        val flowNode = createFlowNode()
        flowNode.navigator.openManageAuthorizedSpaces()
        assertThat(flowNode.currentNavTarget()).isEqualTo(SecurityAndPrivacyFlowNode.NavTarget.ManageAuthorizedSpaces)
    }

    @Test
    fun `closeManageAuthorizedSpaces pops backstack`() = runTest {
        val flowNode = createFlowNode()
        flowNode.navigator.openManageAuthorizedSpaces()
        assertThat(flowNode.currentNavTarget())
            .isInstanceOf(SecurityAndPrivacyFlowNode.NavTarget.ManageAuthorizedSpaces::class.java)
        flowNode.navigator.closeManageAuthorizedSpaces()
        assertThat(flowNode.currentNavTarget()).isEqualTo(SecurityAndPrivacyFlowNode.NavTarget.SecurityAndPrivacy)
    }

    @Test
    fun `onDone invokes callback`() = runTest {
        var onDoneCalled = false
        val callback = object : SecurityAndPrivacyEntryPoint.Callback {
            override fun onDone() {
                onDoneCalled = true
            }
        }
        val flowNode = createFlowNode(callback = callback)
        flowNode.navigator.onDone()
        assertThat(onDoneCalled).isTrue()
    }

    private fun createFlowNode(
        callback: SecurityAndPrivacyEntryPoint.Callback = object : SecurityAndPrivacyEntryPoint.Callback {
            override fun onDone() {}
        },
    ): SecurityAndPrivacyFlowNode {
        val buildContext = BuildContext(
            ancestryInfo = AncestryInfo.Root,
            savedStateMap = null,
            customisations = NodeCustomisationDirectoryImpl()
        )
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                initialRoomInfo = aRoomInfo(
                    joinRule = JoinRule.Invite,
                    historyVisibility = RoomHistoryVisibility.Shared
                )
            )
        )
        return SecurityAndPrivacyFlowNode(
            buildContext = buildContext,
            plugins = listOf(callback),
            room = room,
        )
    }

    private fun SecurityAndPrivacyFlowNode.currentNavTarget() = backstack.activeElement
}
