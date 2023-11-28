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

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.modifiers.blurCompat
import io.element.android.libraries.designsystem.modifiers.blurredShapeShadow
import io.element.android.libraries.designsystem.modifiers.canUseBlurMaskFilter
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview

@Composable
fun ElementLogoAtom(
    size: ElementLogoAtomSize,
    modifier: Modifier = Modifier,
    useBlurredShadow: Boolean = canUseBlurMaskFilter(),
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    val blur = if (darkTheme) 160.dp else 24.dp
    //box-shadow: 0px 6.075949668884277px 24.30379867553711px 0px #1B1D2280;
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
                    elevation = 25.dp,
                    clip = false,
                    shape = CircleShape,
                    ambientColor = logoShadowColor,
                )
                .shadow(
                    elevation = 25.dp,
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
