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

package io.element.android.features.location.api.internal

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

/**
 * Horizontally aligns the content to the center of the space.
 * Vertically aligns the bottom edge of the content to the center of the space.
 */
fun Modifier.centerBottomEdge(scope: BoxScope): Modifier = with(scope) {
    then(
        Modifier.align { size, space, _ ->
            IntOffset(
                x = (space.width - size.width) / 2,
                y = space.height / 2 - size.height,
            )
        }
    )
}
