/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.verifysession.impl.emoji.toEmojiResource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.VerificationEmoji

@Composable
internal fun VerificationContentVerifying(
    data: SessionVerificationData,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        when (data) {
            is SessionVerificationData.Decimals -> {
                val text = data.decimals.joinToString(separator = " - ")
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = data.decimals.joinToString()
                        },
                    text = text,
                    style = ElementTheme.typography.fontHeadingLgBold,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
            }
            is SessionVerificationData.Emojis -> {
                // We want each row to have up to 4 emojis
                val rows = data.emojis.chunked(4)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    verticalArrangement = Arrangement.spacedBy(40.dp),
                ) {
                    rows.forEach { emojis ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (emoji in emojis) {
                                EmojiItemView(emoji = emoji, modifier = Modifier.widthIn(max = 60.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiItemView(emoji: VerificationEmoji, modifier: Modifier = Modifier) {
    val emojiResource = emoji.number.toEmojiResource()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = emojiResource.drawableRes),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = emojiResource.nameRes),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            overflow = TextOverflow.Visible,
            textAlign = TextAlign.Center,
        )
    }
}
