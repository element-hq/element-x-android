/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.api

import androidx.compose.runtime.Composable

interface DeviceUnlockPrompt {
    @Composable
    fun OnUnlockEffect(onUnlockResult: (Boolean) -> Unit)

    @Composable
    fun ShowPrompt()
}
