/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

// Designs: https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&node-id=425%3A24208&mode=design&t=G5hCfkLB6GgXDuWe-1

/**
 * List supporting text item. Used to display an explanation in the list with a pre-formatted style.
 * @param text The text to display.
 * @param modifier The modifier to be applied to the text.
 * @param contentPadding The padding to apply to the text. Default is [ListSupportingTextDefaults.Padding.Default].
 */
@Composable
fun ListSupportingText(
    text: String,
    modifier: Modifier = Modifier,
    contentPadding: ListSupportingTextDefaults.Padding = ListSupportingTextDefaults.Padding.Default,
) {
    Text(
        text = text,
        modifier = modifier.padding(contentPadding.paddingValues()),
        style = ElementTheme.typography.fontBodySmRegular,
        color = ElementTheme.colors.textSecondary,
    )
}

/**
 * List supporting text item. Used to display an explanation in the list with a pre-formatted style.
 * @param annotatedString The annotated string to display.
 * @param modifier The modifier to be applied to the text.
 * @param contentPadding The padding to apply to the text. Default is [ListSupportingTextDefaults.Padding.Default].
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun ListSupportingText(
    annotatedString: AnnotatedString,
    modifier: Modifier = Modifier,
    contentPadding: ListSupportingTextDefaults.Padding = ListSupportingTextDefaults.Padding.Default,
) {
    val style = ElementTheme.typography.fontBodySmRegular
        .copy(color = ElementTheme.colors.textSecondary)
    val paddedModifier = modifier.padding(contentPadding.paddingValues())
    ClickableLinkText(
        annotatedString = annotatedString,
        modifier = paddedModifier,
        style = style,
        linkify = false,
    )
}

object ListSupportingTextDefaults {
    /** Specifies the padding to use for the supporting text. */
    @Immutable
    sealed interface Padding {
        /** No padding. */
        data object None : Padding

        /** Default padding, it will align fine with a [ListItem] with no leading content. */
        data object Default : Padding

        /** It will align to a [ListItem] with an [Icon] or [Checkbox] as leading content. */
        data object SmallLeadingContent : Padding

        /** It will align to with a [ListItem] with a [Switch] as leading content. */
        data object LargeLeadingContent : Padding

        /** It will align to with a [ListItem] with a custom start [padding]. */
        data class Custom(val padding: Dp) : Padding

        private fun startPadding(): Dp = when (this) {
            None -> 0.dp
            Default -> 16.dp
            SmallLeadingContent -> 56.dp
            LargeLeadingContent -> 84.dp
            is Custom -> padding
        }

        private fun endPadding(): Dp = when (this) {
            None -> 0.dp
            else -> 24.dp
        }

        private fun bottomPadding(): Dp = when (this) {
            None -> 0.dp
            else -> 12.dp
        }

        fun paddingValues() = PaddingValues(
            top = 0.dp,
            bottom = bottomPadding(),
            start = startPadding(),
            end = endPadding()
        )
    }
}

@Preview(group = PreviewGroup.ListSections, name = "List supporting text - no padding")
@Composable
internal fun ListSupportingTextNoPaddingPreview() {
    ElementThemedPreview {
        ListSupportingText(
            text = "Supporting line text lorem ipsum dolor sit amet, consectetur. Read more",
            contentPadding = ListSupportingTextDefaults.Padding.None,
        )
    }
}

@Preview(group = PreviewGroup.ListSections, name = "List supporting text - default padding")
@Composable
internal fun ListSupportingTextDefaultPaddingPreview() {
    ElementThemedPreview {
        Column {
            ListItem(headlineContent = { Text("A title") })
            ListSupportingText(
                text = "Supporting line text lorem ipsum dolor sit amet, consectetur. Read more",
                contentPadding = ListSupportingTextDefaults.Padding.Default,
            )
        }
    }
}

@Preview(group = PreviewGroup.ListSections, name = "List supporting text - small padding")
@Composable
internal fun ListSupportingTextSmallPaddingPreview() {
    ElementThemedPreview {
        Column {
            ListItem(
                headlineContent = { Text("A title") },
                leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.ShareAndroid()))
            )
            ListSupportingText(
                text = "Supporting line text lorem ipsum dolor sit amet, consectetur. Read more",
                contentPadding = ListSupportingTextDefaults.Padding.SmallLeadingContent,
            )
        }
    }
}

@Preview(group = PreviewGroup.ListSections, name = "List supporting text - large padding")
@Composable
internal fun ListSupportingTextLargePaddingPreview() {
    ElementThemedPreview {
        Column {
            ListItem(headlineContent = { Text("A title") }, leadingContent = ListItemContent.Switch(checked = true))
            ListSupportingText(
                text = "Supporting line text lorem ipsum dolor sit amet, consectetur. Read more",
                contentPadding = ListSupportingTextDefaults.Padding.LargeLeadingContent,
            )
        }
    }
}

@Preview(group = PreviewGroup.ListSections, name = "List supporting text - custom padding")
@Composable
internal fun ListSupportingTextCustomPaddingPreview() {
    ElementThemedPreview {
        Column {
            ListItem(headlineContent = { Text("A title") })
            ListSupportingText(
                text = "Supporting line text lorem ipsum dolor sit amet, consectetur. Read more",
                contentPadding = ListSupportingTextDefaults.Padding.Custom(24.dp),
            )
        }
    }
}
