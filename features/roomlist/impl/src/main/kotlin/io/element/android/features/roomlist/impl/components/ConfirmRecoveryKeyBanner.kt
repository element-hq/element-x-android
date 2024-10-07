/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.features.roomlist.impl.R
import io.element.android.libraries.designsystem.atomic.molecules.DialogLikeBannerMolecule
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@Composable
internal fun ConfirmRecoveryKeyBanner(
    onContinueClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DialogLikeBannerMolecule(
        modifier = modifier,
        title = stringResource(R.string.confirm_recovery_key_banner_title),
        content = stringResource(R.string.confirm_recovery_key_banner_message),
        onSubmitClick = onContinueClick,
        onDismissClick = onDismissClick,
    )
}

@PreviewsDayNight
@Composable
internal fun ConfirmRecoveryKeyBannerPreview() = ElementPreview {
    ConfirmRecoveryKeyBanner(
        onContinueClick = {},
        onDismissClick = {},
    )
}
