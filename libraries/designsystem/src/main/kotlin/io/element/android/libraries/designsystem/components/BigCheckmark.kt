/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.bigCheckmarkBorderColor
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Compound component that displays a big checkmark centered in a rounded square.
 *
 * @param modifier the modifier to apply to this layout
 */
@Composable
fun BigCheckmark(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(120.dp),
        shape = RoundedCornerShape(14.dp),
        color = ElementTheme.colors.bgCanvasDefault,
        border = BorderStroke(1.dp, ElementTheme.colors.bigCheckmarkBorderColor),
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(72.dp),
                tint = ElementTheme.colors.iconSuccessPrimary,
                imageVector = CompoundIcons.CheckCircleSolid(),
                contentDescription = stringResource(CommonStrings.common_success)
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun BigCheckmarkPreview() {
    ElementPreview {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            BigCheckmark()
        }
    }
}
