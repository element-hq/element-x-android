/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

// Designs: https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&node-id=425%3A24208&mode=design&t=G5hCfkLB6GgXDuWe-1

/**
 * A List Item component to be used in lists and menus with simple layouts, matching the Material 3 guidelines.
 * @param headlineContent The main content of the list item, usually a text.
 * @param modifier The modifier to be applied to the list item.
 * @param supportingContent The content to be displayed below the headline content.
 * @param leadingContent The content to be displayed before the headline content.
 * @param trailingContent The content to be displayed after the headline content.
 * @param style The style to use for the list item. This may change the color and text styles of the contents. [ListItemStyle.Default] is used by default.
 * @param enabled Whether the list item is enabled. When disabled, will change the color of the headline content and the leading content to use disabled tokens.
 * @param onClick The callback to be called when the list item is clicked.
 */
@Suppress("LongParameterList")
@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: ListItemContent? = null,
    trailingContent: ListItemContent? = null,
    style: ListItemStyle = ListItemStyle.Default,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val colors = ListItemDefaults.colors(
        containerColor = Color.Transparent,
        headlineColor = style.headlineColor(),
        leadingIconColor = style.leadingIconColor(),
        trailingIconColor = style.trailingIconColor(),
        supportingColor = style.supportingTextColor(),
        disabledHeadlineColor = ListItemDefaultColors.headlineDisabled,
        disabledLeadingIconColor = ListItemDefaultColors.iconDisabled,
        disabledTrailingIconColor = ListItemDefaultColors.iconDisabled,
    )
    ListItem(
        headlineContent = headlineContent,
        modifier = modifier,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = colors,
        enabled = enabled,
        onClick = onClick,
    )
}

/**
 * A List Item component to be used in lists and menus with simple layouts, matching the Material 3 guidelines.
 * @param headlineContent The main content of the list item, usually a text.
 * @param colors The colors to use for the list item. You can use [ListItemDefaults.colors] to create this.
 * @param modifier The modifier to be applied to the list item.
 * @param supportingContent The content to be displayed below the headline content.
 * @param leadingContent The content to be displayed before the headline content.
 * @param trailingContent The content to be displayed after the headline content.
 * @param enabled Whether the list item is enabled. When disabled, will change the color of the headline content and the leading content to use disabled tokens.
 * @param onClick The callback to be called when the list item is clicked.
 */
@Suppress("LongParameterList")
@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    colors: ListItemColors,
    modifier: Modifier = Modifier,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: ListItemContent? = null,
    trailingContent: ListItemContent? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    // We cannot just pass the disabled colors, they must be set manually: https://issuetracker.google.com/issues/280480132
    val headlineColor = if (enabled) colors.headlineColor else colors.disabledHeadlineColor
    val supportingColor = if (enabled) colors.supportingTextColor else colors.disabledHeadlineColor.copy(alpha = 0.80f)
    val leadingContentColor = if (enabled) colors.leadingIconColor else colors.disabledLeadingIconColor
    val trailingContentColor = if (enabled) colors.trailingIconColor else colors.disabledTrailingIconColor

    val decoratedHeadlineContent: @Composable () -> Unit = {
        CompositionLocalProvider(
            LocalTextStyle provides ElementTheme.materialTypography.bodyLarge,
            LocalContentColor provides headlineColor,
        ) {
            headlineContent()
        }
    }
    val decoratedSupportingContent: (@Composable () -> Unit)? = supportingContent?.let { content ->
        {
            CompositionLocalProvider(
                LocalTextStyle provides ElementTheme.materialTypography.bodyMedium,
                LocalContentColor provides supportingColor,
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
                content.View()
            }
        }
    }
    val decoratedTrailingContent: (@Composable () -> Unit)? = trailingContent?.let { content ->
        {
            CompositionLocalProvider(
                LocalTextStyle provides ElementTheme.typography.fontBodyMdRegular,
                LocalContentColor provides trailingContentColor,
            ) {
                content.View()
            }
        }
    }

    androidx.compose.material3.ListItem(
        headlineContent = decoratedHeadlineContent,
        modifier = if (onClick != null) {
            Modifier
                .clickable(enabled = enabled, onClick = onClick)
                .then(modifier)
        } else {
            modifier
        },
        overlineContent = null,
        supportingContent = decoratedSupportingContent,
        leadingContent = decoratedLeadingContent,
        trailingContent = decoratedTrailingContent,
        colors = colors,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    )
}

/**
 * The style to use for a [ListItem].
 */
@Immutable
sealed interface ListItemStyle {
    data object Default : ListItemStyle
    data object Primary : ListItemStyle
    data object Destructive : ListItemStyle

    @Composable
    fun headlineColor() = when (this) {
        Default, Primary -> ListItemDefaultColors.headline
        Destructive -> ElementTheme.colors.textCriticalPrimary
    }

    @Composable
    fun supportingTextColor() = when (this) {
        Default, Primary -> ListItemDefaultColors.supportingText
        // FIXME once we have a defined color for this value
        Destructive -> ElementTheme.colors.textCriticalPrimary.copy(alpha = 0.8f)
    }

    @Composable
    fun leadingIconColor() = when (this) {
        Default -> ListItemDefaultColors.icon
        Primary -> ElementTheme.colors.iconPrimary
        Destructive -> ElementTheme.colors.iconCriticalPrimary
    }

    @Composable
    fun trailingIconColor() = when (this) {
        Default -> ListItemDefaultColors.icon
        Primary -> ElementTheme.colors.iconPrimary
        Destructive -> ElementTheme.colors.iconCriticalPrimary
    }
}

object ListItemDefaultColors {
    val headline: Color @Composable get() = ElementTheme.colors.textPrimary
    val headlineDisabled: Color @Composable get() = ElementTheme.colors.textDisabled
    val supportingText: Color @Composable get() = ElementTheme.materialColors.onSurfaceVariant
    val icon: Color @Composable get() = ElementTheme.colors.iconTertiary
    val iconDisabled: Color @Composable get() = ElementTheme.colors.iconDisabled

    val colors: ListItemColors
        @Composable get() = ListItemDefaults.colors(
            headlineColor = headline,
            supportingColor = supportingText,
            leadingIconColor = icon,
            trailingIconColor = icon,
            disabledHeadlineColor = headlineDisabled,
            disabledLeadingIconColor = iconDisabled,
            disabledTrailingIconColor = iconDisabled,
        )
}

// region: Simple list item
@Preview(name = "List item (3 lines) - Simple", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesSimplePreview() = PreviewItems.ThreeLinesListItemPreview()

@Preview(name = "List item (2 lines) - Simple", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesSimplePreview() = PreviewItems.TwoLinesListItemPreview()

@Preview(name = "List item (1 line) - Simple", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineSimplePreview() = PreviewItems.OneLineListItemPreview()
// endregion

// region: Trailing Checkbox
@Preview(name = "List item (3 lines) - Trailing Checkbox", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesTrailingCheckBoxPreview() = PreviewItems.ThreeLinesListItemPreview(trailingContent = PreviewItems.checkbox())

@Preview(name = "List item (2 lines) - Trailing Checkbox", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesTrailingCheckBoxPreview() = PreviewItems.TwoLinesListItemPreview(trailingContent = PreviewItems.checkbox())

@Preview(name = "List item (1 line) - Trailing Checkbox", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineTrailingCheckBoxPreview() = PreviewItems.OneLineListItemPreview(trailingContent = PreviewItems.checkbox())
// endregion

// region: Trailing RadioButton
@Preview(name = "List item (3 lines) - Trailing RadioButton", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesTrailingRadioButtonPreview() = PreviewItems.ThreeLinesListItemPreview(trailingContent = PreviewItems.radioButton())

@Preview(name = "List item (2 lines) - Trailing RadioButton", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesTrailingRadioButtonPreview() = PreviewItems.TwoLinesListItemPreview(trailingContent = PreviewItems.radioButton())

@Preview(name = "List item (1 line) - Trailing RadioButton", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineTrailingRadioButtonPreview() = PreviewItems.OneLineListItemPreview(trailingContent = PreviewItems.radioButton())
// endregion

// region: Trailing Switch
@Preview(name = "List item (3 lines) - Trailing Switch", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesTrailingSwitchPreview() = PreviewItems.ThreeLinesListItemPreview(trailingContent = PreviewItems.switch())

@Preview(name = "List item (2 lines) - Trailing Switch", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesTrailingSwitchPreview() = PreviewItems.TwoLinesListItemPreview(trailingContent = PreviewItems.switch())

@Preview(name = "List item (1 line) - Trailing Switch", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineTrailingSwitchPreview() = PreviewItems.OneLineListItemPreview(trailingContent = PreviewItems.switch())
// endregion

// region: Trailing Icon
@Preview(name = "List item (3 lines) - Trailing Icon", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesTrailingIconPreview() = PreviewItems.ThreeLinesListItemPreview(trailingContent = PreviewItems.icon())

@Preview(name = "List item (2 lines) - Trailing Icon", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesTrailingIconPreview() = PreviewItems.TwoLinesListItemPreview(trailingContent = PreviewItems.icon())

@Preview(name = "List item (1 line) - Trailing Icon", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineTrailingIconPreview() = PreviewItems.OneLineListItemPreview(trailingContent = PreviewItems.icon())
// endregion

// region: Leading Checkbox
@Preview(name = "List item (3 lines) - Leading Checkbox", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesLeadingCheckboxPreview() = PreviewItems.ThreeLinesListItemPreview(leadingContent = PreviewItems.checkbox())

@Preview(name = "List item (2 lines) - Leading Checkbox", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesLeadingCheckboxPreview() = PreviewItems.TwoLinesListItemPreview(leadingContent = PreviewItems.checkbox())

@Preview(name = "List item (1 line) - Leading Checkbox", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineLeadingCheckboxPreview() = PreviewItems.OneLineListItemPreview(leadingContent = PreviewItems.checkbox())
// endregion

// region: Leading RadioButton
@Preview(name = "List item (3 lines) - Leading RadioButton", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesLeadingRadioButtonPreview() = PreviewItems.ThreeLinesListItemPreview(leadingContent = PreviewItems.radioButton())

@Preview(name = "List item (2 lines) - Leading RadioButton", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesLeadingRadioButtonPreview() = PreviewItems.TwoLinesListItemPreview(leadingContent = PreviewItems.radioButton())

@Preview(name = "List item (1 line) - Leading RadioButton", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineLeadingRadioButtonPreview() = PreviewItems.OneLineListItemPreview(leadingContent = PreviewItems.radioButton())
// endregion

// region: Leading Switch
@Preview(name = "List item (3 lines) - Leading Switch", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesLeadingSwitchPreview() = PreviewItems.ThreeLinesListItemPreview(leadingContent = PreviewItems.switch())

@Preview(name = "List item (2 lines) - Leading Switch", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesLeadingSwitchPreview() = PreviewItems.TwoLinesListItemPreview(leadingContent = PreviewItems.switch())

@Preview(name = "List item (1 line) - Leading Switch", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineLeadingSwitchPreview() = PreviewItems.OneLineListItemPreview(leadingContent = PreviewItems.switch())
// endregion

// region: Leading Icon
@Preview(name = "List item (3 lines) - Leading Icon", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesLeadingIconPreview() = PreviewItems.ThreeLinesListItemPreview(leadingContent = PreviewItems.icon())

@Preview(name = "List item (2 lines) - Leading Icon", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesLeadingIconPreview() = PreviewItems.TwoLinesListItemPreview(leadingContent = PreviewItems.icon())

@Preview(name = "List item (1 line) - Leading Icon", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineLeadingIconPreview() = PreviewItems.OneLineListItemPreview(leadingContent = PreviewItems.icon())
// endregion

// region: Both Icons
@Preview(name = "List item (3 lines) - Both Icons", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemThreeLinesBothIconsPreview() = PreviewItems.ThreeLinesListItemPreview(
    leadingContent = PreviewItems.icon(),
    trailingContent = PreviewItems.icon()
)

@Preview(name = "List item (2 lines) - Both Icons", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesBothIconsPreview() = PreviewItems.TwoLinesListItemPreview(
    leadingContent = PreviewItems.icon(),
    trailingContent = PreviewItems.icon()
)

@Preview(name = "List item (1 line) - Both Icons", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemSingleLineBothIconsPreview() = PreviewItems.OneLineListItemPreview(
    leadingContent = PreviewItems.icon(),
    trailingContent = PreviewItems.icon()
)
// endregion

// region: Primary action
@Preview(name = "List item - Primary action & Icon", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemPrimaryActionWithIconPreview() = PreviewItems.OneLineListItemPreview(
    style = ListItemStyle.Primary,
    leadingContent = PreviewItems.icon(),
)
// endregion

// region: Error state
@Preview(name = "List item (2 lines) - Simple - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesSimpleErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    style = ListItemStyle.Destructive
)

@Preview(name = "List item (2 lines) - Trailing Checkbox - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesTrailingCheckBoxErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    trailingContent = PreviewItems.checkbox(),
    style = ListItemStyle.Destructive,
)

@Preview(name = "List item (2 lines) - Trailing RadioButton - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesTrailingRadioButtonErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    trailingContent = PreviewItems.radioButton(),
    style = ListItemStyle.Destructive,
)

@Preview(name = "List item (2 lines) - Trailing Switch - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesTrailingSwitchErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    trailingContent = PreviewItems.switch(),
    style = ListItemStyle.Destructive,
)

@Preview(name = "List item (2 lines) - Trailing Icon - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesTrailingIconErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    trailingContent = PreviewItems.icon(),
    style = ListItemStyle.Destructive,
)

// region: Leading Checkbox
@Preview(name = "List item (2 lines) - Leading Checkbox - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesLeadingCheckboxErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    leadingContent = PreviewItems.checkbox(),
    style = ListItemStyle.Destructive,
)

@Preview(name = "List item (2 lines) - Leading RadioButton - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesLeadingRadioButtonErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    leadingContent = PreviewItems.radioButton(),
    style = ListItemStyle.Destructive,
)

@Preview(name = "List item (2 lines) - Leading Switch - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesLeadingSwitchErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    leadingContent = PreviewItems.switch(),
    style = ListItemStyle.Destructive,
)

@Preview(name = "List item (2 lines) - Leading Icon - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesLeadingIconErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    leadingContent = PreviewItems.icon(),
    style = ListItemStyle.Destructive,
)

@Preview(name = "List item (2 lines) - Both Icons - Error", group = PreviewGroup.ListItems)
@Composable
internal fun ListItemTwoLinesBothIconsErrorPreview() = PreviewItems.TwoLinesListItemPreview(
    leadingContent = PreviewItems.icon(),
    trailingContent = PreviewItems.icon(),
    style = ListItemStyle.Destructive,
)
// endregion

@Suppress("ModifierMissing")
private object PreviewItems {
    @Composable
    private fun EnabledDisabledElementThemedPreview(
        content: @Composable (Boolean) -> Unit,
    ) = ElementThemedPreview {
        Column {
            sequenceOf(true, false).forEach {
                content(it)
            }
        }
    }

    @Composable
    fun ThreeLinesListItemPreview(
        modifier: Modifier = Modifier,
        style: ListItemStyle = ListItemStyle.Default,
        leadingContent: ListItemContent? = null,
        trailingContent: ListItemContent? = null,
    ) {
        EnabledDisabledElementThemedPreview {
            ListItem(
                headlineContent = headline(),
                supportingContent = text(),
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                enabled = it,
                style = style,
                modifier = modifier,
            )
        }
    }

    @Composable
    fun TwoLinesListItemPreview(
        modifier: Modifier = Modifier,
        style: ListItemStyle = ListItemStyle.Default,
        leadingContent: ListItemContent? = null,
        trailingContent: ListItemContent? = null,
    ) {
        EnabledDisabledElementThemedPreview {
            ListItem(
                headlineContent = headline(),
                supportingContent = textSingleLine(),
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                enabled = it,
                style = style,
                modifier = modifier,
            )
        }
    }

    @Composable
    fun OneLineListItemPreview(
        modifier: Modifier = Modifier,
        style: ListItemStyle = ListItemStyle.Default,
        leadingContent: ListItemContent? = null,
        trailingContent: ListItemContent? = null,
    ) {
        EnabledDisabledElementThemedPreview {
            ListItem(
                headlineContent = headline(),
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                enabled = it,
                style = style,
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
    fun checkbox(): ListItemContent {
        var checked by remember { mutableStateOf(false) }
        return ListItemContent.Checkbox(checked = checked, onChange = { checked = !checked })
    }

    @Composable
    fun radioButton(): ListItemContent {
        var checked by remember { mutableStateOf(false) }
        return ListItemContent.RadioButton(selected = checked, onClick = { checked = !checked })
    }

    @Composable
    fun switch(): ListItemContent {
        var checked by remember { mutableStateOf(false) }
        return ListItemContent.Switch(checked = checked, onChange = { checked = !checked })
    }

    @Composable
    fun icon() = ListItemContent.Icon(
        iconSource = IconSource.Vector(CompoundIcons.ShareAndroid())
    )
}
