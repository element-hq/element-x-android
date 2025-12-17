/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl.joinbyaddress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.startchat.impl.R
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
                },
                onContinue = {
                    state.eventSink(JoinRoomByAddressEvents.Continue)
                },
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                text = stringResource(CommonStrings.action_continue),
                modifier = Modifier.fillMaxWidth(),
                showProgress = state.addressState is RoomAddressState.Resolving,
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
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    if (requestFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
    TextField(
        modifier = modifier.focusRequester(focusRequester),
        value = address,
        label = stringResource(R.string.screen_start_chat_join_room_by_address_action),
        placeholder = stringResource(R.string.screen_start_chat_join_room_by_address_placeholder),
        supportingText = when (addressState) {
            RoomAddressState.Invalid -> stringResource(R.string.screen_start_chat_join_room_by_address_invalid_address)
            is RoomAddressState.RoomFound -> stringResource(R.string.screen_start_chat_join_room_by_address_room_found)
            RoomAddressState.RoomNotFound -> stringResource(R.string.screen_start_chat_join_room_by_address_room_not_found)
            RoomAddressState.Unknown, RoomAddressState.Resolving -> stringResource(R.string.screen_start_chat_join_room_by_address_supporting_text)
        },
        validity = when (addressState) {
            RoomAddressState.Unknown, RoomAddressState.Resolving -> TextFieldValidity.None
            RoomAddressState.Invalid, RoomAddressState.RoomNotFound -> TextFieldValidity.Invalid
            is RoomAddressState.RoomFound -> TextFieldValidity.Valid
        },
        onValueChange = onAddressChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(
            onGo = { onContinue() }
        )
    )
}

@PreviewsDayNight
@Composable
internal fun JoinRoomByAddressViewPreview(
    @PreviewParameter(JoinRoomByAddressStateProvider::class) state: JoinRoomByAddressState
) = ElementPreview {
    JoinRoomByAddressView(state = state)
}
