/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.CounterAtom

@Composable
fun NavigationBarIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    count: Int = 0,
    isCritical: Boolean = false,
) {
    Box(modifier) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
        )
        CounterAtom(
            modifier = Modifier.offset(11.dp, (-11).dp),
            textStyle = ElementTheme.typography.fontBodyXsMedium,
            count = count,
            isCritical = isCritical,
        )
    }
}
