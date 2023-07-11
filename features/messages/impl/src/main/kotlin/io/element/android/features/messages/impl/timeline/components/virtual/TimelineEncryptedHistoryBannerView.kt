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

package io.element.android.features.messages.impl.timeline.components.virtual

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.R
import io.element.android.libraries.theme.ElementTheme

@Composable
fun TimelineEncryptedHistoryBannerView(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp)
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, ElementTheme.colors.borderInfoSubtle, MaterialTheme.shapes.small)
            .background(ElementTheme.colors.bgInfoSubtle)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = ElementTheme.colors.iconInfoPrimary
        )
        Text(
            text = stringResource(R.string.screen_room_encrypted_history_banner),
            style = ElementTheme.typography.fontBodyMdMedium.copy(color = ElementTheme.colors.textInfoPrimary),
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun TimelineEncryptedHistoryBannerViewPreview() {
    ElementTheme {
        TimelineEncryptedHistoryBannerView()
    }
}
