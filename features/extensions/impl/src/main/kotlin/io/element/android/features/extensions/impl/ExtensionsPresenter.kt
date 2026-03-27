/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.extensions.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.features.widget.api.WidgetActivityData
import io.element.android.features.widget.api.WidgetEntryPoint
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.StateEventType
import kotlinx.collections.immutable.toImmutableList
import org.json.JSONObject
import timber.log.Timber

class ExtensionsPresenter @Inject constructor(
    private val room: JoinedRoom,
    private val widgetEntryPoint: WidgetEntryPoint,
) {
    @Composable
    fun present(): ExtensionsState {
        val extensions = remember { mutableStateOf(emptyList<ExtensionItem>()) }

        LaunchedEffect(Unit) {
            room.getStateEvents(StateEventType.Custom("im.vector.modular.widgets"))
                .onSuccess { rawEvents ->
                    Timber.v("fetched room events. Result from room.getStateEvents $rawEvents")
                    extensions.value = rawEvents.mapNotNull { json ->
                        parseWidgetStateEvent(json)
                    }

                }
                .onFailure { error ->
                    Timber.e(error, "Failed to fetch widget state events")
                }
        }

        fun handleEvent(event: ExtensionsEvents) {
            when (event) {
                is ExtensionsEvents.OnExtensionClicked -> {
                    widgetEntryPoint.startWidget(
                        WidgetActivityData(
                            sessionId = room.sessionId,
                            roomId = room.roomId,
                            url = event.extension.url,
                            widgetName = event.extension.name,
                        )
                    )
                }
            }
        }

        return ExtensionsState(
            extensions = extensions.value.toImmutableList(),
            eventSink = ::handleEvent,
        )
    }

    private fun parseWidgetStateEvent(json: String): ExtensionItem? {
        Timber.v("Try parsing state event $json")
        return try {
            val jsonObject = JSONObject(json)
            val content = jsonObject.optJSONObject("content") ?: return null
            val name = content.optString("name").takeIf { it.isNotEmpty() } ?: return null
            val url = content.optString("url").takeIf { it.isNotEmpty() } ?: return null
            val avatarUrl = content.optString("avatar_url").takeIf { it.isNotEmpty() }
            ExtensionItem(
                name = name,
                avatarUrl = avatarUrl,
                url = url,
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse widget state event")
            null
        }
    }
}
