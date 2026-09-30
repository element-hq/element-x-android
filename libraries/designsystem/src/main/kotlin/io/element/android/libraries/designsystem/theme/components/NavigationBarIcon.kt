/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.CounterAtom

@Composable
fun NavigationBarIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    count: Int = 0,
    isCritical: Boolean = false,
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            CounterAtom(
                textStyle = ElementTheme.typography.fontBodyXsMedium,
                count = count,
                isCritical = isCritical,
            )
        }) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
        )
    }
}
