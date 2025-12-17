/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Compound component that display a big icon centered in a rounded square.
 * Figma: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=1960-553&node-type=frame&m=dev
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
         * @param useCriticalTint whether the icon and background should be rendered using critical tint
         * @param usePrimaryTint whether the icon should be rendered using primary tint
         */
        data class Default(
            val vectorIcon: ImageVector,
            val contentDescription: String? = null,
            val useCriticalTint: Boolean = false,
            val usePrimaryTint: Boolean = false,
        ) : Style

        /**
         * An alert style with a transparent background.
         */
        data object Alert : Style

        /**
         * An alert style with a tinted background.
         */
        data object AlertSolid : Style

        /**
         * A success style with a transparent background.
         */
        data object Success : Style

        /**
         * A success style with a tinted background.
         */
        data object SuccessSolid : Style

        /**
         * A loading style with the default background color.
         */
        data object Loading : Style
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
            is Style.Default -> if (style.useCriticalTint) {
                ElementTheme.colors.bgCriticalSubtle
            } else {
                ElementTheme.colors.bgSubtleSecondary
            }
            Style.Alert,
            Style.Success -> Color.Transparent
            Style.AlertSolid -> ElementTheme.colors.bgCriticalSubtle
            Style.SuccessSolid -> ElementTheme.colors.bgSuccessSubtle
            Style.Loading -> ElementTheme.colors.bgSubtleSecondary
        }
        Box(
            modifier = modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            if (style is Style.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(27.dp),
                    color = ElementTheme.colors.iconSecondary,
                    trackColor = Color.Transparent,
                    strokeWidth = 3.dp,
                )
            } else {
                val icon = when (style) {
                    is Style.Default -> style.vectorIcon
                    Style.Alert,
                    Style.AlertSolid -> CompoundIcons.ErrorSolid()
                    Style.Success,
                    Style.SuccessSolid -> CompoundIcons.CheckCircleSolid()
                    Style.Loading -> error("This should never be reached")
                }
                val contentDescription = when (style) {
                    is Style.Default -> style.contentDescription
                    Style.Alert,
                    Style.AlertSolid -> stringResource(CommonStrings.common_error)
                    Style.Success,
                    Style.SuccessSolid -> stringResource(CommonStrings.common_success)
                    Style.Loading -> error("This should never be reached")
                }
                val iconTint = when (style) {
                    is Style.Default -> if (style.useCriticalTint) {
                        ElementTheme.colors.iconCriticalPrimary
                    } else if (style.usePrimaryTint) {
                        ElementTheme.colors.iconPrimary
                    } else {
                        ElementTheme.colors.iconSecondary
                    }
                    Style.Alert,
                    Style.AlertSolid -> ElementTheme.colors.iconCriticalPrimary
                    Style.Success,
                    Style.SuccessSolid -> ElementTheme.colors.iconSuccessPrimary
                    Style.Loading -> error("This should never be reached")
                }

                Icon(
                    modifier = Modifier.size(32.dp),
                    tint = iconTint,
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun BigIconPreview() = ElementPreview {
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        columns = GridCells.Adaptive(minSize = 64.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(BigIconStyleProvider().values.toList()) { style ->
            Box(
                contentAlignment = Alignment.Center
            ) {
                BigIcon(style = style)
            }
        }
    }
}

internal class BigIconStyleProvider : PreviewParameterProvider<BigIcon.Style> {
    override val values: Sequence<BigIcon.Style>
        get() = sequenceOf(
            BigIcon.Style.Default(Icons.Filled.CatchingPokemon),
            BigIcon.Style.Alert,
            BigIcon.Style.AlertSolid,
            BigIcon.Style.Default(Icons.Filled.CatchingPokemon, useCriticalTint = true),
            BigIcon.Style.Success,
            BigIcon.Style.SuccessSolid,
            BigIcon.Style.Loading,
        )
}
