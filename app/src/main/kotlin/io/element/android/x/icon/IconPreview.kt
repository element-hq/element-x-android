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

package io.element.android.x.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.x.R

@Preview
@Composable
fun IconPreview(
    modifier: Modifier = Modifier,
) {
    Box {
        Image(painter = painterResource(id = R.mipmap.ic_launcher_background), contentDescription = null)
        Image(painter = painterResource(id = R.mipmap.ic_launcher_foreground), contentDescription = null)
    }
}

@Preview
@Composable
fun RoundIconPreview(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clip(shape = CircleShape)) {
        Image(painter = painterResource(id = R.mipmap.ic_launcher_background), contentDescription = null)
        Image(painter = painterResource(id = R.mipmap.ic_launcher_foreground), contentDescription = null)
    }
}
