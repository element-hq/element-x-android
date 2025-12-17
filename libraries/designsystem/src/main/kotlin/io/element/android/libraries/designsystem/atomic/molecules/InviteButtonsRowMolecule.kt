/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.molecules

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun InviteButtonsRowMolecule(
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit,
    modifier: Modifier = Modifier,
    declineText: String = stringResource(CommonStrings.action_decline),
    acceptText: String = stringResource(CommonStrings.action_accept),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(12.dp)
    ) {
        OutlinedButton(
            text = declineText,
            onClick = onDeclineClick,
            size = ButtonSize.MediumLowPadding,
            modifier = Modifier.weight(1f),
        )
        Button(
            text = acceptText,
            onClick = onAcceptClick,
            size = ButtonSize.MediumLowPadding,
            modifier = Modifier.weight(1f),
        )
    }
}
