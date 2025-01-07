/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class OverlapRatioProvider : PreviewParameterProvider<Float> {
    override val values: Sequence<Float> = sequenceOf(
        0f,
        0.25f,
        0.5f,
        0.75f,
        1f
    )
}
