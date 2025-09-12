/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.userprofile.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.userprofile.api.UserProfileEntryPoint
import io.element.android.features.verifysession.api.OutgoingVerificationEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultUserProfileEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultUserProfileEntryPoint()

        val parentNode = TestParentNode.create { buildContext, plugins ->
            UserProfileFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                sessionIdHolder = CurrentSessionIdHolder(FakeMatrixClient()),
                elementCallEntryPoint = object : ElementCallEntryPoint {
                    override fun startCall(callType: CallType) = lambdaError()
                    override suspend fun handleIncomingCall(
                        callType: CallType.RoomCall,
                        eventId: EventId,
                        senderId: UserId,
                        roomName: String?,
                        senderName: String?,
                        avatarUrl: String?,
                        timestamp: Long,
                        notificationChannelId: String,
                        textContent: String?
                    ) = lambdaError()
                },
                mediaViewerEntryPoint = object : MediaViewerEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
                outgoingVerificationEntryPoint = object : OutgoingVerificationEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
            )
        }
        val callback = object : UserProfileEntryPoint.Callback {
            override fun onOpenRoom(roomId: RoomId) {
                lambdaError()
            }
        }
        val params = UserProfileEntryPoint.Params(
            userId = A_USER_ID,
        )
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .params(params)
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(UserProfileFlowNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }
}
