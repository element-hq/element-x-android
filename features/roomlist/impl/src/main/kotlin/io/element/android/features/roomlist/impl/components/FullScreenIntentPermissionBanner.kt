/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.roomlist.impl.R
import io.element.android.libraries.designsystem.atomic.molecules.DialogLikeBannerMolecule
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.fullscreenintent.api.aFullScreenIntentPermissionsState

@Composable
fun FullScreenIntentPermissionBanner(state: FullScreenIntentPermissionsState) {
    DialogLikeBannerMolecule(
        title = stringResource(R.string.full_screen_intent_banner_title),
        content = stringResource(R.string.full_screen_intent_banner_message),
        onDismissClick = state.dismissFullScreenIntentBanner,
        onSubmitClick = state.openFullScreenIntentSettings,
    )
}

@PreviewsDayNight
@Composable
internal fun FullScreenIntentPermissionBannerPreview() {
    ElementPreview {
        FullScreenIntentPermissionBanner(aFullScreenIntentPermissionsState())
    }
}
