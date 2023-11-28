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

package io.element.android.libraries.designsystem.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.WithFontScale
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.compound.theme.ElementTheme

/**
 * Return the maximum value between the receiver value and the value with fontScale applied.
 * So if fontScale is >= 1f, the same value is returned, and if fontScale is < 1f, so returned value
 * will be smaller.
 */
@Composable
fun Dp.applyScaleDown(): Dp = with(LocalDensity.current) {
    return this@applyScaleDown * fontScale.coerceAtMost(1f)
}

/**
 * Return the minimum value between the receiver value and the value with fontScale applied.
 * So if fontScale is <= 1f, the same value is returned, and if fontScale is > 1f, so returned value
 * will be bigger.
 */
@Composable
fun Dp.applyScaleUp(): Dp = with(LocalDensity.current) {
    return this@applyScaleUp * fontScale.coerceAtLeast(1f)
}

@Preview
@Composable
internal fun DpScale_0_75f_Preview() = WithFontScale(0.75f) {
    ElementPreviewLight {
        val fontSizeInDp = 16.dp
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Text with size of 16.sp",
                style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = fontSizeInDp.toSp())
            )
            Text(
                text = "Text with the same size (applyScaleUp)",
                style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = fontSizeInDp.applyScaleUp().toSp())
            )
            Text(
                text = "Text with a smaller size (applyScaleDown)",
                style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = fontSizeInDp.applyScaleDown().toSp())
            )
        }
    }
}

@Preview
@Composable
internal fun DpScale_1_0f_Preview() = WithFontScale(1f) {
    ElementPreviewLight {
        val fontSizeInDp = 16.dp
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Text with size of 16.sp",
                style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = fontSizeInDp.toSp())
            )
            Text(
                text = "Text with the same size (applyScaleUp)",
                style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = fontSizeInDp.applyScaleUp().toSp())
            )
            Text(
                text = "Text with the same size (applyScaleDown)",
                style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = fontSizeInDp.applyScaleDown().toSp())
            )
        }
    }
}

@Preview
@Composable
internal fun DpScale_1_5f_Preview() = WithFontScale(1.5f) {
    ElementPreviewLight {
        val fontSizeInDp = 16.dp
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Text with size of 16.sp",
                style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = fontSizeInDp.toSp())
            )
            Text(
                text = "Text with a bigger size (applyScaleUp)",
                style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = fontSizeInDp.applyScaleUp().toSp())
            )
            Text(
                text = "Text with the same size (applyScaleDown)",
                style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = fontSizeInDp.applyScaleDown().toSp())
            )
        }
    }
}
