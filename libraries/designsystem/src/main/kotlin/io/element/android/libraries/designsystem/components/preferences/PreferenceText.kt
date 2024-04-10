/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.components.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import io.element.android.libraries.designsystem.components.preferences.components.PreferenceIcon
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.toEnabledColor
import io.element.android.libraries.designsystem.toSecondaryEnabledColor

/**
 * Tried to use ListItem, but it cannot really match the design. Keep custom Layout for now.
 */
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
    val minHeight = if (subtitle == null && subtitleAnnotated == null) preferenceMinHeightOnlyTitle else preferenceMinHeight

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clickable { onClick() }
            .padding(horizontal = preferencePaddingHorizontal, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PreferenceIcon(
            icon = icon,
            iconResourceId = iconResourceId,
            showIconBadge = showIconBadge,
            enabled = enabled,
            isVisible = showIconAreaIfNoIcon,
            tintColor = tintColor ?: enabled.toSecondaryEnabledColor(),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                style = ElementTheme.typography.fontBodyLgRegular,
                text = title,
                color = tintColor ?: enabled.toEnabledColor(),
            )
            if (subtitle != null) {
                Text(
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = subtitle,
                    color = tintColor ?: enabled.toSecondaryEnabledColor(),
                )
            } else if (subtitleAnnotated != null) {
                Text(
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = subtitleAnnotated,
                    color = tintColor ?: enabled.toSecondaryEnabledColor(),
                )
            }
        }
        if (currentValue != null) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 16.dp, end = 8.dp),
                text = currentValue,
                style = ElementTheme.typography.fontBodyXsMedium,
                color = enabled.toSecondaryEnabledColor(),
            )
        } else if (loadingCurrentValue) {
            CircularProgressIndicator(
                modifier = Modifier
                    .progressSemantics()
                    .padding(start = 16.dp, end = 8.dp)
                    .size(20.dp)
                    .align(Alignment.CenterVertically),
                strokeWidth = 2.dp
            )
        }
        if (showEndBadge) {
            val endBadgeStartPadding = if (currentValue != null || loadingCurrentValue) 8.dp else 16.dp
            RedIndicatorAtom(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = endBadgeStartPadding)
            )
        }
    }
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
