/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.user.editprofile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.LabelledOutlinedTextField
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.modifiers.clearFocusOnTap
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.ui.components.AvatarActionBottomSheet
import io.element.android.libraries.matrix.ui.components.EditableAvatarView
import io.element.android.libraries.permissions.api.PermissionsView
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserProfileView(
    state: EditUserProfileState,
    onBackClick: () -> Unit,
    onEditProfileSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isAvatarActionsSheetVisible = remember { mutableStateOf(false) }

    fun onAvatarClick() {
        focusManager.clearFocus()
        isAvatarActionsSheetVisible.value = true
    }

    Scaffold(
        modifier = modifier.clearFocusOnTap(focusManager),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_edit_profile_title),
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
                navigationIcon = { BackButton(onClick = onBackClick) },
                actions = {
                    TextButton(
                        text = stringResource(CommonStrings.action_save),
                        enabled = state.saveButtonEnabled,
                        onClick = {
                            focusManager.clearFocus()
                            state.eventSink(EditUserProfileEvents.Save)
                        },
                    )
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            EditableAvatarView(
                matrixId = state.userId.value,
                displayName = state.displayName,
                avatarUrl = state.userAvatarUrl,
                avatarSize = AvatarSize.EditProfileDetails,
                onAvatarClick = { onAvatarClick() },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            // TCHAP hide the Matrix Id in release mode
            if (state.isDebugBuild) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = state.userId.value,
                    style = ElementTheme.typography.fontBodyLgRegular,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(40.dp))
                LabelledOutlinedTextField(
                    label = stringResource(R.string.screen_edit_profile_display_name),
                    value = state.displayName,
                    placeholder = stringResource(CommonStrings.common_room_name_placeholder),
                    singleLine = true,
                    onValueChange = { state.eventSink(EditUserProfileEvents.UpdateDisplayName(it)) },
                )
            }
        }

        AvatarActionBottomSheet(
            actions = state.avatarActions,
            isVisible = isAvatarActionsSheetVisible.value,
            onDismiss = { isAvatarActionsSheetVisible.value = false },
            onSelectAction = { state.eventSink(EditUserProfileEvents.HandleAvatarAction(it)) }
        )

        AsyncActionView(
            async = state.saveAction,
            progressDialog = {
                AsyncActionViewDefaults.ProgressDialog(
                    progressText = stringResource(R.string.screen_edit_profile_updating_details),
                )
            },
            onSuccess = { onEditProfileSuccess() },
            errorTitle = { stringResource(R.string.screen_edit_profile_error_title) },
            errorMessage = { stringResource(R.string.screen_edit_profile_error) },
            onErrorDismiss = { state.eventSink(EditUserProfileEvents.CancelSaveChanges) },
        )
    }
    PermissionsView(
        state = state.cameraPermissionState,
    )
}

@PreviewsDayNight
@Composable
internal fun EditUserProfileViewPreview(@PreviewParameter(EditUserProfileStateProvider::class) state: EditUserProfileState) =
    ElementPreview {
        EditUserProfileView(
            onBackClick = {},
            onEditProfileSuccess = {},
            state = state,
        )
    }
