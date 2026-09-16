/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * Show/hide toggle for a password [TextField], intended for its `trailingIcon` slot.
 * Shared so every password field reveals plaintext the same way and announces the
 * same accessibility labels.
 */
@Composable
fun PasswordVisibilityToggle(
    visible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.clickable(onClick = onToggle)) {
        Icon(
            imageVector = if (visible) CompoundIcons.VisibilityOn() else CompoundIcons.VisibilityOff(),
            contentDescription = stringResource(
                if (visible) CommonStrings.a11y_hide_password else CommonStrings.a11y_show_password
            ),
        )
    }
}
