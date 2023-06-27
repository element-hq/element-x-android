/*
 * Copyright 2023 New Vector Ltd
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

package io.element.android.libraries.theme.compound.generated

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color



// Do not edit directly
// Generated on Tue, 27 Jun 2023 05:38:44 GMT





/**
  * This class holds all the semantic tokens of the Compound theme.
  */
@Stable
class SemanticColors(
    bgActionPrimaryDisabled: Color,
    bgActionPrimaryHovered: Color,
    bgActionPrimaryPressed: Color,
    bgActionPrimaryRest: Color,
    bgActionSecondaryHovered: Color,
    bgActionSecondaryPressed: Color,
    bgActionSecondaryRest: Color,
    bgCanvasDefault: Color,
    bgCanvasDisabled: Color,
    bgCriticalHovered: Color,
    bgCriticalPrimary: Color,
    bgCriticalSubtle: Color,
    bgCriticalSubtleHovered: Color,
    bgInfoSubtle: Color,
    bgSubtlePrimary: Color,
    bgSubtleSecondary: Color,
    bgSuccessSubtle: Color,
    borderCriticalHovered: Color,
    borderCriticalPrimary: Color,
    borderCriticalSubtle: Color,
    borderDisabled: Color,
    borderFocused: Color,
    borderInfoSubtle: Color,
    borderInteractiveHovered: Color,
    borderInteractivePrimary: Color,
    borderInteractiveSecondary: Color,
    borderSuccessSubtle: Color,
    iconAccentTertiary: Color,
    iconCriticalPrimary: Color,
    iconDisabled: Color,
    iconInfoPrimary: Color,
    iconOnSolidPrimary: Color,
    iconPrimary: Color,
    iconPrimaryAlpha: Color,
    iconQuaternary: Color,
    iconQuaternaryAlpha: Color,
    iconSecondary: Color,
    iconSecondaryAlpha: Color,
    iconSuccessPrimary: Color,
    iconTertiary: Color,
    iconTertiaryAlpha: Color,
    textActionAccent: Color,
    textActionPrimary: Color,
    textCriticalPrimary: Color,
    textDisabled: Color,
    textInfoPrimary: Color,
    textLinkExternal: Color,
    textOnSolidPrimary: Color,
    textPlaceholder: Color,
    textPrimary: Color,
    textSecondary: Color,
    textSuccessPrimary: Color,
    isLight: Boolean,
) {
    var isLight by mutableStateOf(isLight)
        private set
    /** Background colour for primary actions. State: Disabled. */
    var bgActionPrimaryDisabled by mutableStateOf(bgActionPrimaryDisabled)
        private set
    /** Background colour for primary actions. State: Hover. */
    var bgActionPrimaryHovered by mutableStateOf(bgActionPrimaryHovered)
        private set
    /** Background colour for primary actions. State: Pressed. */
    var bgActionPrimaryPressed by mutableStateOf(bgActionPrimaryPressed)
        private set
    /** Background colour for primary actions. State: Rest. */
    var bgActionPrimaryRest by mutableStateOf(bgActionPrimaryRest)
        private set
    /** Background colour for secondary actions. State: Hover. */
    var bgActionSecondaryHovered by mutableStateOf(bgActionSecondaryHovered)
        private set
    /** Background colour for secondary actions. State: Pressed. */
    var bgActionSecondaryPressed by mutableStateOf(bgActionSecondaryPressed)
        private set
    /** Background colour for secondary actions. State: Rest. */
    var bgActionSecondaryRest by mutableStateOf(bgActionSecondaryRest)
        private set
    /** Default global background for the user interface.
Elevation: Default (Level 0) */
    var bgCanvasDefault by mutableStateOf(bgCanvasDefault)
        private set
    /** Default background for disabled elements. There's no minimum contrast requirement. */
    var bgCanvasDisabled by mutableStateOf(bgCanvasDisabled)
        private set
    /** High-contrast background color for critical state. State: Hover. */
    var bgCriticalHovered by mutableStateOf(bgCriticalHovered)
        private set
    /** High-contrast background color for critical state. State: Rest. */
    var bgCriticalPrimary by mutableStateOf(bgCriticalPrimary)
        private set
    /** Default subtle critical surfaces. State: Rest. */
    var bgCriticalSubtle by mutableStateOf(bgCriticalSubtle)
        private set
    /** Default subtle critical surfaces. State: Hover. */
    var bgCriticalSubtleHovered by mutableStateOf(bgCriticalSubtleHovered)
        private set
    /** Subtle background colour for informational elements. State: Rest. */
    var bgInfoSubtle by mutableStateOf(bgInfoSubtle)
        private set
    /** Medium contrast surfaces.
Elevation: Default (Level 2). */
    var bgSubtlePrimary by mutableStateOf(bgSubtlePrimary)
        private set
    /** Low contrast surfaces.
Elevation: Default (Level 1). */
    var bgSubtleSecondary by mutableStateOf(bgSubtleSecondary)
        private set
    /** Subtle background colour for success state elements. State: Rest. */
    var bgSuccessSubtle by mutableStateOf(bgSuccessSubtle)
        private set
    /** High-contrast border for critical state. State: Hover. */
    var borderCriticalHovered by mutableStateOf(borderCriticalHovered)
        private set
    /** High-contrast border for critical state. State: Rest. */
    var borderCriticalPrimary by mutableStateOf(borderCriticalPrimary)
        private set
    /** Subtle border colour for critical state elements. */
    var borderCriticalSubtle by mutableStateOf(borderCriticalSubtle)
        private set
    /** Used for borders of disabled elements. There's no minimum contrast requirement. */
    var borderDisabled by mutableStateOf(borderDisabled)
        private set
    /** Used for the focus state outline. */
    var borderFocused by mutableStateOf(borderFocused)
        private set
    /** Subtle border colour for informational elements. */
    var borderInfoSubtle by mutableStateOf(borderInfoSubtle)
        private set
    /** Default contrast for accessible interactive element borders. State: Hover. */
    var borderInteractiveHovered by mutableStateOf(borderInteractiveHovered)
        private set
    /** Default contrast for accessible interactive element borders. State: Rest. */
    var borderInteractivePrimary by mutableStateOf(borderInteractivePrimary)
        private set
    /** ⚠️ Lowest contrast for non-accessible interactive element borders, <3:1. Only use for non-essential borders. Do not rely exclusively on them. State: Rest. */
    var borderInteractiveSecondary by mutableStateOf(borderInteractiveSecondary)
        private set
    /** Subtle border colour for success state elements. */
    var borderSuccessSubtle by mutableStateOf(borderSuccessSubtle)
        private set
    /** Lowest contrast accessible accent icons. */
    var iconAccentTertiary by mutableStateOf(iconAccentTertiary)
        private set
    /** High-contrast icon for critical state. State: Rest. */
    var iconCriticalPrimary by mutableStateOf(iconCriticalPrimary)
        private set
    /** Use for icons in disabled elements. There's no minimum contrast requirement. */
    var iconDisabled by mutableStateOf(iconDisabled)
        private set
    /** High-contrast icon for informational elements. */
    var iconInfoPrimary by mutableStateOf(iconInfoPrimary)
        private set
    /** Highest contrast icon color on top of high-contrast solid backgrounds like primary, accent, or destructive actions. */
    var iconOnSolidPrimary by mutableStateOf(iconOnSolidPrimary)
        private set
    /** Highest contrast icons. */
    var iconPrimary by mutableStateOf(iconPrimary)
        private set
    /** Translucent version of primary icon. Refer to it for intended use. */
    var iconPrimaryAlpha by mutableStateOf(iconPrimaryAlpha)
        private set
    /** ⚠️ Lowest contrast non-accessible icons, <3:1. Only use for non-essential icons. Do not rely exclusively on them. */
    var iconQuaternary by mutableStateOf(iconQuaternary)
        private set
    /** Translucent version of quaternary icon. Refer to it for intended use. */
    var iconQuaternaryAlpha by mutableStateOf(iconQuaternaryAlpha)
        private set
    /** Lower contrast icons. */
    var iconSecondary by mutableStateOf(iconSecondary)
        private set
    /** Translucent version of secondary icon. Refer to it for intended use. */
    var iconSecondaryAlpha by mutableStateOf(iconSecondaryAlpha)
        private set
    /** High-contrast icon for success state elements. */
    var iconSuccessPrimary by mutableStateOf(iconSuccessPrimary)
        private set
    /** Lowest contrast accessible icons. */
    var iconTertiary by mutableStateOf(iconTertiary)
        private set
    /** Translucent version of tertiary icon. Refer to it for intended use. */
    var iconTertiaryAlpha by mutableStateOf(iconTertiaryAlpha)
        private set
    /** Accent text colour for plain actions. */
    var textActionAccent by mutableStateOf(textActionAccent)
        private set
    /** Default text colour for plain actions. */
    var textActionPrimary by mutableStateOf(textActionPrimary)
        private set
    /** Text colour for destructive plain actions. */
    var textCriticalPrimary by mutableStateOf(textCriticalPrimary)
        private set
    /** Use for regular text in disabled elements. There's no minimum contrast requirement. */
    var textDisabled by mutableStateOf(textDisabled)
        private set
    /** Accent text colour for informational elements. */
    var textInfoPrimary by mutableStateOf(textInfoPrimary)
        private set
    /** Text colour for external links. */
    var textLinkExternal by mutableStateOf(textLinkExternal)
        private set
    /** For use as text color on top of high-contrast solid backgrounds like primary, accent, or destructive actions. */
    var textOnSolidPrimary by mutableStateOf(textOnSolidPrimary)
        private set
    /** Use for placeholder text. Placeholder text should be non-essential. Do not rely exclusively on it. */
    var textPlaceholder by mutableStateOf(textPlaceholder)
        private set
    /** Highest contrast text. */
    var textPrimary by mutableStateOf(textPrimary)
        private set
    /** Lowest contrast text. */
    var textSecondary by mutableStateOf(textSecondary)
        private set
    /** Accent text colour for success state elements. */
    var textSuccessPrimary by mutableStateOf(textSuccessPrimary)
        private set

    fun copy(
        bgActionPrimaryDisabled: Color = this.bgActionPrimaryDisabled,
        bgActionPrimaryHovered: Color = this.bgActionPrimaryHovered,
        bgActionPrimaryPressed: Color = this.bgActionPrimaryPressed,
        bgActionPrimaryRest: Color = this.bgActionPrimaryRest,
        bgActionSecondaryHovered: Color = this.bgActionSecondaryHovered,
        bgActionSecondaryPressed: Color = this.bgActionSecondaryPressed,
        bgActionSecondaryRest: Color = this.bgActionSecondaryRest,
        bgCanvasDefault: Color = this.bgCanvasDefault,
        bgCanvasDisabled: Color = this.bgCanvasDisabled,
        bgCriticalHovered: Color = this.bgCriticalHovered,
        bgCriticalPrimary: Color = this.bgCriticalPrimary,
        bgCriticalSubtle: Color = this.bgCriticalSubtle,
        bgCriticalSubtleHovered: Color = this.bgCriticalSubtleHovered,
        bgInfoSubtle: Color = this.bgInfoSubtle,
        bgSubtlePrimary: Color = this.bgSubtlePrimary,
        bgSubtleSecondary: Color = this.bgSubtleSecondary,
        bgSuccessSubtle: Color = this.bgSuccessSubtle,
        borderCriticalHovered: Color = this.borderCriticalHovered,
        borderCriticalPrimary: Color = this.borderCriticalPrimary,
        borderCriticalSubtle: Color = this.borderCriticalSubtle,
        borderDisabled: Color = this.borderDisabled,
        borderFocused: Color = this.borderFocused,
        borderInfoSubtle: Color = this.borderInfoSubtle,
        borderInteractiveHovered: Color = this.borderInteractiveHovered,
        borderInteractivePrimary: Color = this.borderInteractivePrimary,
        borderInteractiveSecondary: Color = this.borderInteractiveSecondary,
        borderSuccessSubtle: Color = this.borderSuccessSubtle,
        iconAccentTertiary: Color = this.iconAccentTertiary,
        iconCriticalPrimary: Color = this.iconCriticalPrimary,
        iconDisabled: Color = this.iconDisabled,
        iconInfoPrimary: Color = this.iconInfoPrimary,
        iconOnSolidPrimary: Color = this.iconOnSolidPrimary,
        iconPrimary: Color = this.iconPrimary,
        iconPrimaryAlpha: Color = this.iconPrimaryAlpha,
        iconQuaternary: Color = this.iconQuaternary,
        iconQuaternaryAlpha: Color = this.iconQuaternaryAlpha,
        iconSecondary: Color = this.iconSecondary,
        iconSecondaryAlpha: Color = this.iconSecondaryAlpha,
        iconSuccessPrimary: Color = this.iconSuccessPrimary,
        iconTertiary: Color = this.iconTertiary,
        iconTertiaryAlpha: Color = this.iconTertiaryAlpha,
        textActionAccent: Color = this.textActionAccent,
        textActionPrimary: Color = this.textActionPrimary,
        textCriticalPrimary: Color = this.textCriticalPrimary,
        textDisabled: Color = this.textDisabled,
        textInfoPrimary: Color = this.textInfoPrimary,
        textLinkExternal: Color = this.textLinkExternal,
        textOnSolidPrimary: Color = this.textOnSolidPrimary,
        textPlaceholder: Color = this.textPlaceholder,
        textPrimary: Color = this.textPrimary,
        textSecondary: Color = this.textSecondary,
        textSuccessPrimary: Color = this.textSuccessPrimary,
        isLight: Boolean = this.isLight,
    ) = SemanticColors(
        bgActionPrimaryDisabled = bgActionPrimaryDisabled,
        bgActionPrimaryHovered = bgActionPrimaryHovered,
        bgActionPrimaryPressed = bgActionPrimaryPressed,
        bgActionPrimaryRest = bgActionPrimaryRest,
        bgActionSecondaryHovered = bgActionSecondaryHovered,
        bgActionSecondaryPressed = bgActionSecondaryPressed,
        bgActionSecondaryRest = bgActionSecondaryRest,
        bgCanvasDefault = bgCanvasDefault,
        bgCanvasDisabled = bgCanvasDisabled,
        bgCriticalHovered = bgCriticalHovered,
        bgCriticalPrimary = bgCriticalPrimary,
        bgCriticalSubtle = bgCriticalSubtle,
        bgCriticalSubtleHovered = bgCriticalSubtleHovered,
        bgInfoSubtle = bgInfoSubtle,
        bgSubtlePrimary = bgSubtlePrimary,
        bgSubtleSecondary = bgSubtleSecondary,
        bgSuccessSubtle = bgSuccessSubtle,
        borderCriticalHovered = borderCriticalHovered,
        borderCriticalPrimary = borderCriticalPrimary,
        borderCriticalSubtle = borderCriticalSubtle,
        borderDisabled = borderDisabled,
        borderFocused = borderFocused,
        borderInfoSubtle = borderInfoSubtle,
        borderInteractiveHovered = borderInteractiveHovered,
        borderInteractivePrimary = borderInteractivePrimary,
        borderInteractiveSecondary = borderInteractiveSecondary,
        borderSuccessSubtle = borderSuccessSubtle,
        iconAccentTertiary = iconAccentTertiary,
        iconCriticalPrimary = iconCriticalPrimary,
        iconDisabled = iconDisabled,
        iconInfoPrimary = iconInfoPrimary,
        iconOnSolidPrimary = iconOnSolidPrimary,
        iconPrimary = iconPrimary,
        iconPrimaryAlpha = iconPrimaryAlpha,
        iconQuaternary = iconQuaternary,
        iconQuaternaryAlpha = iconQuaternaryAlpha,
        iconSecondary = iconSecondary,
        iconSecondaryAlpha = iconSecondaryAlpha,
        iconSuccessPrimary = iconSuccessPrimary,
        iconTertiary = iconTertiary,
        iconTertiaryAlpha = iconTertiaryAlpha,
        textActionAccent = textActionAccent,
        textActionPrimary = textActionPrimary,
        textCriticalPrimary = textCriticalPrimary,
        textDisabled = textDisabled,
        textInfoPrimary = textInfoPrimary,
        textLinkExternal = textLinkExternal,
        textOnSolidPrimary = textOnSolidPrimary,
        textPlaceholder = textPlaceholder,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textSuccessPrimary = textSuccessPrimary,
        isLight = isLight,
    )

    fun updateColorsFrom(other: SemanticColors) {
        bgActionPrimaryDisabled = other.bgActionPrimaryDisabled
        bgActionPrimaryHovered = other.bgActionPrimaryHovered
        bgActionPrimaryPressed = other.bgActionPrimaryPressed
        bgActionPrimaryRest = other.bgActionPrimaryRest
        bgActionSecondaryHovered = other.bgActionSecondaryHovered
        bgActionSecondaryPressed = other.bgActionSecondaryPressed
        bgActionSecondaryRest = other.bgActionSecondaryRest
        bgCanvasDefault = other.bgCanvasDefault
        bgCanvasDisabled = other.bgCanvasDisabled
        bgCriticalHovered = other.bgCriticalHovered
        bgCriticalPrimary = other.bgCriticalPrimary
        bgCriticalSubtle = other.bgCriticalSubtle
        bgCriticalSubtleHovered = other.bgCriticalSubtleHovered
        bgInfoSubtle = other.bgInfoSubtle
        bgSubtlePrimary = other.bgSubtlePrimary
        bgSubtleSecondary = other.bgSubtleSecondary
        bgSuccessSubtle = other.bgSuccessSubtle
        borderCriticalHovered = other.borderCriticalHovered
        borderCriticalPrimary = other.borderCriticalPrimary
        borderCriticalSubtle = other.borderCriticalSubtle
        borderDisabled = other.borderDisabled
        borderFocused = other.borderFocused
        borderInfoSubtle = other.borderInfoSubtle
        borderInteractiveHovered = other.borderInteractiveHovered
        borderInteractivePrimary = other.borderInteractivePrimary
        borderInteractiveSecondary = other.borderInteractiveSecondary
        borderSuccessSubtle = other.borderSuccessSubtle
        iconAccentTertiary = other.iconAccentTertiary
        iconCriticalPrimary = other.iconCriticalPrimary
        iconDisabled = other.iconDisabled
        iconInfoPrimary = other.iconInfoPrimary
        iconOnSolidPrimary = other.iconOnSolidPrimary
        iconPrimary = other.iconPrimary
        iconPrimaryAlpha = other.iconPrimaryAlpha
        iconQuaternary = other.iconQuaternary
        iconQuaternaryAlpha = other.iconQuaternaryAlpha
        iconSecondary = other.iconSecondary
        iconSecondaryAlpha = other.iconSecondaryAlpha
        iconSuccessPrimary = other.iconSuccessPrimary
        iconTertiary = other.iconTertiary
        iconTertiaryAlpha = other.iconTertiaryAlpha
        textActionAccent = other.textActionAccent
        textActionPrimary = other.textActionPrimary
        textCriticalPrimary = other.textCriticalPrimary
        textDisabled = other.textDisabled
        textInfoPrimary = other.textInfoPrimary
        textLinkExternal = other.textLinkExternal
        textOnSolidPrimary = other.textOnSolidPrimary
        textPlaceholder = other.textPlaceholder
        textPrimary = other.textPrimary
        textSecondary = other.textSecondary
        textSuccessPrimary = other.textSuccessPrimary
        isLight = other.isLight
    }
}
