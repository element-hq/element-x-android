/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import io.element.android.libraries.matrix.api.core.SessionId

fun interface RoomSelectNavigator {
    /**
     * Navigate to the verification of the session [sessionId], which has to be verified before content can be shared with it.
     */
    fun navigateToSessionVerification(sessionId: SessionId)
}
