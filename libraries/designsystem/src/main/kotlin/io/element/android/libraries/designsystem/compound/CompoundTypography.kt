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

package io.element.android.libraries.designsystem.compound

import androidx.compose.material3.Typography

val compoundTypography: Typography = Typography(
    // displayLarge = , 57px (Material) size. We have no equivalent
    // displayMedium = , 45px (Material) size. We have no equivalent
    // displaySmall = , 36px (Material) size. We have no equivalent
    headlineLarge = LightDesignTokens.fontHeadingXlBold, // 32px (Material) vs 34px, it's the closest one
    headlineMedium = LightDesignTokens.fontHeadingLgBold, // both are 28px
    // headlineSmall = , 24px (Material) size. We have no equivalent
    titleLarge = LightDesignTokens.fontHeadingSmMedium, // 22px (Material) vs 20px, it's the closest one
    titleMedium = LightDesignTokens.fontBodyLgMedium, // 16px both
    titleSmall = LightDesignTokens.fontBodyMdMedium, // 14px both
    bodyLarge = LightDesignTokens.fontBodyLgRegular, // 16px both
    bodyMedium = LightDesignTokens.fontBodyMdRegular, // 14px both
    bodySmall = LightDesignTokens.fontBodySmRegular, // 12px both,
    labelLarge = LightDesignTokens.fontBodyMdMedium, // 14 px both
    labelMedium = LightDesignTokens.fontBodySmMedium, // 12 px both
    labelSmall = LightDesignTokens.fontBodyXsMedium, // 11 px both
)
