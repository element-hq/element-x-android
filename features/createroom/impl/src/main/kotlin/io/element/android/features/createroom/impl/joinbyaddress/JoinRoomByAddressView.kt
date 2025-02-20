/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.joinbyaddress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TextFieldValidity
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRoomByAddressView(
    state: JoinRoomByAddressState,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = {
            state.eventSink(JoinRoomByAddressEvents.Dismiss)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RoomAddressField(
                address = state.address,
                addressState = state.addressState,
                requestFocus = sheetState.isVisible,
                onAddressChange = {
                    state.eventSink(JoinRoomByAddressEvents.UpdateAddress(it))
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                text = stringResource(CommonStrings.action_continue),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.addressState is RoomAddressState.Valid,
                onClick = {
                    state.eventSink(JoinRoomByAddressEvents.Continue)
                }
            )
        }
    }
}

@Composable
private fun RoomAddressField(
    address: String,
    addressState: RoomAddressState,
    requestFocus: Boolean,
    onAddressChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    if (requestFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
    TextField(
        modifier = modifier.focusRequester(focusRequester),
        value = address,
        label = "Join room by address",
        placeholder = "Enter...",
        supportingText = when (addressState) {
            RoomAddressState.Invalid -> "Not a valid address"
            RoomAddressState.Unknown -> "e.g. #room-name:matrix.org"
            is RoomAddressState.Valid -> if (addressState.matchingRoomFound) {
                "Matching room found"
            } else {
                "e.g. #room-name:matrix.org"
            }
        },
        validity = when (addressState) {
            RoomAddressState.Unknown -> null
            RoomAddressState.Invalid -> TextFieldValidity.Invalid
            is RoomAddressState.Valid -> if (addressState.matchingRoomFound) TextFieldValidity.Valid else null
        },
        onValueChange = onAddressChange,
        singleLine = true,
    )
}

@PreviewsDayNight
@Composable
internal fun JoinRoomByAddressViewPreview(
    @PreviewParameter(JoinRoomByAddressStateProvider::class) state: JoinRoomByAddressState
) = ElementPreview {
    JoinRoomByAddressView(state = state)
}

