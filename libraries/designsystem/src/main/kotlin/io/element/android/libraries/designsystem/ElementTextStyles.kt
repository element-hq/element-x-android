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

package io.element.android.libraries.designsystem

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// TODO Remove
object ElementTextStyles {

    @Suppress("DEPRECATION")
    val Button = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp,
        fontStyle = FontStyle.Normal,
        textAlign = TextAlign.Center,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    object Bold {
        val largeTitle = TextStyle(
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            lineHeight = 41.sp,
            textAlign = TextAlign.Center
        )

        val title1 = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center
        )

        val title2 = TextStyle(
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center
        )

        val title3 = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center
        )

        val headline = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        val body = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        val callout = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )

        val subheadline = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )

        val footnote = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

        val caption1 = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Normal,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )

        val caption2 = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Normal,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center
        )
    }

    object Regular {
        val largeTitle = TextStyle(
            fontSize = 34.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 41.sp,
            textAlign = TextAlign.Center
        )

        val title1 = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center
        )

        val title2 = TextStyle(
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center
        )

        val title3 = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center
        )

        val headline = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        val body = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        val callout = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )

        val subheadline = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )

        val formHeader = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 20.sp,
            textAlign = TextAlign.Start
        )

        val bodyMD = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 20.sp,
            textAlign = TextAlign.Start
        )

        val footnote = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

        val caption1 = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )

        val caption2 = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
