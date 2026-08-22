/*
 * Copyright 2025 Feral
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton

/**
 * Frosted-glass button used on the Feral dark onboarding gradient (iOS parity):
 * 10% white fill, 25% white hairline border, 14dp corners, white serif label.
 * When [onDarkBackground] is false ("Add account" flow rendered on the regular theme)
 * it falls back to the upstream design-system [Button] so it stays legible in light theme.
 */
@Composable
fun FeralOnBoardingButton(
    text: String,
    onClick: () -> Unit,
    onDarkBackground: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showProgress: Boolean = false,
    leadingIcon: IconSource? = null,
) {
    if (!onDarkBackground) {
        Button(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            showProgress = showProgress,
            leadingIcon = leadingIcon,
        )
        return
    }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.White.copy(alpha = 0.4f),
        ),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f)),
    ) {
        when {
            showProgress -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(8.dp))
            }
            leadingIcon != null -> {
                androidx.compose.material3.Icon(
                    painter = leadingIcon.getPainter(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White,
                )
                Spacer(Modifier.width(8.dp))
            }
        }
        Text(
            text = text,
            style = FeralTypography.sectionTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

@Composable
fun FeralOnBoardingTextButton(
    text: String,
    onClick: () -> Unit,
    onDarkBackground: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!onDarkBackground) {
        TextButton(text = text, onClick = onClick, modifier = modifier)
        return
    }
    androidx.compose.material3.TextButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            style = FeralTypography.sectionTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
            color = Color.White.copy(alpha = 0.8f),
        )
    }
}
