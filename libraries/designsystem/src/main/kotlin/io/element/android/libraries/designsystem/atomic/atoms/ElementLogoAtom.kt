/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.modifiers.blurCompat
import io.element.android.libraries.designsystem.modifiers.blurredShapeShadow
import io.element.android.libraries.designsystem.modifiers.canUseBlurMaskFilter
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@Composable
fun ElementLogoAtom(
    size: ElementLogoAtomSize,
    modifier: Modifier = Modifier,
    useBlurredShadow: Boolean = canUseBlurMaskFilter(),
    darkTheme: Boolean = ElementTheme.isLightTheme.not(),
) {
    val blur = if (darkTheme) 160.dp else 24.dp
    val shadowColor = if (darkTheme) size.shadowColorDark else size.shadowColorLight
    val logoShadowColor = if (darkTheme) size.logoShadowColorDark else size.logoShadowColorLight
    val backgroundColor = if (darkTheme) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.4f)
    val borderColor = if (darkTheme) Color.White.copy(alpha = 0.89f) else Color.White
    Box(
        modifier = modifier
            .size(size.outerSize)
            .border(size.borderWidth, borderColor, RoundedCornerShape(size.cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        if (useBlurredShadow) {
            Box(
                Modifier
                    .size(size.outerSize)
                    .blurredShapeShadow(
                        color = shadowColor,
                        cornerRadius = size.cornerRadius,
                        blurRadius = size.shadowRadius,
                        offsetY = 8.dp,
                    )
            )
        } else {
            Box(
                Modifier
                    .size(size.outerSize)
                    .shadow(
                        elevation = size.shadowRadius,
                        shape = RoundedCornerShape(size.cornerRadius),
                        clip = false,
                        ambientColor = shadowColor
                    )
            )
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(size.cornerRadius))
                .size(size.outerSize)
                .background(backgroundColor)
                .blurCompat(blur)
        )
        Image(
            modifier = Modifier
                .size(size.logoSize)
                // Do the same double shadow than on Figma...
                .shadow(
                    elevation = 35.dp,
                    clip = false,
                    shape = CircleShape,
                    ambientColor = logoShadowColor,
                )
                .shadow(
                    elevation = 35.dp,
                    clip = false,
                    shape = CircleShape,
                    ambientColor = Color(0x80000000),
                ),
            painter = painterResource(id = R.drawable.element_logo),
            contentDescription = null
        )
    }
}

sealed class ElementLogoAtomSize(
    val outerSize: Dp,
    val logoSize: Dp,
    val cornerRadius: Dp,
    val borderWidth: Dp,
    val logoShadowColorDark: Color,
    val logoShadowColorLight: Color,
    val shadowColorDark: Color,
    val shadowColorLight: Color,
    val shadowRadius: Dp,
) {
    data object Medium : ElementLogoAtomSize(
        outerSize = 120.dp,
        logoSize = 83.5.dp,
        cornerRadius = 33.dp,
        borderWidth = 0.38.dp,
        logoShadowColorDark = Color(0x4D000000),
        logoShadowColorLight = Color(0x66000000),
        shadowColorDark = Color.Black.copy(alpha = 0.4f),
        shadowColorLight = Color(0x401B1D22),
        shadowRadius = 32.dp,
    )

    data object Large : ElementLogoAtomSize(
        outerSize = 158.dp,
        logoSize = 110.dp,
        cornerRadius = 44.dp,
        borderWidth = 0.5.dp,
        logoShadowColorDark = Color(0x4D000000),
        logoShadowColorLight = Color(0x66000000),
        shadowColorDark = Color.Black,
        shadowColorLight = Color(0x801B1D22),
        shadowRadius = 60.dp,
    )
}

@Composable
@PreviewsDayNight
internal fun ElementLogoAtomMediumPreview() = ElementPreview {
    ContentToPreview(ElementLogoAtomSize.Medium)
}

@Composable
@PreviewsDayNight
internal fun ElementLogoAtomLargePreview() = ElementPreview {
    ContentToPreview(ElementLogoAtomSize.Large)
}

@Composable
@PreviewsDayNight
internal fun ElementLogoAtomMediumNoBlurShadowPreview() = ElementPreview {
    ContentToPreview(ElementLogoAtomSize.Medium, useBlurredShadow = false)
}

@Composable
@PreviewsDayNight
internal fun ElementLogoAtomLargeNoBlurShadowPreview() = ElementPreview {
    ContentToPreview(ElementLogoAtomSize.Large, useBlurredShadow = false)
}

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(elementLogoAtomSize: ElementLogoAtomSize, useBlurredShadow: Boolean = true) {
    Box(
        Modifier
            .size(elementLogoAtomSize.outerSize + elementLogoAtomSize.shadowRadius * 2)
            .background(ElementTheme.colors.bgSubtlePrimary),
        contentAlignment = Alignment.Center
    ) {
        ElementLogoAtom(elementLogoAtomSize, useBlurredShadow = useBlurredShadow)
    }
}
