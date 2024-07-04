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

package io.element.android.appicon.element

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
internal fun IconPreview() {
    Box {
        Image(painter = painterResource(id = R.mipmap.ic_launcher_background), contentDescription = null)
        Image(painter = painterResource(id = R.mipmap.ic_launcher_foreground), contentDescription = null)
    }
}

@Preview
@Composable
internal fun RoundIconPreview() {
    Box(modifier = Modifier.clip(shape = CircleShape)) {
        Image(painter = painterResource(id = R.mipmap.ic_launcher_background), contentDescription = null)
        Image(painter = painterResource(id = R.mipmap.ic_launcher_foreground), contentDescription = null)
    }
}

@Preview
@Composable
internal fun MonochromeIconPreview() {
    Box(
        modifier = Modifier
            .size(108.dp)
            .background(Color(0xFF2F3133))
            .clip(shape = RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_monochrome),
            colorFilter = ColorFilter.tint(Color(0xFFC3E0F6)),
            contentDescription = null
        )
    }
}
