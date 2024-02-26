/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.designsystem.components.async

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon

/**
 * A helper to create [AsyncIndicatorView] with some defaults.
 */
@Stable
object AsyncIndicator {
    /**
     * A loading async indicator.
     * @param text The text to display.
     * @param modifier The modifier to apply to the indicator.
     */
    @Composable
    fun Loading(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        AsyncIndicatorView(
            modifier = modifier,
            text = text,
            spacing = 10.dp,
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .progressSemantics()
                    .size(12.dp),
                color = ElementTheme.colors.textPrimary,
                strokeWidth = 1.5.dp,
            )
        }
    }

    /**
     * A failure async indicator.
     * @param text The text to display.
     * @param modifier The modifier to apply to the indicator.
     */
    @Composable
    fun Failure(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        AsyncIndicatorView(
            modifier = modifier,
            text = text,
            spacing = defaultSpacing
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = CompoundIcons.Close(),
                contentDescription = null,
            )
        }
    }

    /**
     * A custom async indicator.
     * @param text The text to display.
     * @param modifier The modifier to apply to the indicator.
     * @param spacing The spacing between the leading content and the text.
     * @param leadingContent The leading content to display.
     */
    @Composable
    fun Custom(
        text: String,
        modifier: Modifier = Modifier,
        spacing: Dp = defaultSpacing,
        leadingContent: @Composable (() -> Unit)? = null,
    ) {
        AsyncIndicatorView(
            modifier = modifier,
            text = text,
            spacing = spacing,
            leadingContent = leadingContent,
        )
    }

    /**
     * A short duration to display indicators.
     */
    const val DURATION_SHORT = 3000L

    /**
     * A long duration to display indicators.
     */
    const val DURATION_LONG = 5000L

    private val defaultSpacing = 4.dp
}
