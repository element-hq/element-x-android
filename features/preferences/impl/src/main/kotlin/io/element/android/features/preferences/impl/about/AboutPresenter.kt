/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.about

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter

@Inject
class AboutPresenter : Presenter<AboutState> {
    @Composable
    override fun present(): AboutState {
        return AboutState(
            elementLegals = getAllLegals(),
        )
    }
}
