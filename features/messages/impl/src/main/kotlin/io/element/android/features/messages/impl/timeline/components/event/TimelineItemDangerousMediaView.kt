/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons

@Composable
fun TimelineItemDangerousMediaView(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(ElementTheme.colors.bgCriticalSubtle).fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = CompoundIcons.Error(),
                contentDescription = null,
                tint = ElementTheme.colors.iconCriticalPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "This file is not safe",
                color = ElementTheme.colors.textCriticalPrimary,
                style = ElementTheme.typography.fontBodyLgMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Preview and download have been disabled.",
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodyMdRegular,
                textAlign = TextAlign.Center,
            )
        }
    }
}
