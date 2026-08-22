/*
 * Copyright 2025 Feral
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Clean white Feral logo matching the iOS design.
 * No container or background — just the logo in white on the dark gradient.
 */
@Composable
fun FeralLogo(
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier.size(160.dp),
        painter = painterResource(id = io.element.android.features.login.impl.R.drawable.feral_logo_black),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(Color.White),
    )
}
