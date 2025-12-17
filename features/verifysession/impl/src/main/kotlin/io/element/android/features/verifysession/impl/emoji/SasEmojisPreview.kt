/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.emoji

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
@PreviewsDayNight
internal fun SasEmojisPreview() = ElementPreview {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        List(64) { it to it.toEmojiResource() }
            .chunked(8)
            .forEach {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    it.forEach { emoji ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                painter = painterResource(id = emoji.second.drawableRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                            )
                            Text(
                                text = emoji.first.toString() + ":" + stringResource(id = emoji.second.nameRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = ElementTheme.typography.fontBodySmRegular.copy(
                                    fontSize = 8.sp
                                )
                            )
                        }
                    }
                }
            }
    }
}
