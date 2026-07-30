/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaRenderer

/**
 * An implementation of [LocalMediaRenderer] that displays a sample background image.
 * To be used for Previews only.
 */
internal class SampleMediaRenderer : LocalMediaRenderer {
    @Composable
    override fun Render(localMedia: LocalMedia) {
        Image(
            painter = painterResource(id = CommonDrawables.sample_background),
            modifier = Modifier.fillMaxSize(),
            contentDescription = null,
        )
    }
}
