/*
 * Copyright 2024 New Vector Ltd.
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
internal fun NativeSlidingSyncMigrationBanner(
    onContinueClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DialogLikeBannerMolecule(
        modifier = modifier,
        title = stringResource(R.string.banner_migrate_to_native_sliding_sync_title),
        content = stringResource(R.string.banner_migrate_to_native_sliding_sync_description),
        actionText = stringResource(R.string.banner_migrate_to_native_sliding_sync_action),
        onSubmitClick = onContinueClick,
        onDismissClick = onDismissClick,
    )
}

@PreviewsDayNight
@Composable
internal fun NativeSlidingSyncMigrationBannerPreview() = ElementPreview {
    NativeSlidingSyncMigrationBanner(
        onContinueClick = {},
        onDismissClick = {},
    )
}
