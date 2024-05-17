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

package io.element.android.features.userprofile.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.button.MainActionButton
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun UserProfileMainActionsSection(
    isCurrentUser: Boolean,
    canCall: Boolean,
    onShareUser: () -> Unit,
    onStartDM: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (!isCurrentUser) {
            MainActionButton(
                title = stringResource(CommonStrings.action_message),
                imageVector = CompoundIcons.Chat(),
                onClick = onStartDM,
            )
        }
        if (canCall) {
            MainActionButton(
                title = stringResource(CommonStrings.action_call),
                imageVector = CompoundIcons.VideoCall(),
                onClick = onCall,
            )
        }
        MainActionButton(
            title = stringResource(CommonStrings.action_share),
            imageVector = CompoundIcons.ShareAndroid(),
            onClick = onShareUser
        )
    }
}
