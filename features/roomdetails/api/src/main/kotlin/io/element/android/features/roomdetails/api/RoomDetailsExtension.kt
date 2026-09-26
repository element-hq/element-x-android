/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Optional room-details sections contributed by a build-specific implementation. */
interface RoomDetailsExtension {
    @Composable
    fun Render(modifier: Modifier)
}
