/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.icons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@PreviewsDayNight
@Composable
internal fun IconsOtherPreview() = ElementPreview {
    IconsPreview(
        title = "Other icons",
        iconsList = iconsOther.toImmutableList(),
        iconNameTransform = { name ->
            name.removePrefix("ic_")
                .replace("_", " ")
        }
    )
}

@Composable
private fun IconsPreview(
    title: String,
    iconsList: ImmutableList<Int>,
    iconNameTransform: (String) -> String,
) = ElementPreview {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            style = ElementTheme.typography.fontHeadingSmMedium,
            text = title,
            textAlign = TextAlign.Center,
        )
        iconsList.chunked(6).forEach { iconsRow ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                iconsRow.forEach { icon ->
                    Column(
                        modifier = Modifier.width(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier.padding(2.dp),
                            resourceId = icon,
                            contentDescription = null,
                        )
                        Text(
                            text = iconNameTransform(
                                context.resources
                                    .getResourceEntryName(icon)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = ElementTheme.typography.fontBodyXsMedium,
                            color = ElementTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
