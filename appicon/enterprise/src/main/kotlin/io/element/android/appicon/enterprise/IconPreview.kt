/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appicon.enterprise

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
internal fun IconPreview() {
    Box {
        Image(painter = painterResource(id = R.mipmap.ic_launcher_background_enterprise), contentDescription = null)
        Image(
            modifier = Modifier.align(Alignment.Center),
            painter = painterResource(id = R.mipmap.ic_launcher_foreground_enterprise),
            contentDescription = null,
        )
    }
}

@Preview
@Composable
internal fun RoundIconPreview() {
    Box(modifier = Modifier.clip(shape = CircleShape)) {
        Image(painter = painterResource(id = R.mipmap.ic_launcher_background_enterprise), contentDescription = null)
        Image(
            modifier = Modifier.align(Alignment.Center),
            painter = painterResource(id = R.mipmap.ic_launcher_foreground_enterprise),
            contentDescription = null,
        )
    }
}
