/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.knockrequests.api.banner.KnockRequestsBannerRenderer
import io.element.android.libraries.di.RoomScope

@ContributesBinding(RoomScope::class)
class DefaultKnockRequestsBannerRenderer(
    private val presenter: KnockRequestsBannerPresenter,
) : KnockRequestsBannerRenderer {
    @Composable
    override fun View(modifier: Modifier, onViewRequestsClick: () -> Unit) {
        val state = presenter.present()
        KnockRequestsBannerView(
            state = state,
            onViewRequestsClick = onViewRequestsClick,
        )
    }
}
