/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import io.element.android.libraries.androidutils.ui.hideKeyboard
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapTypeBottomSheet(
    state: MapRealtimePresenterState,
    onTileProviderSelected: (MapType) -> Unit,
    modifier: Modifier = Modifier
) {
    val localView = LocalView.current
    var isVisible by rememberSaveable { mutableStateOf(state.showMapTypeDialog) }

    BackHandler {
        isVisible = false
    }

    LaunchedEffect(state.showMapTypeDialog) {
        isVisible = if (state.showMapTypeDialog) {
            // We need to use this instead of `LocalFocusManager.clearFocus()` to hide the keyboard when focus is on an Android View
            localView.hideKeyboard()
            true
        } else {
            false
        }
    }

    // Send 'DismissAttachmentMenu' event when the bottomsheet was just hidden
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            state.eventSink(MapRealtimeEvents.CloseMapTypeDialog)
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            modifier = modifier,
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            onDismissRequest = { isVisible = false }
        ) {
            Row {
                Column(Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)) {
                    Text(
                        text = "CHOOSE MAP "
                    )
                    Text(
                        text = "Select map type",
                    )
                }

            }
            TileProviderPickerMenu(
                state = state,
                onTileProviderSelected = onTileProviderSelected
            )
        }
    }
}

@Composable
private fun ProviderItem(
    selected: Boolean,
    provider: MapType,
    onTileProviderSelected: (MapType) -> Unit
) {
    Surface(onClick = { onTileProviderSelected(provider) }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .background(Color.Gray, shape = RoundedCornerShape(8.dp)) // Add rounded corners
                    .size(72.dp)
                    .border(
                        width = 2.dp,
                        color = if (selected) Color.Blue else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            Text(text = provider.displayName.uppercase(), modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun TileProviderPickerMenu(
    state: MapRealtimePresenterState,
    onTileProviderSelected: (MapType) -> Unit
) {
    // TODO (tb): create a single source of truth for the list of Map providers
    val mapTileProviders = listOf(
        MapType("OSM", "openstreetmap"), MapType("Satellite", "satellite"),
        MapType("Streets", "streets-v2"), MapType("TOPO", "topo-v2")
    )

    // A row of 4 options with an image and text below each
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp, 0.dp, 0.dp, 16.dp)
    ) {
        for (i in mapTileProviders) {
            ProviderItem(
                provider = i,
                onTileProviderSelected = onTileProviderSelected,
                selected = state.mapType.mapKey == i.mapKey
            )
        }
    }
}

