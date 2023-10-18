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

package io.element.android.libraries.designsystem.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
fun alphaAnimation(
    fromAlpha: Float = 0f,
    toAlpha: Float = 1f,
    delayMillis: Int = 150,
    durationMillis: Int = 150,
    label: String = "AlphaAnimation",
): State<Float> {
    val firstAlpha = if (LocalInspectionMode.current) 1f else fromAlpha
    var alpha by remember { mutableFloatStateOf(firstAlpha) }
    LaunchedEffect(Unit) { alpha = toAlpha }
    return animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(
            delayMillis = delayMillis,
            durationMillis = durationMillis,
        ),
        label = label
    )
}
