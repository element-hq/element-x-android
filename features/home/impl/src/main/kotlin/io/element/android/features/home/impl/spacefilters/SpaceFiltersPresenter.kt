/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spacefilters

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter

@Inject
class SpaceFiltersPresenter : Presenter<SpaceFiltersState> {
    @Composable
    override fun present(): SpaceFiltersState {
        return SpaceFiltersState(
            eventSink = {},
        )
    }
}
