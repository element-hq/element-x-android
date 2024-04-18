/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.designsystem.atomic.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoomPreviewOrganism(
    avatar: @Composable () -> Unit,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    memberCount: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        avatar()
        Spacer(modifier = Modifier.height(16.dp))
        title()
        Spacer(modifier = Modifier.height(8.dp))
        subtitle()
        Spacer(modifier = Modifier.height(8.dp))
        if (memberCount != null) {
            memberCount()
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (description != null) {
            description()
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
