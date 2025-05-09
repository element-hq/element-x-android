/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@PreviewsDayNight
@Composable
internal fun IconsCompoundPreview(@PreviewParameter(CompoundIconChunkProvider::class) chunk: IconChunk) = ElementPreview {
    IconsPreview(
        title = "R.drawable.ic_compound_* ${chunk.index}/${chunk.total}",
        iconsList = chunk.icons,
        iconNameTransform = { name ->
            name.removePrefix("ic_compound_")
                .replace("_", " ")
        }
    )
}
