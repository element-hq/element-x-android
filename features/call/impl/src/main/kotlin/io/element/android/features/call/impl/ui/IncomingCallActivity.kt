/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.call.impl.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.call.impl.di.CallBindings
import io.element.android.features.call.impl.services.CallNotificationData
import io.element.android.features.call.impl.utils.CallIntegrationManager
import io.element.android.features.call.impl.utils.CallState
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class IncomingCallActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_NOTIFICATION_DATA = "EXTRA_NOTIFICATION_DATA"
    }

    @Inject
    lateinit var elementCallEntryPoint: ElementCallEntryPoint

    @Inject
    lateinit var callIntegrationManager: CallIntegrationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applicationContext.bindings<CallBindings>().inject(this)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val notificationData = intent?.let {IntentCompat.getParcelableExtra(it, EXTRA_NOTIFICATION_DATA, CallNotificationData::class.java) }
        if (notificationData != null) {
            setContent {
                IncomingCallScreen(
                    notificationData = notificationData,
                    onAnswer = ::onAnswer,
                    onCancel = ::onCancel,
                )
            }
        } else {
            finish()
            return
        }

        callIntegrationManager.activeCall
            .filter { it?.callState !is CallState.Ringing }
            .onEach { finish() }
            .launchIn(lifecycleScope)
    }

    private fun onAnswer(notificationData: CallNotificationData) {
        elementCallEntryPoint.startCall(CallType.RoomCall(notificationData.sessionId, notificationData.roomId))
    }

    private fun onCancel() {
        callIntegrationManager.hungUpCall()
    }
    
    @Composable
    private fun IncomingCallScreen(
        notificationData: CallNotificationData,
        onAnswer: (CallNotificationData) -> Unit,
        onCancel: () -> Unit,
    ) {
        ElementTheme {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 120.dp)
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Avatar(
                        avatarData = AvatarData(
                            id = notificationData.senderId.value,
                            name = notificationData.senderName,
                            url = notificationData.avatarUrl,
                            size = AvatarSize.RoomHeader, // TODO create own
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = notificationData.senderName ?: notificationData.senderId.value,
                        style = ElementTheme.typography.fontHeadingLgBold,
                        textAlign = TextAlign.Center,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, end = 48.dp, bottom = 64.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        modifier = Modifier.size(64.dp),
                        onClick = { onAnswer(notificationData) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = ElementTheme.colors.iconSuccessPrimary,
                            contentColor = ElementTheme.colors.iconOnSolidPrimary,
                        )
                    ) {
                        Icon(imageVector = CompoundIcons.VideoCallSolid(), contentDescription = "Accept call")
                    }

                    FilledIconButton(
                        modifier = Modifier.size(64.dp),
                        onClick = onCancel,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = ElementTheme.colors.iconCriticalPrimary,
                            contentColor = ElementTheme.colors.iconOnSolidPrimary,
                        )
                    ) {
                        Icon(imageVector = CompoundIcons.Close(), contentDescription = "Reject call")
                    }
                }
            }
        }
    }
    
    @Composable
    @Preview
    internal fun IncomingCallScreenPreview() {
        ElementPreview {
            IncomingCallScreen(
                notificationData = CallNotificationData(
                    sessionId = SessionId("@alice:matrix.org"),
                    roomId = RoomId("!1234:matrix.org"),
                    eventId = EventId("\$asdadadsad:matrix.org"),
                    senderId = UserId("@bob:matrix.org"),
                    roomName = "A room",
                    senderName = "Bob",
                    avatarUrl = null,
                    notificationChannelId = "incoming_call",
                    timestamp = System.currentTimeMillis()
                ),
                onAnswer = {},
                onCancel = {},
            )
        }
    }
}
