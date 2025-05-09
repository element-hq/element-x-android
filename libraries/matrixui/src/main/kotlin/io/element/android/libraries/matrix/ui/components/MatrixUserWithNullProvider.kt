/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.user.MatrixUser

open class MatrixUserWithNullProvider : PreviewParameterProvider<MatrixUser?> {
    override val values: Sequence<MatrixUser?>
        get() = sequenceOf(
            aMatrixUser(),
            aMatrixUser().copy(displayName = null),
            null,
        )
}
