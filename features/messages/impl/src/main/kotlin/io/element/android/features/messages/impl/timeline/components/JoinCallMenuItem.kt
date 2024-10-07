/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun JoinCallMenuItem(
    onJoinCallClick: () -> Unit,
) {
    Button(
        onClick = onJoinCallClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = ElementTheme.colors.bgCanvasDefault,
            containerColor = ElementTheme.colors.iconAccentTertiary
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        modifier = Modifier.heightIn(min = 36.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = CompoundIcons.VideoCallSolid(),
            contentDescription = null
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(CommonStrings.action_join),
            style = ElementTheme.typography.fontBodyMdMedium
        )
        Spacer(Modifier.width(8.dp))
    }
}
