/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.extensions.impl

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.persistentListOf

class ExtensionsPresenter @Inject constructor() {
    @Composable
    fun present(): ExtensionsState {
        // TODO: Fetch real state events of type "im.vector.modular.widgets" from the room
        val extensions = persistentListOf(
            ExtensionItem(
                name = "widget1",
                avatarUrl = null,
            ),
            ExtensionItem(
                name = "widget2",
                avatarUrl = null,
            ),
        )

        return ExtensionsState(
            extensions = extensions,
            eventSink = {},
        )
    }
}

