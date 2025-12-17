/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun VoiceMessageDeleteButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier
            .size(48.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = CompoundIcons.Delete(),
            contentDescription = stringResource(CommonStrings.a11y_delete),
            tint = if (enabled) {
                ElementTheme.colors.iconCriticalPrimary
            } else {
                ElementTheme.colors.iconDisabled
            },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun VoiceMessageDeleteButtonPreview() = ElementPreview {
    Row {
        VoiceMessageDeleteButton(
            enabled = true,
            onClick = {},
        )
        VoiceMessageDeleteButton(
            enabled = false,
            onClick = {},
        )
    }
}
