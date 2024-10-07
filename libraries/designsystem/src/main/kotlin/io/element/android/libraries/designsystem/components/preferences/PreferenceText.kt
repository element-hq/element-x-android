/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.atomic.atoms.RedIndicatorAtom
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.components.preferenceIcon
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.toEnabledColor
import io.element.android.libraries.designsystem.toSecondaryEnabledColor

@Composable
fun PreferenceText(
    title: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subtitle: String? = null,
    subtitleAnnotated: AnnotatedString? = null,
    currentValue: String? = null,
    loadingCurrentValue: Boolean = false,
    icon: ImageVector? = null,
    @DrawableRes iconResourceId: Int? = null,
    showIconAreaIfNoIcon: Boolean = false,
    showIconBadge: Boolean = false,
    showEndBadge: Boolean = false,
    tintColor: Color? = null,
    onClick: () -> Unit = {},
) {
    ListItem(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        leadingContent = preferenceIcon(
            icon = icon,
            iconResourceId = iconResourceId,
            showIconBadge = showIconBadge,
            enabled = enabled,
            showIconAreaIfNoIcon = showIconAreaIfNoIcon,
            tintColor = tintColor,
        ),
        headlineContent = {
            Text(
                style = ElementTheme.typography.fontBodyLgRegular,
                text = title,
                color = tintColor ?: enabled.toEnabledColor(),
            )
        },
        supportingContent = if (subtitle != null) {
            {
                Text(
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = subtitle,
                    color = tintColor ?: enabled.toSecondaryEnabledColor(),
                )
            }
        } else {
            subtitleAnnotated?.let {
                {
                    Text(
                        style = ElementTheme.typography.fontBodyMdRegular,
                        text = it,
                        color = tintColor ?: enabled.toSecondaryEnabledColor(),
                    )
                }
            }
        },
        trailingContent = if (currentValue != null || loadingCurrentValue || showEndBadge) {
            ListItemContent.Custom {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentValue != null) {
                        Text(
                            text = currentValue,
                            style = ElementTheme.typography.fontBodyXsMedium,
                            color = enabled.toSecondaryEnabledColor(),
                        )
                    } else if (loadingCurrentValue) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .progressSemantics()
                                .size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    if (showEndBadge) {
                        val endBadgeStartPadding = if (currentValue != null || loadingCurrentValue) 16.dp else 0.dp
                        RedIndicatorAtom(
                            modifier = Modifier
                                .padding(start = endBadgeStartPadding)
                        )
                    }
                }
            }
        } else {
            null
        }
    )
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceTextLightPreview() = ElementPreviewLight {
    ContentToPreview(showEndBadge = false)
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceTextDarkPreview() = ElementPreviewDark {
    ContentToPreview(showEndBadge = false)
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceTextWithEndBadgeLightPreview() = ElementPreviewLight {
    ContentToPreview(showEndBadge = true)
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceTextWithEndBadgeDarkPreview() = ElementPreviewDark {
    ContentToPreview(showEndBadge = true)
}

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(showEndBadge: Boolean) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        PreferenceText(
            title = "Title",
            icon = CompoundIcons.ChatProblem(),
            showEndBadge = showEndBadge,
        )
        PreferenceText(
            title = "Title",
            subtitle = "Some content",
            icon = CompoundIcons.ChatProblem(),
            showEndBadge = showEndBadge,
        )
        PreferenceText(
            title = "Title",
            subtitle = "Some content",
            icon = CompoundIcons.ChatProblem(),
            currentValue = "123",
            showEndBadge = showEndBadge,
        )
        PreferenceText(
            title = "Title",
            subtitle = "Some content",
            icon = CompoundIcons.ChatProblem(),
            currentValue = "123",
            enabled = false,
            showEndBadge = showEndBadge,
        )
        PreferenceText(
            title = "Title",
            subtitle = "Some content",
            icon = CompoundIcons.ChatProblem(),
            loadingCurrentValue = true,
            showEndBadge = showEndBadge,
        )
        PreferenceText(
            title = "Title",
            icon = CompoundIcons.ChatProblem(),
            currentValue = "123",
            showEndBadge = showEndBadge,
        )
        PreferenceText(
            title = "Title",
            icon = CompoundIcons.ChatProblem(),
            loadingCurrentValue = true,
            showEndBadge = showEndBadge,
        )
        PreferenceText(
            title = "Title no icon with icon area",
            showIconAreaIfNoIcon = true,
            loadingCurrentValue = true,
            showEndBadge = showEndBadge,
        )
        PreferenceText(
            title = "Title no icon",
            loadingCurrentValue = true,
            showEndBadge = showEndBadge,
        )
    }
}
