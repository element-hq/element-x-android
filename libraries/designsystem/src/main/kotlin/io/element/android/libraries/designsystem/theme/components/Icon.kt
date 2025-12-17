/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

/**
 * Icon is a wrapper around [androidx.compose.material3.Icon] which allows to use
 * [ImageVector], [ImageBitmap] or [DrawableRes] as icon source.
 *
 * @param contentDescription the content description to be used for accessibility
 * @param modifier the modifier to apply to this layout
 * @param tint the tint to apply to the icon
 * @param imageVector the image vector of the icon to display, exclusive with [bitmap] and [resourceId]
 * @param bitmap the bitmap of the icon to display, exclusive with [imageVector] and [resourceId]
 * @param resourceId the resource id of the icon to display, exclusive with [imageVector] and [bitmap]
 */
@Composable
fun Icon(
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    imageVector: ImageVector? = null,
    bitmap: ImageBitmap? = null,
    @DrawableRes resourceId: Int? = null,
) {
    when {
        imageVector != null -> {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
        bitmap != null -> {
            Icon(
                bitmap = bitmap,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
        resourceId != null -> {
            Icon(
                resourceId = resourceId,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
    }
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun Icon(
    bitmap: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    androidx.compose.material3.Icon(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun Icon(
    @DrawableRes resourceId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    androidx.compose.material3.Icon(
        imageVector = ImageVector.vectorResource(id = resourceId),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun Icon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    androidx.compose.material3.Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

@Preview(group = PreviewGroup.Icons)
@Composable
internal fun IconImageVectorPreview() = ElementThemedPreview {
    Icon(imageVector = CompoundIcons.Close(), contentDescription = null)
}

@Preview(group = PreviewGroup.Icons)
@Composable
internal fun AllIconsPreview() = ElementPreview {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Adaptive(32.dp),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CompoundIcons.allResIds.forEach { icon ->
            item {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                )
            }
        }
    }
}
