/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Announcement component following design system https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2002-2154.
 */
@Composable
fun Announcement(
    title: String,
    description: String?,
    type: AnnouncementType,
    modifier: Modifier = Modifier,
) {
    when (type) {
        is AnnouncementType.Informative -> InformativeAnnouncement(
            title = title,
            description = description,
            isError = type.isCritical,
            modifier = modifier,
        )
        is AnnouncementType.Actionable -> ActionableAnnouncement(
            title = title,
            description = description,
            actionText = type.actionText,
            onActionClick = type.onActionClick,
            onDismissClick = type.onDismissClick,
            modifier = modifier,
        )
    }
}

@Immutable
sealed interface AnnouncementType {
    data class Informative(val isCritical: Boolean = false) : AnnouncementType
    data class Actionable(
        val actionText: String,
        val onActionClick: () -> Unit,
        val onDismissClick: (() -> Unit)?,
    ) : AnnouncementType
}

@Composable
private fun ActionableAnnouncement(
    title: String,
    description: String?,
    actionText: String,
    onActionClick: () -> Unit,
    onDismissClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    AnnouncementSurface(modifier) {
        Column {
            TitleAndDescription(
                title = title,
                description = description,
                trailingContent = onDismissClick?.let {
                    {
                        Icon(
                            modifier = Modifier.clickable(onClick = onDismissClick),
                            imageVector = CompoundIcons.Close(),
                            contentDescription = stringResource(CommonStrings.action_close)
                        )
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            Button(
                text = actionText,
                size = ButtonSize.Medium,
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun InformativeAnnouncement(
    title: String,
    description: String?,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    AnnouncementSurface(modifier = modifier) {
        Row {
            Icon(
                imageVector = if (isError) CompoundIcons.ErrorSolid() else CompoundIcons.Info(),
                tint = if (isError) ElementTheme.colors.iconCriticalPrimary else ElementTheme.colors.iconPrimary,
                contentDescription = null,
            )
            Spacer(Modifier.width(12.dp))
            TitleAndDescription(
                title = title,
                description = description,
                titleColor = if (isError) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun TitleAndDescription(
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
    titleColor: Color = ElementTheme.colors.textPrimary,
    descriptionColor: Color = ElementTheme.colors.textSecondary,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Row {
            Text(
                text = title,
                style = ElementTheme.typography.fontBodyLgMedium,
                color = titleColor,
                modifier = Modifier.weight(1f),
            )
            if (trailingContent != null) {
                Spacer(Modifier.width(12.dp))
                trailingContent()
            }
        }
        if (description != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = descriptionColor,
            )
        }
    }
}

@Composable
private fun AnnouncementSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = 12.dp),
        color = ElementTheme.colors.bgSubtleSecondary
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AnnouncementPreview() = ElementPreview {
    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Announcement(
            title = "Headline",
            description = "Text description goes here.",
            type = AnnouncementType.Informative(isCritical = false),
        )
        Announcement(
            title = "Headline",
            description = "Text description goes here.",
            type = AnnouncementType.Informative(isCritical = true),
        )
        Announcement(
            title = "Headline",
            description = "Text description goes here.",
            type = AnnouncementType.Actionable(
                actionText = "Label",
                onActionClick = {},
                onDismissClick = {},
            ),
        )
    }
}
