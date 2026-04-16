/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.molecules.ButtonRowMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.R
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getFullName
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Figma:
 * https://www.figma.com/design/dywzKQvHYxFD1Ncn4a5NkI/PSB-675%253A-Improve-invite-into-a-DM?node-id=12-36886
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDmConfirmationBottomSheet(
    matrixUser: MatrixUser,
    enableKeyShareOnInvite: Boolean,
    isUserIdentityUnknown: Boolean,
    onSendInvite: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleContent = if (enableKeyShareOnInvite && isUserIdentityUnknown) {
        stringResource(R.string.screen_bottom_sheet_create_dm_unknown_user_title)
    } else {
        stringResource(R.string.screen_bottom_sheet_create_dm_title)
    }
    val descriptionContent = if (enableKeyShareOnInvite && isUserIdentityUnknown) {
        stringResource(R.string.screen_bottom_sheet_create_dm_unknown_user_content)
    } else {
        stringResource(R.string.screen_bottom_sheet_create_dm_message, matrixUser.getFullName())
    }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isUserIdentityUnknown) {
                IconTitleSubtitleMolecule(
                    modifier = Modifier.padding(
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
                    title = titleContent,
                    subTitle = descriptionContent,
                    iconStyle = BigIcon.Style.Default(CompoundIcons.UserAddSolid()),
                )
                MatrixUserRow(matrixUser)
                Spacer(modifier = Modifier.height(32.dp))
                ButtonRowMolecule(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(CommonStrings.action_cancel),
                        onClick = onDismiss
                    )
                    Button(
                        modifier = Modifier.weight(1f),
                        text = stringResource(CommonStrings.action_continue),
                        onClick = onSendInvite
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            } else {
                Avatar(
                    avatarData = matrixUser.getAvatarData(AvatarSize.DmCreationConfirmation),
                    avatarType = AvatarType.User,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = titleContent,
                    style = ElementTheme.typography.fontHeadingMdBold,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = descriptionContent,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSendInvite,
                    leadingIcon = IconSource.Vector(CompoundIcons.UserAdd()),
                    text = stringResource(R.string.screen_bottom_sheet_create_dm_confirmation_button_title),
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss,
                    text = stringResource(CommonStrings.action_cancel),
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun CreateDmConfirmationBottomSheetPreview(@PreviewParameter(
    CreateDmConfirmationBottomSheetStateProvider::class
) state: CreateDmConfirmationBottomSheetState) = ElementPreview {
    CreateDmConfirmationBottomSheet(
        matrixUser = state.matrixUser,
        enableKeyShareOnInvite = state.enableKeyShareOnInvite,
        isUserIdentityUnknown = state.isUserIdentityUnknown,
        onSendInvite = {},
        onDismiss = {},
    )
}

data class CreateDmConfirmationBottomSheetState(
    val matrixUser: MatrixUser,
    val enableKeyShareOnInvite: Boolean,
    val isUserIdentityUnknown: Boolean,
)

class CreateDmConfirmationBottomSheetStateProvider : PreviewParameterProvider<CreateDmConfirmationBottomSheetState> {
    override val values = sequenceOf(
        CreateDmConfirmationBottomSheetState(matrixUser = aMatrixUser(), enableKeyShareOnInvite = false, isUserIdentityUnknown = false),
            CreateDmConfirmationBottomSheetState(matrixUser = aMatrixUser(), enableKeyShareOnInvite = true, isUserIdentityUnknown = false),
            CreateDmConfirmationBottomSheetState(matrixUser = aMatrixUser(), enableKeyShareOnInvite = true, isUserIdentityUnknown = true),
        )
}
