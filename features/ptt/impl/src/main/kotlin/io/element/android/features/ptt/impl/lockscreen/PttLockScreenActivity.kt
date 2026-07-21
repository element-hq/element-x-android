/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.lockscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.zacsweers.metro.Inject
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.ptt.api.PttConnectionState
import io.element.android.features.ptt.api.PttSessionManager
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Full-screen, over-the-lock-screen PTT alert. Launched by a full-screen-intent notification when a
 * session goes live; lets the user join and hold-to-talk without unlocking. Reuses the app-scoped
 * [PttSessionManager], so it drives the same native Mumble session as the in-app UI.
 */
class PttLockScreenActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_SESSION_ID = "EXTRA_PTT_SESSION_ID"
        private const val EXTRA_ROOM_ID = "EXTRA_PTT_ROOM_ID"
        private const val EXTRA_ROOM_NAME = "EXTRA_PTT_ROOM_NAME"

        fun newIntent(context: Context, sessionId: SessionId, roomId: RoomId, roomName: String): Intent =
            Intent(context, PttLockScreenActivity::class.java).apply {
                putExtra(EXTRA_SESSION_ID, sessionId.value)
                putExtra(EXTRA_ROOM_ID, roomId.value)
                putExtra(EXTRA_ROOM_NAME, roomName)
            }
    }

    @Inject lateinit var pttSessionManager: PttSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings<PttLockScreenBindings>().inject(this)

        // Show over the lock screen and turn the screen on, like an incoming call.
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val sessionId = intent?.getStringExtra(EXTRA_SESSION_ID)?.let(::SessionId)
        val roomId = intent?.getStringExtra(EXTRA_ROOM_ID)?.let(::RoomId)
        val roomName = intent?.getStringExtra(EXTRA_ROOM_NAME).orEmpty()
        if (sessionId == null || roomId == null) {
            finish()
            return
        }

        setContent {
            ElementTheme {
                PttLockScreenContent(
                    roomName = roomName,
                    sessionManager = pttSessionManager,
                    onJoin = { pttSessionManager.start(sessionId, roomId) },
                    onLeave = {
                        pttSessionManager.stop()
                        finish()
                    },
                    onDismiss = { finish() },
                )
            }
        }
    }
}

// NOTE: strings hard-coded — debug-only Stage 1 prototype, not localised.
@Composable
private fun PttLockScreenContent(
    roomName: String,
    sessionManager: PttSessionManager,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sessionState by sessionManager.sessionState.collectAsState()
    val inSession = sessionState != null
    val isConnected = sessionState?.connection is PttConnectionState.Connected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ElementTheme.colors.bgCanvasDefault)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Push-to-talk",
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary,
        )
        Text(
            text = roomName.ifEmpty { "Session" },
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp),
        )

        if (!inSession) {
            Button(text = "Join", onClick = onJoin, modifier = Modifier.fillMaxWidth())
            Button(text = "Dismiss", onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        } else {
            Text(
                text = if (isConnected) "Connected — hold to talk" else "Connecting…",
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textPrimary,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(ElementTheme.colors.bgActionPrimaryRest)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                sessionManager.pressToTalk()
                                tryAwaitRelease()
                                sessionManager.releaseToTalk()
                            }
                        )
                    }
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Hold to talk",
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textOnSolidPrimary,
                )
            }
            Button(text = "Leave", onClick = onLeave, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
        }
    }
}
