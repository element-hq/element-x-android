/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
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
import java.io.File
import timber.log.Timber

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
                "dottie://open/${Uri.encode(sessionId)}/${Uri.encode(roomId)}"
            )
            Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                setClass(context, MainActivity::class.java)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        } else {
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }
}

class NewChatActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val chats = RecentChatsDataStore.loadChats(context)
        val sessionId = chats.firstOrNull()?.sessionId
        val intent = if (!sessionId.isNullOrEmpty()) {
            val deepLinkUri = Uri.parse("dottie://open/${Uri.encode(sessionId)}/create-room")
            Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                setClass(context, MainActivity::class.java)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        } else {
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }
}

class OpenAppActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val chats = RecentChatsDataStore.loadChats(context)
        val sessionId = chats.firstOrNull()?.sessionId
        val intent = if (!sessionId.isNullOrEmpty()) {
            val deepLinkUri = Uri.parse("dottie://open/${Uri.encode(sessionId)}")
            Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                setClass(context, MainActivity::class.java)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        } else {
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }
}

class RefreshWidgetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        RecentChatsWidget().updateAll(context)
    }
}

class RecentChatsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val chats = RecentChatsDataStore.loadChats(context)

        // Load avatar bitmaps from local file paths (downloaded by DefaultRecentChatsWidgetService)
        val avatarBitmaps = mutableMapOf<String, Bitmap>()
        for (chat in chats) {
            val path = chat.avatarUrl ?: continue
            if (!path.startsWith("/")) continue // Skip non-file paths (e.g. stale mxc:// URLs)
            try {
                val file = File(path)
                if (file.exists()) {
                    BitmapFactory.decodeFile(path)?.let { bitmap ->
                        avatarBitmaps[chat.roomId] = bitmap
                    }
                }
            } catch (e: Exception) {
                Timber.d(e, "Widget: Failed to load avatar for ${chat.roomId}")
            }
        }

        provideContent {
            GlanceTheme {
                RecentChatsContent(chats, avatarBitmaps)
            }
        }
    }

    @Composable
    private fun RecentChatsContent(chats: List<WidgetChatItem>, avatarBitmaps: Map<String, Bitmap>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(24.dp)
                .padding(8.dp),
        ) {
            // Header: [App icon] [Element X (N) → open app] [↻ refresh] [✎ compose]
            WidgetHeader(chatCount = chats.size)
            Spacer(modifier = GlanceModifier.height(4.dp))

            if (chats.isEmpty()) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(actionRunCallback<NewChatActionCallback>()),
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
                        text = "Tap to open Dottie",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(chats) { chat ->
                        ChatItemRow(chat, avatarBitmaps[chat.roomId])
                        Spacer(modifier = GlanceModifier.height(2.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetHeader(chatCount: Int) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(12.dp)
                .background(GlanceTheme.colors.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // App icon
            Image(
                provider = ImageProvider(io.element.android.appicon.element.R.mipmap.ic_launcher_round),
                contentDescription = "Dottie",
                modifier = GlanceModifier.size(24.dp).cornerRadius(12.dp),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            // "Dottie (N)" — clickable to open app
            Text(
                text = "Dottie ($chatCount)",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(actionRunCallback<OpenAppActionCallback>()),
            )
            // Refresh button
            Text(
                text = "\u21BB",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary,
                ),
                modifier = GlanceModifier
                    .padding(horizontal = 8.dp)
                    .clickable(actionRunCallback<RefreshWidgetActionCallback>()),
            )
            // Compose / new chat button
            Text(
                text = "\u270E",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary,
                ),
                modifier = GlanceModifier
                    .padding(start = 4.dp)
                    .clickable(actionRunCallback<NewChatActionCallback>()),
            )
        }
    }

    @Composable
    private fun ChatItemRow(chat: WidgetChatItem, avatarBitmap: Bitmap?) {
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
                .padding(horizontal = 8.dp, vertical = 12.dp)
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
            // Avatar: real image or fallback initial circle
            if (avatarBitmap != null) {
                Image(
                    provider = ImageProvider(avatarBitmap),
                    contentDescription = chat.roomName,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier
                        .size(40.dp)
                        .cornerRadius(20.dp),
                )
            } else {
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
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            // Two-row layout: name+timestamp on row 1, message+badge on row 2
            Column(modifier = GlanceModifier.defaultWeight()) {
                // Row 1: [Name ......................... Timestamp]
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val displayName = if (chat.isFavorite) {
                        "\u2605 ${chat.roomName}"
                    } else {
                        chat.roomName
                    }
                    Text(
                        text = displayName,
                        style = TextStyle(
                            fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp,
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
                // Row 2: [Sender: message .............. Badge   ]
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val preview = if (chat.lastMessage.isNotEmpty()) {
                        if (chat.senderName.isNotEmpty()) {
                            "${chat.senderName}: ${chat.lastMessage}"
                        } else {
                            chat.lastMessage
                        }
                    } else {
                        ""
                    }
                    Text(
                        text = preview,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    if (chat.unreadCount > 0) {
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Column(
                            modifier = GlanceModifier
                                .size(20.dp)
                                .cornerRadius(10.dp)
                                .background(GlanceTheme.colors.error),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlanceTheme.colors.onError,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
