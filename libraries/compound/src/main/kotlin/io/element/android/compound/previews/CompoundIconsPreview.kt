/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Preview(widthDp = 730, heightDp = 1920)
@Composable
internal fun IconsCompoundPreviewLight() = ElementTheme {
    IconsCompoundPreview()
}

@Preview(widthDp = 730, heightDp = 1920)
@Composable
internal fun IconsCompoundPreviewRtl() = ElementTheme {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        IconsCompoundPreview(
            title = "Compound Icons Rtl",
        )
    }
}

@Preview(widthDp = 730, heightDp = 1920)
@Composable
internal fun IconsCompoundPreviewDark() = ElementTheme(darkTheme = true) {
    IconsCompoundPreview()
}

@Composable
private fun IconsCompoundPreview(
    title: String = "Compound Icons",
) {
    val context = LocalContext.current
    val content: Sequence<@Composable ColumnScope.() -> Unit> = sequence {
        for (icon in CompoundIcons.allResIds) {
            yield {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = ImageVector.vectorResource(icon),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = context.resources.getResourceEntryName(icon)
                        .removePrefix("ic_compound_")
                        .replace("_", " "),
                    textAlign = TextAlign.Center,
                    style = ElementTheme.typography.fontBodyXsMedium,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    }
    IconsPreview(
        title = title,
        content = content.toImmutableList(),
    )
}

@Composable
internal fun IconsPreview(
    title: String,
    content: ImmutableList<@Composable ColumnScope.() -> Unit>,
) = Surface {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
            .width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            style = ElementTheme.typography.fontHeadingSmMedium,
            text = title,
            textAlign = TextAlign.Center,
        )
        content.chunked(10).forEach { chunk ->
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                // Keep same order of icons for an easier comparison of previews
                horizontalArrangement = Arrangement.Absolute.Left,
            ) {
                chunk.forEachIndexed { index, icon ->
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxHeight()
                            .width(64.dp)
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        icon()
                    }
                    if (index < chunk.size - 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
        }
    }
}
