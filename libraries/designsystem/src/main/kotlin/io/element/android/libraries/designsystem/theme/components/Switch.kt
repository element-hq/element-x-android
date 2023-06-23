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

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Preview(group = PreviewGroup.Toggles)
@Composable
internal fun SwitchPreview() {
    ElementThemedPreview {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var checked by remember { mutableStateOf(false) }
            Switch(checked = checked, onCheckedChange = { checked = !checked })
            Switch(checked = checked, onCheckedChange = { checked = !checked }, thumbContent = {
                Icon(imageVector = Icons.Outlined.Check, contentDescription = null)
            })
            Switch(checked = checked, enabled = false, onCheckedChange = { checked = !checked })
            Switch(checked = checked, enabled = false, onCheckedChange = { checked = !checked }, thumbContent = {
                Icon(imageVector = Icons.Outlined.Check, contentDescription = null)
            })
        }
    }
}
