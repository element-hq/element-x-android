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

package io.element.android.libraries.textcomposer.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.text.applyScaleUp
import io.element.android.libraries.textcomposer.model.MessageComposerMode

@Composable
internal fun textInputRoundedCornerShape(
    composerMode: MessageComposerMode,
): RoundedCornerShape {
    val roundCornerSmall = 20.dp.applyScaleUp()
    val roundCornerLarge = 21.dp.applyScaleUp()

    val roundedCornerSize = if (composerMode is MessageComposerMode.Special) {
        roundCornerSmall
    } else {
        roundCornerLarge
    }

    val roundedCornerSizeState = animateDpAsState(
        targetValue = roundedCornerSize,
        animationSpec = tween(
            durationMillis = 100,
        ),
        label = "roundedCornerSizeAnimation"
    )
    return RoundedCornerShape(roundedCornerSizeState.value)
}
