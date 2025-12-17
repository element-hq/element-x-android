/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.editroomaddress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.securityandprivacy.impl.R
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.ui.room.address.RoomAddressField
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun EditRoomAddressView(
    state: EditRoomAddressState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EditRoomAddressTopBar(
                isSaveActionEnabled = state.canBeSaved,
                onBackClick = onBackClick,
                onSaveClick = {
                    state.eventSink(EditRoomAddressEvents.Save)
                },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .consumeWindowInsets(padding)
        ) {
            RoomAddressField(
                address = state.roomAddress,
                homeserverName = state.homeserverName,
                addressValidity = state.roomAddressValidity,
                onAddressChange = {
                    state.eventSink(EditRoomAddressEvents.RoomAddressChanged(it))
                },
                label = stringResource(R.string.screen_edit_room_address_title),
                supportingText = stringResource(R.string.screen_edit_room_address_room_address_section_footer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            )
        }
        AsyncActionView(
            async = state.saveAction,
            progressDialog = {
                AsyncActionViewDefaults.ProgressDialog(
                    progressText = stringResource(CommonStrings.common_saving),
                )
            },
            onSuccess = {},
            errorMessage = { stringResource(CommonStrings.error_unknown) },
            onRetry = { state.eventSink(EditRoomAddressEvents.Save) },
            onErrorDismiss = { state.eventSink(EditRoomAddressEvents.DismissError) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRoomAddressTopBar(
    isSaveActionEnabled: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        titleStr = stringResource(R.string.screen_edit_room_address_title),
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_save),
                enabled = isSaveActionEnabled,
                onClick = onSaveClick,
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun EditRoomAddressViewPreview(
    @PreviewParameter(EditRoomAddressStateProvider::class) state: EditRoomAddressState
) = ElementPreview {
    EditRoomAddressView(
        state = state,
        onBackClick = {},
    )
}
