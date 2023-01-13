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

package io.element.android.x.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ShowkaseButton(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onClick: () -> Unit,
    onCloseClicked: () -> Unit
) {
    if (isVisible) {
        Button(
            modifier = Modifier
                .padding(top = 32.dp, start = 16.dp),
            onClick = onClick
        ) {
            Text(text = "Showkase Browser")
            IconButton(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(16.dp),
                onClick = onCloseClicked,
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "")
            }
        }
    }
}
