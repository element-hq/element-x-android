/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.preview

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import io.element.android.libraries.designsystem.R

/**
 * I wanted to set up a FakeImageLoader as per https://github.com/coil-kt/coil/issues/1327
 * but it does not render in preview. In the meantime, you can use this trick to have image.
 */
@Composable
fun debugPlaceholder(
    @DrawableRes debugPreview: Int,
    nonDebugPainter: Painter? = null,
) = if (LocalInspectionMode.current) {
    painterResource(id = debugPreview)
} else {
    nonDebugPainter
}

@Composable
fun debugPlaceholderBackground(nonDebugPainter: Painter? = null): Painter? {
    return debugPlaceholder(debugPreview = R.drawable.sample_background, nonDebugPainter)
}

@Composable
fun debugPlaceholderAvatar(nonDebugPainter: Painter? = null): Painter? {
    return debugPlaceholder(debugPreview = R.drawable.sample_avatar, nonDebugPainter)
}
