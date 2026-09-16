/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.core.extensions.withBrackets
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import io.element.android.libraries.matrix.ui.media.contentvalidation.InvalidContentView
import io.element.android.libraries.matrix.ui.media.contentvalidation.NotFoundContentView
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun GalleryFileListItem(
    contentValidationValue: ContentValidationValue,
    caption: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        when (contentValidationValue) {
            ContentValidationValue.Invalid -> InvalidContentView(
                modifier = invalidContentModifier(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            )
            is ContentValidationValue.UnrecoverableError -> NotFoundContentView(
                modifier = invalidContentModifier(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            )
            else -> {
                content()
            }
        }

        if (caption != null) {
            CaptionView(caption)
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }
        HorizontalDivider()
    }
}

@Composable
private fun invalidContentModifier(): Modifier {
    return Modifier
        .border(width = 1.dp, color = ElementTheme.colors.borderCriticalSubtle, shape = RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp))
        .background(ElementTheme.colors.bgCriticalSubtle)
}

@Composable
internal fun GalleryFileListItemContent(
    name: String,
    formattedSize: String,
    icon: ImageVector,
    isValidating: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = ElementTheme.colors.bgSubtleSecondary,
                shape = RoundedCornerShape(12.dp),
            )
            .combinedClickable(
                onClick = onClick.takeIf { !isValidating } ?: {},
                onLongClick = onLongClick.takeIf { !isValidating } ?: {},
                onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
            )
            .onKeyboardContextMenuAction(onLongClick)
            .fillMaxWidth()
            .padding(start = 12.dp, end = 36.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconModifier = Modifier
            .background(
                color = ElementTheme.colors.bgActionSecondaryRest,
                shape = CircleShape,
            )
            .size(32.dp)
            .padding(6.dp)

        if (isValidating) {
            CircularProgressIndicator(
                modifier = iconModifier,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                modifier = iconModifier,
                imageVector = icon,
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (formattedSize.isNotEmpty()) {
            Text(
                text = formattedSize.withBrackets(),
                style = ElementTheme.typography.fontBodyLgRegular,
                color = ElementTheme.colors.textPrimary,
            )
        }
    }
}
