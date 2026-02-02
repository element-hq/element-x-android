/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spacefilters

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@Composable
fun SpaceFiltersView(
    state: SpaceFiltersState,
    modifier: Modifier = Modifier
) {
    // TODO
}

@PreviewsDayNight
@Composable
internal fun SpaceFiltersViewPreview(@PreviewParameter(SpaceFiltersStateProvider::class) state: SpaceFiltersState) = ElementPreview {
    SpaceFiltersView(state = state)
}
