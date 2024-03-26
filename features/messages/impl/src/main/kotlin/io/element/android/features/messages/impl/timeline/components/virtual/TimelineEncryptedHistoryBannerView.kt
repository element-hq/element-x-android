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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.session.SessionState
import io.element.android.features.messages.impl.timeline.session.SessionStateProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

@Composable
fun TimelineEncryptedHistoryBannerView(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp)
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, ElementTheme.colors.borderInfoSubtle, MaterialTheme.shapes.small)
            .background(ElementTheme.colors.bgInfoSubtle)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = CompoundIcons.InfoSolid(),
            contentDescription = null,
            tint = ElementTheme.colors.iconInfoPrimary
        )
        Text(
            text = stringResource(R.string.screen_room_encrypted_history_banner),
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textInfoPrimary
        )
    }
}

@PreviewsDayNight
@Composable
internal fun EncryptedHistoryBannerViewPreview(
    @PreviewParameter(SessionStateProvider::class) sessionState: SessionState,
) = ElementPreview {
    TimelineEncryptedHistoryBannerView()
}
