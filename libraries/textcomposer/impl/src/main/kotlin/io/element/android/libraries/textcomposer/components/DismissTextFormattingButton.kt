/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun DismissTextFormattingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        modifier = modifier
            .size(48.dp),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(30.dp),
            resourceId = CommonDrawables.ic_cancel,
            contentDescription = stringResource(CommonStrings.action_close),
            tint = ElementTheme.colors.iconPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun DismissTextFormattingButtonPreview() = ElementPreview {
    DismissTextFormattingButton(onClick = {})
}
