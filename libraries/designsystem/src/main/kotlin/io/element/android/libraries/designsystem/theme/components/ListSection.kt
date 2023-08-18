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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.theme.ElementTheme

// Designs:https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&node-id=425%3A24208&mode=design&t=G5hCfkLB6GgXDuWe-1

/**
 * List section header.
 * @param title The title of the section.
 * @param modifier The modifier to be applied to the section.
 * @param hasDivider Whether to show a divider above the section or not. Default is `true`.
 * @param description A description for the section. It's empty by default.
 */
@Composable
fun ListSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    hasDivider: Boolean = true,
    description: @Composable () -> Unit = {},
) {
    Column(modifier.fillMaxWidth()) {
        if (hasDivider) {
            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = ElementTheme.typography.fontBodyLgMedium,
                color = ElementTheme.colors.textPrimary,
            )
            CompositionLocalProvider(
                LocalTextStyle provides ElementTheme.typography.fontBodySmRegular,
                LocalContentColor provides ElementTheme.colors.textSecondary,
            ) {
                description()
            }
        }
    }
}

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
@Composable
fun ListSupportingText(
    annotatedString: AnnotatedString,
    modifier: Modifier = Modifier,
    contentPadding: ListSupportingTextDefaults.Padding = ListSupportingTextDefaults.Padding.Default,
) {
    Text(
        text = annotatedString,
        modifier = modifier.padding(contentPadding.paddingValues()),
        style = ElementTheme.typography.fontBodySmRegular,
        color = ElementTheme.colors.textSecondary,
    )
}

object ListSupportingTextDefaults {

    /** Specifies the padding to use for the supporting text. */
    sealed interface Padding {
        /** No padding. */
        object None : Padding
        /** Default padding, it will align fine with a [ListItem] with no leading content. */
        object Default : Padding
        /** It will align to a [ListItem] with an [Icon] or [Checkbox] as leading content. */
        object SmallLeadingContent : Padding
        /** It will align to with a [ListItem] with a [Switch] as leading content. */
        object LargeLeadingContent : Padding
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

// region: List header previews

@Preview(group = PreviewGroup.ListSections, name = "List section header")
@Composable
internal fun ListSectionHeaderPreview() {
    ElementThemedPreview {
        ListSectionHeader(
            title = "List section",
            hasDivider = false,
        )
    }
}

@Preview(group = PreviewGroup.ListSections, name = "List section header with divider")
@Composable
internal fun ListSectionHeaderWithDividerPreview() {
    ElementThemedPreview {
        ListSectionHeader(
            title = "List section",
            hasDivider = true,
        )
    }
}

@Preview(group = PreviewGroup.ListSections, name = "List section header with description")
@Composable
internal fun ListSectionHeaderWithDescriptionPreview() {
    ElementThemedPreview {
        ListSectionHeader(
            title = "List section",
            description = {
                ListSupportingText(
                    text = "Supporting line text lorem ipsum dolor sit amet, consectetur. Read more",
                    contentPadding = ListSupportingTextDefaults.Padding.None,
                )
            },
            hasDivider = false,
        )
    }
}

@Preview(group = PreviewGroup.ListSections, name = "List section header with description and divider")
@Composable
internal fun ListSectionHeaderWithDescriptionAndDividerPreview() {
    ElementThemedPreview {
        ListSectionHeader(
            title = "List section",
            description = {
                ListSupportingText(
                    text = "Supporting line text lorem ipsum dolor sit amet, consectetur. Read more",
                    contentPadding = ListSupportingTextDefaults.Padding.None,
                )
            },
            hasDivider = true,
        )
    }
}

// endregion

// region: List supporting text previews

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
            ListItem(headlineContent = { Text("A title") }, leadingContent = { Icon(Icons.Default.Share, null) })
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
            ListItem(headlineContent = { Text("A title") }, leadingContent = { Switch(checked = true, onCheckedChange = null) })
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

// endregion
