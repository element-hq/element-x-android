/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components.accountinfo

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.RoundedIconAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoundedIconAtomSize
import io.element.android.libraries.designsystem.atomic.molecules.TextWithLabelMolecule
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=5873-996
 */
@Composable
fun AccountInfoCard(
    state: AccountInfoCardState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElementTheme.colors.borderDisabled, RoundedCornerShape(8.dp))
            .padding(20.dp),
    ) {
        AccountInfoCardUser(
            matrixUser = state.matrixUser,
        )
        when (state.mode) {
            AccountInfoCardMode.Simple -> Unit
            is AccountInfoCardMode.DeviceVerification -> {
                AccountInfoSeparator()
                DeviceInfoRow(state.mode)
            }
            is AccountInfoCardMode.UserVerification -> {
                AccountInfoSeparator()
                Text(
                    text = state.mode.hint,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                AccountInfoCardUser(
                    matrixUser = state.mode.otherUser,
                )
            }
        }
    }
}

@Composable
private fun AccountInfoSeparator() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 20.dp),
        color = ElementTheme.colors.separatorPrimary,
    )
}

@Composable
private fun DeviceInfoRow(
    mode: AccountInfoCardMode.DeviceVerification,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundedIconAtom(
                modifier = Modifier,
                size = RoundedIconAtomSize.Big,
                resourceId = CompoundDrawables.ic_compound_devices
            )
            Text(
                text = mode.deviceName ?: mode.deviceId.value,
                style = ElementTheme.typography.fontBodyMdMedium,
                color = ElementTheme.colors.textPrimary,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextWithLabelMolecule(
                label = stringResource(CommonStrings.common_signed_in),
                text = mode.signInFormattedTimestamp,
                modifier = Modifier.weight(1f),
            )
            TextWithLabelMolecule(
                label = stringResource(CommonStrings.common_device_id),
                text = mode.deviceId.value,
                modifier = Modifier.weight(1f),
                spellText = true,
            )
        }
    }
}

@Composable
private fun AccountInfoCardUser(matrixUser: MatrixUser) {
    val subtext = if (matrixUser.displayName.isNullOrEmpty()) null else matrixUser.userId.value
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = matrixUser.getAvatarData(AvatarSize.AccountInfoUser),
            avatarType = AvatarType.User,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            // Name
            Text(
                modifier = Modifier.clipToBounds(),
                text = matrixUser.getBestName(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontBodyLgMedium,
            )
            // Id
            subtext?.let {
                Text(
                    text = subtext,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AccountInfoCardPreview(
    @PreviewParameter(AccountInfoCardStatePreviewParam::class) state: AccountInfoCardState
) = ElementPreview {
    // Add some padding to view the border
    Box(modifier = Modifier.padding(16.dp)) {
        AccountInfoCard(state)
    }
}
