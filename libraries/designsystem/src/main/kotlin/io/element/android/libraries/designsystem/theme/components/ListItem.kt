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

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.theme.ElementTheme

@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
    hasError: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = { },
) {
    val headlineColor = when {
        hasError -> ElementTheme.colors.textCriticalPrimary
        !enabled -> ElementTheme.colors.textDisabled
        else -> ElementTheme.colors.textPrimary
    }
    val leadingContentColor = when {
        hasError -> ElementTheme.colors.iconCriticalPrimary
        !enabled -> ElementTheme.colors.iconDisabled
        else -> ElementTheme.colors.iconTertiary
    }

    val decoratedHeadlineContent: @Composable () -> Unit = {
        CompositionLocalProvider(
            LocalTextStyle provides ElementTheme.materialTypography.bodyLarge.forceLineHeight(),
            LocalContentColor provides headlineColor,
        ) {
            headlineContent()
        }
    }
    val decoratedSupportingContent: (@Composable () -> Unit)? = supportingContent?.let { content ->
        {
            CompositionLocalProvider(
                LocalTextStyle provides ElementTheme.materialTypography.bodyMedium.forceLineHeight(),
                LocalContentColor provides ElementTheme.materialColors.onSurfaceVariant,
            ) {
                content()
            }
        }
    }
    val decoratedLeadingContent: (@Composable () -> Unit)? = leadingContent?.let { content ->
        {
            CompositionLocalProvider(
                LocalContentColor provides leadingContentColor,
            ) {
                content()
            }
        }
    }
    val decoratedTrailingContent: (@Composable () -> Unit)? = trailingContent?.let { content ->
        {
            CompositionLocalProvider(
                LocalContentColor provides ElementTheme.materialColors.primary,
            ) {
                content()
            }
        }
    }
    androidx.compose.material3.ListItem(
        headlineContent = decoratedHeadlineContent,
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        overlineContent = overlineContent,
        supportingContent = decoratedSupportingContent,
        leadingContent = decoratedLeadingContent,
        trailingContent = decoratedTrailingContent,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    )
}

// region: Simple list item
@Preview(name = "List item (3 lines) - Simple", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesSimplePreview() = PreviewItems.ThreeLinesListItemPreview()

@Preview(name = "List item (2 lines) - Simple", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesSimplePreview() = PreviewItems.TwoLinesListItemPreview()

@Preview(name = "List item (1 line) - Simple", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineSimplePreview()  = PreviewItems.OneLineListItemPreview()
// endregion

// region: Trailing Checkbox
@Preview(name = "List item (3 lines) - Trailing Checkbox", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesTrailingCheckBoxPreview() = PreviewItems.ThreeLinesListItemPreview(trailingContent = PreviewItems.checkbox())

@Preview(name = "List item (2 lines) - Trailing Checkbox", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesTrailingCheckBoxPreview() = PreviewItems.TwoLinesListItemPreview(trailingContent = PreviewItems.checkbox())

@Preview(name = "List item (1 line) - Trailing Checkbox", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineTrailingCheckBoxPreview() = PreviewItems.OneLineListItemPreview(trailingContent = PreviewItems.checkbox())
// endregion

// region: Trailing RadioButton
@Preview(name = "List item (3 lines) - Trailing RadioButton", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesTrailingRadioButtonPreview() = PreviewItems.ThreeLinesListItemPreview(trailingContent = PreviewItems.radioButton())

@Preview(name = "List item (2 lines) - Trailing RadioButton", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesTrailingRadioButtonPreview() = PreviewItems.TwoLinesListItemPreview(trailingContent = PreviewItems.radioButton())

@Preview(name = "List item (1 line) - Trailing RadioButton", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineTrailingRadioButtonPreview() = PreviewItems.OneLineListItemPreview(trailingContent = PreviewItems.radioButton())
// endregion

// region: Trailing Switch
@Preview(name = "List item (3 lines) - Trailing Switch", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesTrailingSwitchPreview() = PreviewItems.ThreeLinesListItemPreview(trailingContent = PreviewItems.switch())

@Preview(name = "List item (2 lines) - Trailing Switch", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesTrailingSwitchPreview() = PreviewItems.TwoLinesListItemPreview(trailingContent = PreviewItems.switch())

@Preview(name = "List item (1 line) - Trailing Switch", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineTrailingSwitchPreview() = PreviewItems.OneLineListItemPreview(trailingContent = PreviewItems.switch())
// endregion

// region: Trailing Icon
@Preview(name = "List item (3 lines) - Trailing Icon", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesTrailingIconPreview() = PreviewItems.ThreeLinesListItemPreview(trailingContent = PreviewItems.icon())

@Preview(name = "List item (2 lines) - Trailing Icon", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesTrailingIconPreview() = PreviewItems.TwoLinesListItemPreview(trailingContent = PreviewItems.icon())

@Preview(name = "List item (1 line) - Trailing Icon", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineTrailingIconPreview() = PreviewItems.OneLineListItemPreview(trailingContent = PreviewItems.icon())
// endregion

// region: Leading Checkbox
@Preview(name = "List item (3 lines) - Leading Checkbox", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesLeadingCheckboxPreview() = PreviewItems.ThreeLinesListItemPreview(leadingContent = PreviewItems.checkbox())

@Preview(name = "List item (2 lines) - Leading Checkbox", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesLeadingCheckboxPreview() = PreviewItems.TwoLinesListItemPreview(leadingContent = PreviewItems.checkbox())

@Preview(name = "List item (1 line) - Leading Checkbox", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineLeadingCheckboxPreview() = PreviewItems.OneLineListItemPreview(leadingContent = PreviewItems.checkbox())
// endregion

// region: Leading RadioButton
@Preview(name = "List item (3 lines) - Leading RadioButton", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesLeadingRadioButtonPreview() = PreviewItems.ThreeLinesListItemPreview(leadingContent = PreviewItems.radioButton())

@Preview(name = "List item (2 lines) - Leading RadioButton", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesLeadingRadioButtonPreview() = PreviewItems.TwoLinesListItemPreview(leadingContent = PreviewItems.radioButton())

@Preview(name = "List item (1 line) - Leading RadioButton", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineLeadingRadioButtonPreview() = PreviewItems.OneLineListItemPreview(leadingContent = PreviewItems.radioButton())
// endregion

// region: Leading Switch
@Preview(name = "List item (3 lines) - Leading Switch", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesLeadingSwitchPreview() = PreviewItems.ThreeLinesListItemPreview(leadingContent = PreviewItems.switch())

@Preview(name = "List item (2 lines) - Leading Switch", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesLeadingSwitchPreview() = PreviewItems.TwoLinesListItemPreview(leadingContent = PreviewItems.switch())

@Preview(name = "List item (1 line) - Leading Switch", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineLeadingSwitchPreview() = PreviewItems.OneLineListItemPreview(leadingContent = PreviewItems.switch())
// endregion

// region: Leading Icon
@Preview(name = "List item (3 lines) - Leading Icon", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesLeadingIconPreview() = PreviewItems.ThreeLinesListItemPreview(leadingContent = PreviewItems.icon())

@Preview(name = "List item (2 lines) - Leading Icon", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesLeadingIconPreview() = PreviewItems.TwoLinesListItemPreview(leadingContent = PreviewItems.icon())

@Preview(name = "List item (1 line) - Leading Icon", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineLeadingIconPreview() = PreviewItems.OneLineListItemPreview(leadingContent = PreviewItems.icon())
// endregion

// region: Both Icons
@Preview(name = "List item (3 lines) - Both Icons", group = PreviewGroup.Lists)
@Composable
internal fun ListItemThreeLinesBothIconsPreview() = PreviewItems.ThreeLinesListItemPreview(
    leadingContent = PreviewItems.icon(),
    trailingContent = PreviewItems.icon()
)

@Preview(name = "List item (2 lines) - Both Icons", group = PreviewGroup.Lists)
@Composable
internal fun ListItemTwoLinesBothIconsPreview() = PreviewItems.TwoLinesListItemPreview(
    leadingContent = PreviewItems.icon(),
    trailingContent = PreviewItems.icon()
)

@Preview(name = "List item (1 line) - Both Icons", group = PreviewGroup.Lists)
@Composable
internal fun ListItemSingleLineBothIconsPreview() = PreviewItems.OneLineListItemPreview(
    leadingContent = PreviewItems.icon(),
    trailingContent = PreviewItems.icon()
)
// endregion

// region: Error state
@Preview(name = "List item - Error", group = PreviewGroup.Lists)
@Composable
internal fun ListItemErrorPreview() = PreviewItems.OneLineListItemPreview(hasError = true)

@Preview(name = "List item - Error & Icon", group = PreviewGroup.Lists)
@Composable
internal fun ListItemErrorWithIconPreview() = PreviewItems.OneLineListItemPreview(
    hasError = true,
    leadingContent = PreviewItems.icon(),
)
// endregion

// region: Disabled state
@Preview(name = "List item - Disabled", group = PreviewGroup.Lists)
@Composable
internal fun ListItemDisabledPreview() = PreviewItems.OneLineListItemPreview(enabled = false)

@Preview(name = "List item - Disabled & Icon", group = PreviewGroup.Lists)
@Composable
internal fun ListItemDisabledWithIconPreview() = PreviewItems.OneLineListItemPreview(
    enabled = false,
    leadingContent = PreviewItems.icon(),
)
// endregion

@Suppress("ModifierMissing")
private object PreviewItems {

    @Composable
    fun ThreeLinesListItemPreview(
        modifier: Modifier = Modifier,
        leadingContent: @Composable (() -> Unit)? = null,
        trailingContent: @Composable (() -> Unit)? = null,
    ) {
        ElementThemedPreview {
            ListItem(
                headlineContent = PreviewItems.headline(),
                supportingContent = PreviewItems.text(),
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                modifier = modifier,
            )
        }
    }

    @Composable
    fun TwoLinesListItemPreview(
        modifier: Modifier = Modifier,
        leadingContent: @Composable (() -> Unit)? = null,
        trailingContent: @Composable (() -> Unit)? = null,
    ) {
        ElementThemedPreview {
            ListItem(
                headlineContent = PreviewItems.headline(),
                supportingContent = PreviewItems.textSingleLine(),
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                modifier = modifier,
            )
        }
    }

    @Composable
    fun OneLineListItemPreview(
        modifier: Modifier = Modifier,
        leadingContent: @Composable (() -> Unit)? = null,
        trailingContent: @Composable (() -> Unit)? = null,
        enabled: Boolean = true,
        hasError: Boolean = false,
    ) {
        ElementThemedPreview {
            ListItem(
                headlineContent = PreviewItems.headline(),
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                enabled = enabled,
                hasError = hasError,
                modifier = modifier,
            )
        }
    }

    @Composable
    fun headline() = @Composable {
        Text("List item")
    }

    @Composable
    fun text() = @Composable {
        Text("Supporting line text lorem ipsum dolor sit amet, consectetur.")
    }

    @Composable
    fun textSingleLine() = @Composable {
        Text("Supporting line text lorem ipsum dolor sit amet, consectetur.", overflow = TextOverflow.Ellipsis, maxLines = 1)
    }

    @Composable
    fun checkbox() = @Composable {
        var checked by remember { mutableStateOf(false) }
        Checkbox(checked = checked, onCheckedChange = { checked = !checked })
    }

    @Composable
    fun radioButton() = @Composable {
        var checked by remember { mutableStateOf(false) }
        RadioButton(selected = checked, onClick = { checked = !checked })
    }

    @Composable
    fun switch() = @Composable {
        var checked by remember { mutableStateOf(false) }
        Switch(checked = checked, onCheckedChange = { checked = !checked })
    }

    @Composable
    fun icon() = @Composable {
        Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
    }
}
