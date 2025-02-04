/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.about

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class AboutStateProvider : PreviewParameterProvider<AboutState> {
    override val values: Sequence<AboutState>
        get() = sequenceOf(
            anAboutState(),
        )
}

fun anAboutState(
    elementLegals: List<ElementLegal> = getAllLegals(),
) = AboutState(
    elementLegals = elementLegals,
)
