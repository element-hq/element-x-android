/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

@Composable
fun PinIcon(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(ElementTheme.colors.bgSubtlePrimary)
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .width(22.dp),
            resourceId = R.drawable.pin,
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun PinIconPreview() = ElementPreview {
    PinIcon()
}
