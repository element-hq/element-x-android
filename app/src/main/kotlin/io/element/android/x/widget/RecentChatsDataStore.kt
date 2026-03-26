/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.widget

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object RecentChatsDataStore {
    private const val PREFS_NAME = "recent_chats_widget"
    private const val KEY_CHATS = "chats"
    private val json = Json { ignoreUnknownKeys = true }

    fun saveChats(context: Context, chats: List<WidgetChatItem>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CHATS, json.encodeToString(chats))
            .apply()
    }

    fun loadChats(context: Context): List<WidgetChatItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val data = prefs.getString(KEY_CHATS, null) ?: return emptyList()
        return try {
            json.decodeFromString(data)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
