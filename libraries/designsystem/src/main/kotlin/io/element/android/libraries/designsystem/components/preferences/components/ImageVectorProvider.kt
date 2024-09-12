/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.preferences.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class ImageVectorProvider : PreviewParameterProvider<ImageVector?> {
    override val values: Sequence<ImageVector?>
        get() = sequenceOf(
            Icons.Default.BugReport,
            null,
        )
}
