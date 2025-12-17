/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl.root

import io.element.android.libraries.matrix.api.user.MatrixUser

sealed interface StartChatEvents {
    data class StartDM(val matrixUser: MatrixUser) : StartChatEvents
    data object CancelStartDM : StartChatEvents
}
