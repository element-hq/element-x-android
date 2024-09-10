/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.fullscreenintent.test

import androidx.compose.runtime.Composable
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsPresenter
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState

class FakeFullScreenIntentPermissionsPresenter : FullScreenIntentPermissionsPresenter {
    var state = FullScreenIntentPermissionsState(
        permissionGranted = true,
        shouldDisplayBanner = false,
        dismissFullScreenIntentBanner = {},
        openFullScreenIntentSettings = {},
    )
    @Composable
    override fun present(): FullScreenIntentPermissionsState {
        return state
    }
}
