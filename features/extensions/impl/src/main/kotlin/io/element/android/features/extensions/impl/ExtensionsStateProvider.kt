/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.extensions.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.toImmutableList

open class ExtensionsStateProvider : PreviewParameterProvider<ExtensionsState> {
    override val values: Sequence<ExtensionsState>
        get() = sequenceOf(
            anExtensionsState(),
            anExtensionsState(
                extensions = listOf(
                    anExtensionItem(name = "Alice's Bot", url = "https://example.com/bot"),
                    anExtensionItem(name = "Reminder", avatarUrl = "https://example.com/avatar.png", url = "https://example.com/reminder"),
                    anExtensionItem(name = "Poll Bot", url = "https://example.com/poll"),
                ),
            ),
        )
}

fun anExtensionsState(
    extensions: List<ExtensionItem> = emptyList(),
    eventSink: (ExtensionsEvents) -> Unit = {},
) = ExtensionsState(
    extensions = extensions.toImmutableList(),
    eventSink = eventSink,
)

fun anExtensionItem(
    name: String = "An extension",
    avatarUrl: String? = null,
    url: String = "https://example.com/widget",
) = ExtensionItem(
    name = name,
    avatarUrl = avatarUrl,
    url = url,
)
