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

package io.element.android.features.createroom.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.R as DrawableR
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomRootScreen(
    state: CreateRoomRootState,
    modifier: Modifier = Modifier,
    onClosePressed: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxWidth(),
        topBar = {
            CreateRoomRootViewTopBar(onClosePressed = onClosePressed)
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            SearchBar(
                modifier = Modifier.padding(horizontal = 16.dp),
                // TODO use resource string
                placeHolderTitle = "Search for someone",
                // TODO implement click behavior
                onClickDescription = "",
                onClick = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomRootViewTopBar(
    modifier: Modifier = Modifier,
    onClosePressed: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(id = StringR.string.start_chat),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            IconButton(onClick = onClosePressed) {
                Icon(resourceId = DrawableR.drawable.ic_close, contentDescription = stringResource(id = StringR.string.action_close))
            }
        }
    )
}

// TODO export into design system package
// TODO comment that SearchBar is not yet implemented in Material3 compose
//  and that TextField cannot be used since contentPadding cannot be changed
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    placeHolderTitle: String,
    onClickDescription: String = "",
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(28.dp),
            )
            .clickable(
                role = Role.Button,
                onClickLabel = onClickDescription,
                onClick = onClick,
            ),
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically)
                .weight(1f)
                .alpha(0.4f),
            text = placeHolderTitle,
        )
        Icon(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically)
                .alpha(0.4f),
            resourceId = DrawableR.drawable.ic_search,
            contentDescription = stringResource(id = StringR.string.search)
        )
    }
}

@Preview
@Composable
fun CreateRoomRootViewLightPreview(@PreviewParameter(CreateRoomRootStateProvider::class) state: CreateRoomRootState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun CreateRoomRootViewDarkPreview(@PreviewParameter(CreateRoomRootStateProvider::class) state: CreateRoomRootState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: CreateRoomRootState) {
    CreateRoomRootScreen(
        state = state,
    )
}
