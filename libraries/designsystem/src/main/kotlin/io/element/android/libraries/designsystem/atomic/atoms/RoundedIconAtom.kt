/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.temporaryColorBgSpecial

/**
 * RoundedIconAtom is an atom which displays an icon inside a rounded container.
 *
 * @param modifier the modifier to apply to this layout
 * @param size the size of the icon
 * @param resourceId the resource id of the icon to display, exclusive with [imageVector]
 * @param imageVector the image vector of the icon to display, exclusive with [resourceId]
 * @param tint the tint to apply to the icon
 * @param backgroundTint the tint to apply to the icon background
 */
@Composable
fun RoundedIconAtom(
    modifier: Modifier = Modifier,
    size: RoundedIconAtomSize = RoundedIconAtomSize.Big,
    resourceId: Int? = null,
    imageVector: ImageVector? = null,
    tint: Color = MaterialTheme.colorScheme.secondary,
    backgroundTint: Color = ElementTheme.colors.temporaryColorBgSpecial,
) {
    Box(
        modifier = modifier
            .size(size.toContainerSize())
            .background(
                color = backgroundTint,
                shape = RoundedCornerShape(size.toCornerSize())
            )
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .size(size.toIconSize()),
            tint = tint,
            resourceId = resourceId,
            imageVector = imageVector,
            contentDescription = null,
        )
    }
}

private fun RoundedIconAtomSize.toContainerSize(): Dp {
    return when (this) {
        RoundedIconAtomSize.Medium -> 30.dp
        RoundedIconAtomSize.Big -> 36.dp
    }
}

private fun RoundedIconAtomSize.toCornerSize(): Dp {
    return when (this) {
        RoundedIconAtomSize.Medium -> 8.dp
        RoundedIconAtomSize.Big -> 8.dp
    }
}

private fun RoundedIconAtomSize.toIconSize(): Dp {
    return when (this) {
        RoundedIconAtomSize.Medium -> 16.dp
        RoundedIconAtomSize.Big -> 24.dp
    }
}

@PreviewsDayNight
@Composable
internal fun RoundedIconAtomPreview() = ElementPreview {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RoundedIconAtom(
            size = RoundedIconAtomSize.Medium,
            imageVector = Icons.Filled.Home,
        )
        RoundedIconAtom(
            size = RoundedIconAtomSize.Big,
            imageVector = Icons.Filled.Home,
        )
    }
}

enum class RoundedIconAtomSize {
    Medium,
    Big,
}
