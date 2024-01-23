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

package io.element.android.libraries.designsystem.components.button

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.TextButton

/**
 * A sealed interface that represents the different visual styles that a button can have.
 */
@Immutable
sealed interface ButtonVisuals {
    val action: () -> Unit

    /**
     * Creates a [Button] composable based on the visual state.
     */
    @Composable
    fun Composable()

    data class Text(val text: String, override val action: () -> Unit) : ButtonVisuals {
        @Composable
        override fun Composable() {
            TextButton(text = text, onClick = action)
        }
    }
    data class Icon(val iconSource: IconSource, override val action: () -> Unit) : ButtonVisuals {
        @Composable
        override fun Composable() {
            IconButton(onClick = action) {
                Icon(iconSource.getPainter(), iconSource.contentDescription)
            }
        }
    }
}
