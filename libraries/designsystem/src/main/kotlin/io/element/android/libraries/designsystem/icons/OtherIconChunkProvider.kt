/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.icons

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.R
import kotlinx.collections.immutable.toPersistentList

internal class OtherIconChunkProvider : PreviewParameterProvider<IconChunk> {
    // This list and all the drawable it contains should be removed at some point.
    // All the icons should be defined in Compound.
    private val iconsOther = listOf(
        R.drawable.ic_notification,
        R.drawable.ic_stop,
        R.drawable.pin,
    )

    override val values: Sequence<IconChunk>
        get() {
            val chunks = iconsOther.chunked(36)
            return chunks.mapIndexed { index, chunk ->
                IconChunk(index = index + 1, total = chunks.size, icons = chunk.toPersistentList())
            }
                .asSequence()
        }
}
