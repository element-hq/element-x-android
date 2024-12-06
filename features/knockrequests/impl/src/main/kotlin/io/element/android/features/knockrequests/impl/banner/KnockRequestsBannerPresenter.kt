/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.runtime.Composable
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class KnockRequestsBannerPresenter @Inject constructor(): Presenter<KnockRequestsBannerState> {
    @Composable
    override fun present(): KnockRequestsBannerState {
        return KnockRequestsBannerState.Hidden
    }
}
