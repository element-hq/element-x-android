/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import io.element.android.libraries.matrix.api.core.SessionId

sealed interface HomeEvents {
    data class SelectHomeNavigationBarItem(val item: HomeNavigationBarItem) : HomeEvents
    data class SwitchToAccount(val sessionId: SessionId) : HomeEvents
}
