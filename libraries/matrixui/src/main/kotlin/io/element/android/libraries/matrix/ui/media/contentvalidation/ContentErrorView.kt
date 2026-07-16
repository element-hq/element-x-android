/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.contentvalidation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons

@Composable
internal fun ContentErrorView(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    val updatedOnTextLayout by rememberUpdatedState(onTextLayout)
    Row(
        modifier = modifier
            .background(color = ElementTheme.colors.bgCriticalSubtle)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = CompoundIcons.Error(),
            contentDescription = null,
            tint = ElementTheme.colors.iconCriticalPrimary
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = ElementTheme.colors.textCriticalPrimary,
                style = ElementTheme.typography.fontBodyMdMedium,
            )
            val textMeasurer = rememberTextMeasurer()
            val textContent = remember(message) {
                movableContentOf(
                    @Composable {
                        Text(
                            text = message,
                            color = ElementTheme.colors.textSecondary,
                            style = ElementTheme.typography.fontBodySmRegular,
                        )
                    }
                )
            }

            // BoxWithConstraints is needed to be able to calculate the text layout in the current constraints, so we can pass it to the onTextLayout callback.
            // However, this can't be used inside a SubComposeLayout (like the one in the text composer), so we only use it when the onTextLayout callback
            // is provided.
            if (updatedOnTextLayout != null) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    LaunchedEffect(message) {
                        updatedOnTextLayout?.invoke(textMeasurer.measure(message, overflow = TextOverflow.Visible, constraints = constraints))
                    }

                    textContent()
                }
            } else {
                textContent()
            }
        }
    }
}
