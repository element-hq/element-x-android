/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import io.element.android.libraries.matrix.api.core.RoomId

sealed interface ManageAuthorizedSpacesEvent {
    data object Cancel : ManageAuthorizedSpacesEvent
    data object Done : ManageAuthorizedSpacesEvent
    data class ToggleSpace(val roomId: RoomId) : ManageAuthorizedSpacesEvent
}
