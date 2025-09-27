/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import io.element.android.libraries.matrix.api.core.SessionId

sealed interface PreferencesRootEvents {
    data object OnVersionInfoClick : PreferencesRootEvents
    data class SwitchToSession(val sessionId: SessionId) : PreferencesRootEvents
}
