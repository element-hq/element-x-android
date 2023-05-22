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

package io.element.android.libraries.designsystem.theme.compound

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

object CompoundTypography {
    val fontLetterSpacingHeadingXl = 0.em
    val fontLetterSpacingHeadingLg = 0.em
    val fontLetterSpacingHeadingMd = 0.em
    val fontLetterSpacingHeadingSm = 0.em
    val fontLetterSpacingBodyLg = 0.015629999999999998.em
    val fontLetterSpacingBodyMd = 0.01786.em
    val fontLetterSpacingBodySm = 0.03333.em
    val fontLetterSpacingBodyXs = 0.04545.em
    val fontSizeHeadingXl = 34.sp
    val fontSizeHeadingLg = 28.sp
    val fontSizeHeadingMd = 22.sp
    val fontSizeHeadingSm = 20.sp
    val fontSizeBodyLg = 16.sp
    val fontSizeBodyMd = 14.sp
    val fontSizeBodySm = 12.sp
    val fontSizeBodyXs = 11.sp
    val fontLineHeightHeadingXlRegular = 41.sp
    val fontLineHeightHeadingLgRegular = 34.sp
    val fontLineHeightHeadingMdRegular = 27.sp
    val fontLineHeightHeadingSmRegular = 25.sp
    val fontLineHeightBodyLgRegular = 22.sp
    val fontLineHeightBodyMdRegular = 20.sp
    val fontLineHeightBodySmRegular = 17.sp
    val fontLineHeightBodyXsRegular = 15.sp
    val fontWeightBold = FontWeight.W700
    val fontWeightMedium = FontWeight.W500
    val fontWeightRegular = FontWeight.W400
    const val fontFamilyMono = "Roboto Mono"
    const val fontFamilySans = "Roboto"
    val fontHeadingXlBold = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightBold,
        lineHeight = fontLineHeightHeadingXlRegular,
        fontSize = fontSizeHeadingXl,
        letterSpacing = fontLetterSpacingHeadingXl,
    )
    val fontHeadingXlRegular = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightRegular,
        lineHeight = fontLineHeightHeadingXlRegular,
        fontSize = fontSizeHeadingXl,
        letterSpacing = fontLetterSpacingHeadingXl,
    )
    val fontHeadingLgBold = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightBold,
        lineHeight = fontLineHeightHeadingLgRegular,
        fontSize = fontSizeHeadingLg,
        letterSpacing = fontLetterSpacingHeadingLg,
    )
    val fontHeadingLgRegular = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightRegular,
        lineHeight = fontLineHeightHeadingLgRegular,
        fontSize = fontSizeHeadingLg,
        letterSpacing = fontLetterSpacingHeadingLg,
    )
    val fontHeadingMdBold = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightBold,
        lineHeight = fontLineHeightHeadingMdRegular,
        fontSize = fontSizeHeadingMd,
        letterSpacing = fontLetterSpacingHeadingMd,
    )
    val fontHeadingMdRegular = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightRegular,
        lineHeight = fontLineHeightHeadingMdRegular,
        fontSize = fontSizeHeadingMd,
        letterSpacing = fontLetterSpacingHeadingMd,
    )
    val fontHeadingSmMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightMedium,
        lineHeight = fontLineHeightHeadingSmRegular,
        fontSize = fontSizeHeadingSm,
        letterSpacing = fontLetterSpacingHeadingSm,
    )
    val fontHeadingSmRegular = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightRegular,
        lineHeight = fontLineHeightHeadingSmRegular,
        fontSize = fontSizeHeadingSm,
        letterSpacing = fontLetterSpacingHeadingSm,
    )
    val fontBodyLgMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightMedium,
        lineHeight = fontLineHeightBodyLgRegular,
        fontSize = fontSizeBodyLg,
        letterSpacing = fontLetterSpacingBodyLg,
    )
    val fontBodyLgRegular = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightRegular,
        lineHeight = fontLineHeightBodyLgRegular,
        fontSize = fontSizeBodyLg,
        letterSpacing = fontLetterSpacingBodyLg,
    )
    val fontBodyMdMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightMedium,
        lineHeight = fontLineHeightBodyMdRegular,
        fontSize = fontSizeBodyMd,
        letterSpacing = fontLetterSpacingBodyMd,
    )
    val fontBodyMdRegular = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightRegular,
        lineHeight = fontLineHeightBodyMdRegular,
        fontSize = fontSizeBodyMd,
        letterSpacing = fontLetterSpacingBodyMd,
    )
    val fontBodySmMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightMedium,
        lineHeight = fontLineHeightBodySmRegular,
        fontSize = fontSizeBodySm,
        letterSpacing = fontLetterSpacingBodySm,
    )
    val fontBodySmRegular = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightRegular,
        lineHeight = fontLineHeightBodySmRegular,
        fontSize = fontSizeBodySm,
        letterSpacing = fontLetterSpacingBodySm,
    )
    val fontBodyXsMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightMedium,
        lineHeight = fontLineHeightBodyXsRegular,
        fontSize = fontSizeBodyXs,
        letterSpacing = fontLetterSpacingBodyXs,
    )
    val fontBodyXsRegular = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeightRegular,
        lineHeight = fontLineHeightBodyXsRegular,
        fontSize = fontSizeBodyXs,
        letterSpacing = fontLetterSpacingBodyXs,
    )
}
