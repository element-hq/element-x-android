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

package io.element.android.features.roomlist.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.roomListPlaceHolder

/**
 * https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?node-id=6547%3A147623
 */
@Composable
internal fun RoomSummaryPlaceholderRow(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(minHeight)
            .padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(AvatarSize.RoomListItem.dp)
                .align(Alignment.CenterVertically)
                .background(color = ElementTheme.colors.roomListPlaceHolder(), shape = CircleShape)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 19.dp, end = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlaceholderAtom(width = 40.dp, height = 7.dp)
                Spacer(modifier = Modifier.width(7.dp))
                PlaceholderAtom(width = 45.dp, height = 7.dp)
                Spacer(modifier = Modifier.weight(1f))
                PlaceholderAtom(width = 22.dp, height = 4.dp)
            }
            Row(
                modifier = Modifier
                    .height(25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlaceholderAtom(width = 70.dp, height = 6.dp)
                Spacer(modifier = Modifier.width(6.dp))
                PlaceholderAtom(width = 70.dp, height = 6.dp)
            }
        }
    }
}

@Preview
@Composable
internal fun RoomSummaryPlaceholderRowLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun RoomSummaryPlaceholderRowDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    RoomSummaryPlaceholderRow()
}
