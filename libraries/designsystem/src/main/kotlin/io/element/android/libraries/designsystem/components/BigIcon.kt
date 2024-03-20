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

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.bigIconDefaultBackgroundColor
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Compound component that display a big icon centered in a rounded square.
 */
object BigIcon {
    /**
     * The style of the [BigIcon].
     */
    @Immutable
    sealed interface Style {
        /**
         * The default style.
         *
         * @param vectorIcon the [ImageVector] to display
         * @param contentDescription the content description of the icon, if any. It defaults to `null`
         */
        data class Default(val vectorIcon: ImageVector, val contentDescription: String? = null) : Style

        /**
         * An alert style with a tinted background.
         */
        data object Alert : Style

        /**
         * An alert style with the default background color.
         */
        data object AlertSolid : Style

        /**
         * A success style with a tinted background.
         */
        data object Success : Style

        /**
         * A success style with the default background color.
         */
        data object SuccessSolid : Style
    }

    /**
     * Display a [BigIcon].
     *
     * @param style the style of the icon
     * @param modifier the modifier to apply to this layout
     */
    @Composable
    operator fun invoke(
        style: Style,
        modifier: Modifier = Modifier,
    ) {
        val backgroundColor = when (style) {
            is Style.Default -> ElementTheme.colors.bigIconDefaultBackgroundColor
            Style.AlertSolid, Style.SuccessSolid -> ElementTheme.colors.bgCanvasDefault
            Style.Alert -> ElementTheme.colors.bgCriticalSubtle
            Style.Success -> ElementTheme.colors.bgSuccessSubtle
        }
        val icon = when (style) {
            is Style.Default -> style.vectorIcon
            Style.Alert, Style.AlertSolid -> CompoundIcons.Error()
            Style.Success, Style.SuccessSolid -> CompoundIcons.CheckCircleSolid()
        }
        val contentDescription = when (style) {
            is Style.Default -> style.contentDescription
            Style.Alert, Style.AlertSolid -> stringResource(CommonStrings.common_error)
            Style.Success, Style.SuccessSolid -> stringResource(CommonStrings.common_success)
        }
        val iconTint = when (style) {
            is Style.Default -> ElementTheme.colors.iconSecondaryAlpha
            Style.Alert, Style.AlertSolid -> ElementTheme.colors.iconCriticalPrimary
            Style.Success, Style.SuccessSolid -> ElementTheme.colors.iconSuccessPrimary
        }
        Box(
            modifier = modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                tint = iconTint,
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun BigIconPreview() {
    ElementPreview {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(10.dp)) {
            val provider = BigIconStylePreviewProvider()
            for (style in provider.values) {
                BigIcon(style = style)
            }
        }
    }
}

internal class BigIconStylePreviewProvider : PreviewParameterProvider<BigIcon.Style> {
    override val values: Sequence<BigIcon.Style>
        get() = sequenceOf(
            BigIcon.Style.Default(Icons.Filled.CatchingPokemon),
            BigIcon.Style.Alert,
            BigIcon.Style.AlertSolid,
            BigIcon.Style.Success,
            BigIcon.Style.SuccessSolid
        )
}
