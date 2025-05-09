/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.icons

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.toPersistentList

internal class OtherIconChunkProvider : PreviewParameterProvider<IconChunk> {
    override val values: Sequence<IconChunk>
        get() {
            val chunks = iconsOther.chunked(36)
            return chunks.mapIndexed { index, chunk ->
                IconChunk(index = index + 1, total = chunks.size, icons = chunk.toPersistentList())
            }
                .asSequence()
        }
}
