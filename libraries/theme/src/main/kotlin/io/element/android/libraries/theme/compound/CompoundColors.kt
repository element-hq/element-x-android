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

package io.element.android.libraries.theme.compound

import io.element.android.libraries.theme.compound.generated.internal.DarkDesignTokens
import io.element.android.libraries.theme.compound.generated.internal.LightDesignTokens
import io.element.android.libraries.theme.compound.generated.SemanticColors

internal val compoundColorsLight = SemanticColors(
    textPrimary = LightDesignTokens.colorGray1400,
    textSecondary = LightDesignTokens.colorGray900,
    textPlaceholder = LightDesignTokens.colorGray800,
    textDisabled = LightDesignTokens.colorGray800,
    textActionPrimary = LightDesignTokens.colorGray1400,
    textActionAccent = LightDesignTokens.colorGreen900,
    textLinkExternal = LightDesignTokens.colorBlue900,
    textCriticalPrimary = LightDesignTokens.colorRed900,
    textSuccessPrimary = LightDesignTokens.colorGreen900,
    textInfoPrimary = LightDesignTokens.colorBlue900,
    textOnSolidPrimary = LightDesignTokens.colorThemeBg,
    bgSubtlePrimary = LightDesignTokens.colorGray400,
    bgSubtleSecondary = LightDesignTokens.colorBgSubtleSecondaryLevel0,
    bgCanvasDefault = LightDesignTokens.colorBgCanvasDefaultLevel1,
    bgCanvasDisabled = LightDesignTokens.colorGray200,
    bgActionPrimaryRest = LightDesignTokens.colorGray1400,
    bgActionPrimaryHovered = LightDesignTokens.colorGray1200,
    bgActionPrimaryPressed = LightDesignTokens.colorGray1100,
    bgActionPrimaryDisabled = LightDesignTokens.colorGray700,
    bgActionSecondaryRest = LightDesignTokens.colorThemeBg,
    bgActionSecondaryHovered = LightDesignTokens.colorAlphaGray200,
    bgActionSecondaryPressed = LightDesignTokens.colorAlphaGray300,
    bgCriticalPrimary = LightDesignTokens.colorRed900,
    bgCriticalHovered = LightDesignTokens.colorRed1000,
    bgCriticalSubtle = LightDesignTokens.colorRed200,
    bgCriticalSubtleHovered = LightDesignTokens.colorRed300,
    bgSuccessSubtle = LightDesignTokens.colorGreen200,
    bgInfoSubtle = LightDesignTokens.colorBlue200,
    borderDisabled = LightDesignTokens.colorGray500,
    borderFocused = LightDesignTokens.colorBlue900,
    borderInteractivePrimary = LightDesignTokens.colorGray800,
    borderInteractiveSecondary = LightDesignTokens.colorGray600,
    borderInteractiveHovered = LightDesignTokens.colorGray1100,
    borderCriticalPrimary = LightDesignTokens.colorRed900,
    borderCriticalHovered = LightDesignTokens.colorRed1000,
    borderCriticalSubtle = LightDesignTokens.colorRed500,
    borderSuccessSubtle = LightDesignTokens.colorGreen500,
    borderInfoSubtle = LightDesignTokens.colorBlue500,
    iconPrimary = LightDesignTokens.colorGray1400,
    iconSecondary = LightDesignTokens.colorGray900,
    iconTertiary = LightDesignTokens.colorGray800,
    iconQuaternary = LightDesignTokens.colorGray700,
    iconDisabled = LightDesignTokens.colorGray700,
    iconPrimaryAlpha = LightDesignTokens.colorAlphaGray1400,
    iconSecondaryAlpha = LightDesignTokens.colorAlphaGray900,
    iconTertiaryAlpha = LightDesignTokens.colorAlphaGray800,
    iconQuaternaryAlpha = LightDesignTokens.colorAlphaGray700,
    iconAccentTertiary = LightDesignTokens.colorGreen800,
    iconCriticalPrimary = LightDesignTokens.colorRed900,
    iconSuccessPrimary = LightDesignTokens.colorGreen900,
    iconInfoPrimary = LightDesignTokens.colorBlue900,
    iconOnSolidPrimary = LightDesignTokens.colorThemeBg,
    isLight = true,
)

internal val compoundColorsDark = SemanticColors(
    textPrimary = DarkDesignTokens.colorGray1400,
    textSecondary = DarkDesignTokens.colorGray900,
    textPlaceholder = DarkDesignTokens.colorGray800,
    textDisabled = DarkDesignTokens.colorGray800,
    textActionPrimary = DarkDesignTokens.colorGray1400,
    textActionAccent = DarkDesignTokens.colorGreen900,
    textLinkExternal = DarkDesignTokens.colorBlue900,
    textCriticalPrimary = DarkDesignTokens.colorRed900,
    textSuccessPrimary = DarkDesignTokens.colorGreen900,
    textInfoPrimary = DarkDesignTokens.colorBlue900,
    textOnSolidPrimary = DarkDesignTokens.colorThemeBg,
    bgSubtlePrimary = DarkDesignTokens.colorGray400,
    // The value DarkDesignTokens.colorBgSubtleSecondaryLevel0 is defined to colorThemeBg, this is not correct, so override the value here until this is fixed,
    bgSubtleSecondary =  DarkDesignTokens.colorGray300, // DarkDesignTokens.colorBgSubtleSecondaryLevel0
    bgCanvasDefault = DarkDesignTokens.colorBgCanvasDefaultLevel1,
    bgCanvasDisabled = DarkDesignTokens.colorGray200,
    bgActionPrimaryRest = DarkDesignTokens.colorGray1400,
    bgActionPrimaryHovered = DarkDesignTokens.colorGray1200,
    bgActionPrimaryPressed = DarkDesignTokens.colorGray1100,
    bgActionPrimaryDisabled = DarkDesignTokens.colorGray700,
    bgActionSecondaryRest = DarkDesignTokens.colorThemeBg,
    bgActionSecondaryHovered = DarkDesignTokens.colorAlphaGray200,
    bgActionSecondaryPressed = DarkDesignTokens.colorAlphaGray300,
    bgCriticalPrimary = DarkDesignTokens.colorRed900,
    bgCriticalHovered = DarkDesignTokens.colorRed1000,
    bgCriticalSubtle = DarkDesignTokens.colorRed200,
    bgCriticalSubtleHovered = DarkDesignTokens.colorRed300,
    bgSuccessSubtle = DarkDesignTokens.colorGreen200,
    bgInfoSubtle = DarkDesignTokens.colorBlue200,
    borderDisabled = DarkDesignTokens.colorGray500,
    borderFocused = DarkDesignTokens.colorBlue900,
    borderInteractivePrimary = DarkDesignTokens.colorGray800,
    borderInteractiveSecondary = DarkDesignTokens.colorGray600,
    borderInteractiveHovered = DarkDesignTokens.colorGray1100,
    borderCriticalPrimary = DarkDesignTokens.colorRed900,
    borderCriticalHovered = DarkDesignTokens.colorRed1000,
    borderCriticalSubtle = DarkDesignTokens.colorRed500,
    borderSuccessSubtle = DarkDesignTokens.colorGreen500,
    borderInfoSubtle = DarkDesignTokens.colorBlue500,
    iconPrimary = DarkDesignTokens.colorGray1400,
    iconSecondary = DarkDesignTokens.colorGray900,
    iconTertiary = DarkDesignTokens.colorGray800,
    iconQuaternary = DarkDesignTokens.colorGray700,
    iconDisabled = DarkDesignTokens.colorGray700,
    iconPrimaryAlpha = DarkDesignTokens.colorAlphaGray1400,
    iconSecondaryAlpha = DarkDesignTokens.colorAlphaGray900,
    iconTertiaryAlpha = DarkDesignTokens.colorAlphaGray800,
    iconQuaternaryAlpha = DarkDesignTokens.colorAlphaGray700,
    iconAccentTertiary = DarkDesignTokens.colorGreen800,
    iconCriticalPrimary = DarkDesignTokens.colorRed900,
    iconSuccessPrimary = DarkDesignTokens.colorGreen900,
    iconInfoPrimary = DarkDesignTokens.colorBlue900,
    iconOnSolidPrimary = DarkDesignTokens.colorThemeBg,
    isLight = false,
)
