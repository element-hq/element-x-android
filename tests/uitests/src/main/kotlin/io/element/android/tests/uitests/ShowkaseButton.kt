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

package io.element.android.tests.uitests

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun ShowkaseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    var isShowkaseButtonVisible by remember { mutableStateOf(BuildConfig.DEBUG) }

    if (isShowkaseButtonVisible) {
        Button(
            modifier = modifier
                .padding(top = 32.dp),
            onClick = onClick
        ) {
            Text(text = "Showkase Browser")
            IconButton(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp),
                onClick = { isShowkaseButtonVisible = false },
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "")
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ShowkaseButtonPreview() = ElementPreview {
    ShowkaseButton()
}
