/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.compoundColorsHcDark
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

@Preview(heightDp = 2000)
@Composable
internal fun CompoundSemanticColorsLight() = ElementTheme {
    Surface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Compound Semantic Colors - Light")
            ColorListPreview(
                backgroundColor = Color.White,
                foregroundColor = Color.Black,
                colors = getSemanticColors(),
                numColumns = 2,
            )
        }
    }
}

@Preview(heightDp = 2000)
@Composable
internal fun CompoundSemanticColorsLightHc() = ElementTheme(
    compoundDark = compoundColorsHcDark,
) {
    Surface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Compound Semantic Colors - Light HC")
            ColorListPreview(
                backgroundColor = Color.White,
                foregroundColor = Color.Black,
                colors = getSemanticColors(),
                numColumns = 2,
            )
        }
    }
}

@Preview(heightDp = 2000)
@Composable
internal fun CompoundSemanticColorsDark() = ElementTheme(darkTheme = true) {
    Surface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Compound Semantic Colors - Dark")
            ColorListPreview(
                backgroundColor = Color.White,
                foregroundColor = Color.Black,
                colors = getSemanticColors(),
                numColumns = 2,
            )
        }
    }
}

@Preview(heightDp = 2000)
@Composable
internal fun CompoundSemanticColorsDarkHc() = ElementTheme(
    darkTheme = true,
    compoundDark = compoundColorsHcDark,
) {
    Surface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Compound Semantic Colors - Dark HC")
            ColorListPreview(
                backgroundColor = Color.White,
                foregroundColor = Color.Black,
                colors = getSemanticColors(),
                numColumns = 2,
            )
        }
    }
}

@Composable
private fun getSemanticColors(): ImmutableMap<String, Color> {
    return with(ElementTheme.colors) {
        persistentMapOf(
            "bgAccentHovered" to bgAccentHovered,
            "bgAccentPressed" to bgAccentPressed,
            "bgAccentRest" to bgAccentRest,
            "bgAccentSelected" to bgAccentSelected,
            "bgActionPrimaryDisabled" to bgActionPrimaryDisabled,
            "bgActionPrimaryHovered" to bgActionPrimaryHovered,
            "bgActionPrimaryPressed" to bgActionPrimaryPressed,
            "bgActionPrimaryRest" to bgActionPrimaryRest,
            "bgActionSecondaryHovered" to bgActionSecondaryHovered,
            "bgActionSecondaryPressed" to bgActionSecondaryPressed,
            "bgActionSecondaryRest" to bgActionSecondaryRest,
            "bgBadgeAccent" to bgBadgeAccent,
            "bgBadgeDefault" to bgBadgeDefault,
            "bgBadgeInfo" to bgBadgeInfo,
            "bgCanvasDefault" to bgCanvasDefault,
            "bgCanvasDefaultLevel1" to bgCanvasDefaultLevel1,
            "bgCanvasDisabled" to bgCanvasDisabled,
            "bgCriticalHovered" to bgCriticalHovered,
            "bgCriticalPrimary" to bgCriticalPrimary,
            "bgCriticalSubtle" to bgCriticalSubtle,
            "bgCriticalSubtleHovered" to bgCriticalSubtleHovered,
            "bgDecorative1" to bgDecorative1,
            "bgDecorative2" to bgDecorative2,
            "bgDecorative3" to bgDecorative3,
            "bgDecorative4" to bgDecorative4,
            "bgDecorative5" to bgDecorative5,
            "bgDecorative6" to bgDecorative6,
            "bgInfoSubtle" to bgInfoSubtle,
            "bgSubtlePrimary" to bgSubtlePrimary,
            "bgSubtleSecondary" to bgSubtleSecondary,
            "bgSubtleSecondaryLevel0" to bgSubtleSecondaryLevel0,
            "bgSuccessSubtle" to bgSuccessSubtle,
            "borderAccentSubtle" to borderAccentSubtle,
            "borderCriticalHovered" to borderCriticalHovered,
            "borderCriticalPrimary" to borderCriticalPrimary,
            "borderCriticalSubtle" to borderCriticalSubtle,
            "borderDisabled" to borderDisabled,
            "borderFocused" to borderFocused,
            "borderInfoSubtle" to borderInfoSubtle,
            "borderInteractiveHovered" to borderInteractiveHovered,
            "borderInteractivePrimary" to borderInteractivePrimary,
            "borderInteractiveSecondary" to borderInteractiveSecondary,
            "borderSuccessSubtle" to borderSuccessSubtle,
            "gradientActionStop1" to gradientActionStop1,
            "gradientActionStop2" to gradientActionStop2,
            "gradientActionStop3" to gradientActionStop3,
            "gradientActionStop4" to gradientActionStop4,
            "gradientInfoStop1" to gradientInfoStop1,
            "gradientInfoStop2" to gradientInfoStop2,
            "gradientInfoStop3" to gradientInfoStop3,
            "gradientInfoStop4" to gradientInfoStop4,
            "gradientInfoStop5" to gradientInfoStop5,
            "gradientInfoStop6" to gradientInfoStop6,
            "gradientSubtleStop1" to gradientSubtleStop1,
            "gradientSubtleStop2" to gradientSubtleStop2,
            "gradientSubtleStop3" to gradientSubtleStop3,
            "gradientSubtleStop4" to gradientSubtleStop4,
            "gradientSubtleStop5" to gradientSubtleStop5,
            "gradientSubtleStop6" to gradientSubtleStop6,
            "iconAccentPrimary" to iconAccentPrimary,
            "iconAccentTertiary" to iconAccentTertiary,
            "iconCriticalPrimary" to iconCriticalPrimary,
            "iconDisabled" to iconDisabled,
            "iconInfoPrimary" to iconInfoPrimary,
            "iconOnSolidPrimary" to iconOnSolidPrimary,
            "iconPrimary" to iconPrimary,
            "iconPrimaryAlpha" to iconPrimaryAlpha,
            "iconQuaternary" to iconQuaternary,
            "iconQuaternaryAlpha" to iconQuaternaryAlpha,
            "iconSecondary" to iconSecondary,
            "iconSecondaryAlpha" to iconSecondaryAlpha,
            "iconSuccessPrimary" to iconSuccessPrimary,
            "iconTertiary" to iconTertiary,
            "iconTertiaryAlpha" to iconTertiaryAlpha,
            "textActionAccent" to textActionAccent,
            "textActionPrimary" to textActionPrimary,
            "textBadgeAccent" to textBadgeAccent,
            "textBadgeInfo" to textBadgeInfo,
            "textCriticalPrimary" to textCriticalPrimary,
            "textDecorative1" to textDecorative1,
            "textDecorative2" to textDecorative2,
            "textDecorative3" to textDecorative3,
            "textDecorative4" to textDecorative4,
            "textDecorative5" to textDecorative5,
            "textDecorative6" to textDecorative6,
            "textDisabled" to textDisabled,
            "textInfoPrimary" to textInfoPrimary,
            "textLinkExternal" to textLinkExternal,
            "textOnSolidPrimary" to textOnSolidPrimary,
            "textPrimary" to textPrimary,
            "textSecondary" to textSecondary,
            "textSuccessPrimary" to textSuccessPrimary,
            "isLight" to if (isLight) Color.White else Color.Black,
        )
    }
}
