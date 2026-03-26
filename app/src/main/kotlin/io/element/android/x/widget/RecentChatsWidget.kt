/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.element.android.x.MainActivity

private val ElementGreen = Color(0xFF0DBD8B)
private val BadgeRed = Color(0xFFFF3B30)

private val SessionIdKey = ActionParameters.Key<String>("session_id")
private val RoomIdKey = ActionParameters.Key<String>("room_id")

class OpenChatActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val sessionId = parameters[SessionIdKey]
        val roomId = parameters[RoomIdKey]
        val intent = if (!sessionId.isNullOrEmpty() && !roomId.isNullOrEmpty()) {
            val deepLinkUri = Uri.parse(
                "elementx://open/${Uri.encode(sessionId)}/${Uri.encode(roomId)}"
            )
            Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                setClass(context, MainActivity::class.java)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        } else {
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }
}

class RecentChatsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val chats = RecentChatsDataStore.loadChats(context)
        provideContent {
            GlanceTheme {
                RecentChatsContent(chats)
            }
        }
    }

    @Composable
    private fun RecentChatsContent(chats: List<WidgetChatItem>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(24.dp)
                .padding(8.dp),
        ) {
            // Header
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Element",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onSurface,
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(
                    text = "+ New Chat",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary,
                    ),
                    modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp))

            if (chats.isEmpty()) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(actionStartActivity<MainActivity>()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "No recent chats",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "Tap to open Element",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(chats) { chat ->
                        ChatItemRow(chat)
                        Spacer(modifier = GlanceModifier.height(2.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun ChatItemRow(chat: WidgetChatItem) {
        val avatarColors = listOf(
            Color(0xFF0DBD8B), // green
            Color(0xFF368BD6), // blue
            Color(0xFFAC3BA8), // purple
            Color(0xFFE64F7A), // pink
            Color(0xFFFF812D), // orange
            Color(0xFF2DC50A), // lime
        )
        val avatarColor = avatarColors[chat.roomName.hashCode().absoluteValue % avatarColors.size]

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .clickable(
                    actionRunCallback<OpenChatActionCallback>(
                        actionParametersOf(
                            SessionIdKey to chat.sessionId,
                            RoomIdKey to chat.roomId,
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar placeholder (circle with initial)
            Column(
                modifier = GlanceModifier
                    .size(40.dp)
                    .cornerRadius(20.dp)
                    .background(ColorProvider(avatarColor)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = chat.avatarInitial,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ColorProvider(Color.White),
                    ),
                )
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            // Name + last message
            Column(modifier = GlanceModifier.defaultWeight()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = chat.roomName,
                        style = TextStyle(
                            fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurface,
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    Text(
                        text = chat.timestamp,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
                if (chat.lastMessage.isNotEmpty()) {
                    val preview = if (chat.senderName.isNotEmpty() && chat.lastMessage.isNotEmpty()) {
                        "${chat.senderName}: ${chat.lastMessage}"
                    } else {
                        chat.lastMessage
                    }
                    Text(
                        text = preview,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                        maxLines = 1,
                    )
                }
            }

            // Unread badge
            if (chat.unreadCount > 0) {
                Spacer(modifier = GlanceModifier.width(8.dp))
                Column(
                    modifier = GlanceModifier
                        .size(20.dp)
                        .cornerRadius(10.dp)
                        .background(ColorProvider(BadgeRed)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White),
                        ),
                    )
                }
            }
        }
    }
}
