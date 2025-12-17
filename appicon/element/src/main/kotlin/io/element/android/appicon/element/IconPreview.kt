/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appicon.element

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
internal fun IconPreview() {
    Box {
        Image(
            modifier = Modifier.matchParentSize(),
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
        )
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = null,
        )
    }
}

@Preview
@Composable
internal fun RoundIconPreview() {
    Box(modifier = Modifier.clip(shape = CircleShape)) {
        Image(
            modifier = Modifier.matchParentSize(),
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
        )
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = null,
        )
    }
}

@Preview
@Composable
internal fun MonochromeIconPreview() {
    Box(
        modifier = Modifier
            .background(Color(0xFF2F3133)),
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_monochrome),
            colorFilter = ColorFilter.tint(Color(0xFFC3E0F6)),
            contentDescription = null
        )
    }
}
