/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import io.element.android.features.call.api.CallType
import io.element.android.libraries.matrix.api.core.SessionId

fun CallType.getSessionId(): SessionId? {
    return when (this) {
        is CallType.ExternalUrl -> null
        is CallType.RoomCall -> sessionId
    }
}
